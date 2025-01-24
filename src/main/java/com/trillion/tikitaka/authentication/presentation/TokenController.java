package com.trillion.tikitaka.authentication.presentation;

import com.trillion.tikitaka.authentication.application.JwtService;
import com.trillion.tikitaka.authentication.application.util.JwtUtil;
import com.trillion.tikitaka.authentication.dto.response.TokenResponse;
import com.trillion.tikitaka.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.trillion.tikitaka.authentication.application.util.JwtUtil.*;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;
    private final JwtUtil jwtUtil;

    @PostMapping("/reissue")
    public ApiResponse<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        TokenResponse newTokens = jwtService.reissueTokens(request);

        response.setHeader(TOKEN_HEADER, TOKEN_PREFIX + newTokens.getAccessToken());
        response.addCookie(jwtUtil.createCookie(TOKEN_TYPE_REFRESH, newTokens.getRefreshToken()));
        return new ApiResponse<>("토큰이 재발급 되었습니다.", null);
    }
}
