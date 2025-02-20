package com.trillion.tikitaka.attachment.presentation;

import com.trillion.tikitaka.attachment.application.FileService;
import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Void> deleteFile(@PathVariable("attachmentId") Long attachmentId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        fileService.deleteFile(attachmentId, userDetails);
        return new ApiResponse<>("파일이 업로드 되었습니다.", null);
    }
}
