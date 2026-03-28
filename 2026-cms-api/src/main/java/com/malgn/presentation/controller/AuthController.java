package com.malgn.presentation.controller;

import com.malgn.application.dto.TokenDto;
import com.malgn.application.service.AuthService;
import com.malgn.domain.exception.AuthException;
import com.malgn.domain.model.ErrorCode;
import com.malgn.presentation.api.AuthApi;
import com.malgn.presentation.dto.request.auth.SignInRequest;
import com.malgn.presentation.dto.request.auth.SignUpRequest;
import com.malgn.presentation.dto.response.ApiResponse;
import com.malgn.presentation.dto.response.auth.AuthTokenResponse;
import com.malgn.presentation.dto.response.auth.SignUpResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
    private final AuthService authService;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenExpiration;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @RequestBody
            @Valid
            SignUpRequest request) {
        return ResponseEntity.created(URI.create("/"))
                .body(ApiResponse.success(authService.signUp(request)));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> signIn(
            @RequestBody
            @Valid
            SignInRequest request,
            HttpServletResponse response) {

        TokenDto tokenDto = authService.signIn(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken")
                .value(tokenDto.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(AuthTokenResponse.from(tokenDto)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank())
            throw new AuthException(ErrorCode.TOKEN_NOT_FOUND);

        TokenDto tokenDto = authService.reissue(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken")
                .value(tokenDto.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(AuthTokenResponse.from(tokenDto)));
    }
}
