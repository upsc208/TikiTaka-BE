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
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
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
    private final UserRepository userRepository;

    @Transactional
    public void uploadUserProfile(MultipartFile file, Long userId) {
        log.info("[파일 업로드] 사용자 프로필 이미지 업로드 요청 - userId: {}", userId);

        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("[파일 업로드] 파일 크기 초과");
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
            log.error("[파일 업로드] 파일 이름 오류");
            throw new CustomException(ErrorCode.INVALID_FILE_NAME);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.error("[파일 업로드] 허용되지 않는 파일 확장자");
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getProfileImageUrl() != null) {
            log.info("[파일 업로드] 기존 프로필 이미지 삭제");
            String existingFileUrl = user.getProfileImageUrl();
            String prefix = endpoint + "/v1/" + projectId + "/" + bucketName + "/";
            if (existingFileUrl.startsWith(prefix)) {
                String existingS3Key = existingFileUrl.substring(prefix.length());
                try {
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(existingS3Key)
                            .build();
                    s3Client.deleteObject(deleteRequest);
                } catch (Exception e) {
                    log.error("[파일 업로드] 파일 삭제 실패");
                    throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
                }
            }
        }

        try {
            log.info("[파일 업로드] 새 프로필 이미지 업로드");
            Path tempFilePath = Files.createTempFile(null, null);
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String s3Key = "users/" + user.getId() + "/" + today + "profile" + user.getId() + "." + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.putObject(putObjectRequest, tempFilePath);

            String fileUrl = endpoint + "/v1/" + projectId + "/" + bucketName + "/" + s3Key;
            log.info("[파일 업로드] 파일 업로드 성공 - URL: {}", fileUrl);

            Files.delete(tempFilePath);

            user.updateProfileImageUrl(fileUrl);
            userRepository.save(user);
        } catch (IOException e) {
            log.error("[파일 업로드] 파일 업로드 실패");
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional
    public void uploadFilesForTicket(List<MultipartFile> files, Ticket ticket) {
        log.info("[파일 업로드] 티켓 첨부파일 업로드 요청 - ticketId: {}", ticket.getId());
        {
            if (files.size() > 5) {
                log.error("[파일 업로드] 파일 개수 초과");
                throw new CustomException(ErrorCode.TOO_MANY_FILES);
            }

            List<Attachment> attachments = new ArrayList<>();

            files.forEach(file -> {
                if (file.getSize() > MAX_FILE_SIZE) {
                    log.error("[파일 업로드] 파일 크기 초과");
                    throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
                }

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
                    log.error("[파일 업로드] 파일 이름 오류");
                    throw new CustomException(ErrorCode.INVALID_FILE_NAME);
                }

                String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    log.error("[파일 업로드] 허용되지 않는 파일 확장자");
                    throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
                }

                try {
                    log.info("[파일 업로드] 파일 업로드 시작");
                    Path path = Files.createTempFile(null, null);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                    String uuid = UUID.randomUUID().toString();
                    String fileName = today + "ticket" + ticket.getId() + "-" + uuid + "." + extension;
                    String s3Key = "tickets/" + ticket.getId() + "/" + fileName;

                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build();

                    s3Client.putObject(putObjectRequest, path);

                    String fileUrl = endpoint + "/v1/" + projectId + "/" + bucketName + "/" + s3Key;
                    log.info("[파일 업로드] 파일 업로드 성공 - URL: {}", fileUrl);

                    Files.delete(path);

                    Attachment attachment = Attachment.builder()
                            .ticket(ticket)
                            .fileName(fileName)
                            .filePath(fileUrl)
                            .fileSize(file.getSize())
                            .build();

                    attachmentRepository.save(attachment);
                    attachments.add(attachment);

                } catch (IOException e) {
                    log.error("[파일 업로드] 파일 업로드 실패");
                    throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
                }
            });
        }
    }

    @Transactional
    public void uploadFilesForComment(List<MultipartFile> files, TicketComment comment) {
        {
            if (files.size() > 5) {
                log.info("[파일 업로드] 파일 개수 초과");
                throw new CustomException(ErrorCode.TOO_MANY_FILES);
            }

            List<Attachment> attachments = new ArrayList<>();

            files.forEach(file -> {
                if (file.getSize() > MAX_FILE_SIZE) {
                    log.info("[파일 업로드] 파일 크기 초과");
                    throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
                }

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
                    log.info("[파일 업로드] 파일 이름 오류");
                    throw new CustomException(ErrorCode.INVALID_FILE_NAME);
                }

                String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    log.info("[파일 업로드] 허용되지 않는 파일 확장자");
                    throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
                }

                try {
                    log.info("[파일 업로드] 파일 업로드 시작");
                    Path path = Files.createTempFile(null, null);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                    String uuid = UUID.randomUUID().toString();
                    String fileName = today + "comment" + comment.getId() + "-" + uuid + "." + extension;
                    String s3Key = "tickets/" + comment.getTicket().getId() + "/comments/" + comment.getId() + "/" + fileName;

                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build();

                    s3Client.putObject(putObjectRequest, path);

                    String fileUrl = endpoint + "/v1/" + projectId + "/" + bucketName + "/" + fileName;
                    log.info("[파일 업로드] 파일 업로드 성공 - URL: {}", fileUrl);

                    Files.delete(path);

                    Attachment attachment = Attachment.builder()
                            .comment(comment)
                            .fileName(fileName)
                            .filePath(fileUrl)
                            .fileSize(file.getSize())
                            .build();

                    attachmentRepository.save(attachment);
                    attachments.add(attachment);

                } catch (IOException e) {
                    log.error("[파일 업로드] 파일 업로드 실패");
                    throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
                }
            });
        }
    }

    @Transactional
    public void deleteFile(Long attachmentId, CustomUserDetails currentUser) {
        log.info("[파일 삭제] 파일 삭제 요청 - attachmentId: {}", attachmentId);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(FileNotFoundException::new);

        if (currentUser.getUser().getRole() == Role.USER) {
            // 일반 사용자의 경우:
            if (attachment.getTicket() != null && attachment.getComment() == null) {
                // 티켓 첨부파일: 첨부파일의 티켓 요청자와 현재 사용자 일치 확인
                if (!attachment.getTicket().getRequester().getId().equals(currentUser.getId())) {
                    log.error("[파일 삭제] 권한 없음");
                    throw new CustomException(ErrorCode.UNAUTHORIZED_FILE_ACCESS);
                }
            } else if (attachment.getComment() != null) {
                // 댓글 첨부파일: 첨부파일의 댓글 작성자와 현재 사용자 일치 확인
                if (!attachment.getComment().getAuthor().getId().equals(currentUser.getId())) {
                    log.error("[파일 삭제] 권한 없음");
                    throw new CustomException(ErrorCode.UNAUTHORIZED_FILE_ACCESS);
                }
            }
        } else {
            // 담당자의 경우:
            // 티켓 첨부파일은 제한 없이 삭제 가능
            if (attachment.getComment() != null) {
                // 댓글 첨부파일: 담당자도 자신이 작성한 댓글 첨부파일만 삭제 가능
                if (!attachment.getComment().getAuthor().getId().equals(currentUser.getId())) {
                    log.error("[파일 삭제] 권한 없음");
                    throw new CustomException(ErrorCode.UNAUTHORIZED_FILE_ACCESS);
                }
            }
        }

        String s3Key = attachment.getFilePath();
        try {
            log.info("[파일 삭제] 파일 삭제");
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            log.error("[파일 삭제] 파일 삭제 실패");
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }

        attachmentRepository.delete(attachment);
    }
}
