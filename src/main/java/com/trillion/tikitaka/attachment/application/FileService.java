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
import java.util.*;

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
    public void uploadFiles(List<MultipartFile> files, Ticket ticket) {
        {
            if (files.size() > 5) throw new CustomException(ErrorCode.TOO_MANY_FILES);

            List<Attachment> attachments = new ArrayList<>();

            files.forEach(file -> {
                if (file.getSize() > MAX_FILE_SIZE) throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
                    throw new CustomException(ErrorCode.INVALID_FILE_NAME);
                }

                String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
                }

                try {
                    Path path = Files.createTempFile(null, null);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                    String uuid = UUID.randomUUID().toString();
                    String fileName = "tickets/" + ticket.getId() + "/" + uuid + "." + extension;

                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build();

                    s3Client.putObject(putObjectRequest, path);

                    String fileUrl = endpoint + "/v1/" + projectId + "/" + bucketName + "/" + fileName;

                    Files.delete(path);

                    Attachment attachment = Attachment.builder()
                            .ticket(ticket)
                            .fileName(uuid + "." + extension)
                            .filePath(fileUrl)
                            .fileSize(file.getSize())
                            .build();

                    attachmentRepository.save(attachment);
                    attachments.add(attachment);

                } catch (IOException e) {
                    throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
                }
            });
        }
    }
}
