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

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token으로 Access Token을 갱신합니다.<br><br>" +
                    "**💡 Swagger 테스트 안내**<br>" +
                    "- Refresh Token은 보안을 위해 **HttpOnly Cookie**로 발급 및 관리됩니다.<br>" +
                    "- 브라우저 정책상 Swagger UI에서 HttpOnly 쿠키값을 직접 입력하거나 확인할 수 없습니다.<br>" +
                    "- **테스트 방법:** 먼저 `[로그인 API]`를 실행하여 브라우저에 쿠키를 정상적으로 세팅한 후, 본 API의 `Execute` 버튼을 눌러주세요.",
            operationId = "auth-03"
    )
    ResponseEntity<ApiResponse<AuthTokenResponse>> reissue(
            @Parameter(hidden = true) String refreshToken,
            @Parameter(hidden = true) HttpServletResponse response);
}
