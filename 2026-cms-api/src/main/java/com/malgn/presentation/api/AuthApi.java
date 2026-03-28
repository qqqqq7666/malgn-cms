package com.malgn.presentation.api;

import com.malgn.presentation.dto.request.auth.SignInRequest;
import com.malgn.presentation.dto.request.auth.SignUpRequest;
import com.malgn.presentation.dto.response.ApiResponse;
import com.malgn.presentation.dto.response.auth.AuthTokenResponse;
import com.malgn.presentation.dto.response.auth.SignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "01. 회원 인증", description = "회원가입 및 인증 API")
public interface AuthApi {

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.", operationId = "auth-01")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request);

    @Operation(summary = "로그인", description = "ID/PW로 로그인하고 토큰을 발급받습니다.", operationId = "auth-02")
    ResponseEntity<ApiResponse<AuthTokenResponse>> signIn(
            @RequestBody SignInRequest request,
            @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 갱신합니다.", operationId = "auth-03")
    ResponseEntity<ApiResponse<AuthTokenResponse>> reissue(
            @Parameter(description = "리프레시 토큰", example = "eyJhbGci...") String refreshToken,
            @Parameter(hidden = true) HttpServletResponse response);
}
