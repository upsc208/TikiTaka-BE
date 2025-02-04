package com.trillion.tikitaka.attachment.application;

import com.trillion.tikitaka.attachment.domain.Attachment;
import com.trillion.tikitaka.attachment.exception.*;
import com.trillion.tikitaka.attachment.infrastructure.AttachmentRepository;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.exception.CustomException;
import com.trillion.tikitaka.global.exception.ErrorCode;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.ticketcomment.domain.TicketComment;
import com.trillion.tikitaka.ticketcomment.exception.TicketCommentNotFoundException;
import com.trillion.tikitaka.ticketcomment.infrastructure.TicketCommentRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.exception.UserNotFoundException;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(List.of("png", "jpg", "jpeg"));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${kakaocloud.object-storage.endpoint}")
    private String endpoint;

    @Value("${kakaocloud.object-storage.bucket-name}")
    private String bucketName;

    @Value("${kakaocloud.object-storage.iam.project-id}")
    private String projectId;

    private final S3Client s3Client;
    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserRepository userRepository;

    @Transactional
    public String uploadFile(MultipartFile file, String prefix, String ticketId, String commentId,
                             String userId, CustomUserDetails currentUser) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidFileNameException();
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileExetensionException();
        }

        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        String keyPrefix = switch (prefix.toUpperCase()) {
            case "USER" -> "users/" + currentUser.getId();
            case "TICKET" -> {
                if (ticketId == null || ticketId.isEmpty()) {
                    throw new InvalidInputExeption();
                }
                yield "tickets/" + ticketId;
            }
            case "COMMENT" -> {
                if (ticketId == null || ticketId.isEmpty() || commentId == null || commentId.isEmpty()) {
                    throw new InvalidInputExeption();
                }
                yield "tickets/" + ticketId + "/" + commentId;
            }
            default -> throw new InvalidInputExeption();
        };


        String s3Key = keyPrefix + "/" + uniqueFileName;
        String fileUrl;
        try {
            Path tempFile = Files.createTempFile("upload-", uniqueFileName);
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempFile));
            Files.deleteIfExists(tempFile);

            fileUrl = endpoint + "/v1/" + projectId + "/" + bucketName + "/" + s3Key;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.", e);
        }

        /**
         * 현재 로그인한 사용자만 자신의 프로필 업데이트 가능
         * 일반 사용자는 자신이 요청한 티켓에만 파일 첨부 가능
         * 일반 사용자는 자신이 작성한 댓글에만 파일 첨부 가능
         */
        switch (prefix.toUpperCase()) {
            case "USER":
                if (!userId.equals(currentUser.getId().toString())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
                User user = userRepository.findById(currentUser.getId())
                        .orElseThrow(UserNotFoundException::new);
                user.updateProfileImageUrl(fileUrl);
                userRepository.save(user);
                break;
            case "TICKET":
                Ticket ticket = ticketRepository.findById(Long.valueOf(ticketId))
                        .orElseThrow(TicketNotFoundException::new);
                if (currentUser.getUser().getRole() == Role.USER && !ticket.getRequester().getId().equals(currentUser.getId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
                Attachment ticketAttachment = Attachment.builder()
                        .ticket(ticket)
                        .comment(null)
                        .fileName(uniqueFileName)
                        .filePath(s3Key)
                        .fileSize(file.getSize())
                        .build();
                attachmentRepository.save(ticketAttachment);
                break;
            case "COMMENT":
                Ticket ticketForComment = ticketRepository.findById(Long.valueOf(ticketId))
                        .orElseThrow(TicketNotFoundException::new);
                TicketComment comment = ticketCommentRepository.findById(Long.valueOf(commentId))
                        .orElseThrow(TicketCommentNotFoundException::new);
                if (!comment.getAuthor().getId().equals(currentUser.getId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
                Attachment commentAttachment = Attachment.builder()
                        .ticket(ticketForComment)
                        .comment(comment)
                        .fileName(uniqueFileName)
                        .filePath(s3Key)
                        .fileSize(file.getSize())
                        .build();
                attachmentRepository.save(commentAttachment);
                break;
            default:
                throw new InvalidInputExeption();
        }

        return fileUrl;
    }

    /**
     * 사용자는 자신이 요청한 티켓, 자신이 작성한 댓글의 파일만 삭제 가능
     * 담당자는 모든 티켓의 파일 삭제 가능, 자신이 작성한 댓글의 파일만 삭제 가능
     */
    @Transactional
    public void deleteFile(Long attachmentId, CustomUserDetails currentUser) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(FileNotFoundException::new);

        if (currentUser.getUser().getRole() == Role.USER) {
            // 일반 사용자의 경우:
            if (attachment.getTicket() != null && attachment.getComment() == null) {
                // 티켓 첨부파일: 첨부파일의 티켓 요청자와 현재 사용자 일치 확인
                if (!attachment.getTicket().getRequester().getId().equals(currentUser.getId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
            } else if (attachment.getComment() != null) {
                // 댓글 첨부파일: 첨부파일의 댓글 작성자와 현재 사용자 일치 확인
                if (!attachment.getComment().getAuthor().getId().equals(currentUser.getId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
            }
        } else {
            // 담당자의 경우:
            // 티켓 첨부파일은 제한 없이 삭제 가능
            if (attachment.getComment() != null) {
                // 댓글 첨부파일: 담당자도 자신이 작성한 댓글 첨부파일만 삭제 가능
                if (!attachment.getComment().getAuthor().getId().equals(currentUser.getId())) {
                    throw new CustomException(ErrorCode.ACCESS_DENIED);
                }
            }
        }

        String s3Key = attachment.getFilePath();
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다.", e);
        }

        attachmentRepository.delete(attachment);
    }
}
