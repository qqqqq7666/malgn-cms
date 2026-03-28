package com.malgn.presentation.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @Schema(description = "로그인 아이디", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(max = 50, message = "아이디는 최대 100자까지 입력 가능합니다.")
        String username,
        @Schema(description = "비밀번호", example = "passw0rd", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(max = 255, message = "비밀번호는 최대 255자까지 입력 가능합니다.")
        String password
) {
}
