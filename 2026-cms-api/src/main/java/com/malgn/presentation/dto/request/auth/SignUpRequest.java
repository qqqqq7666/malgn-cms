package com.malgn.presentation.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @Schema(description = "사용할 아이디", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(max = 50, message = "아이디는 50자를 초과할 수 없습니다.")
        String username,
        @Schema(description = "비밀번호", example = "passw0rd", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password,
        @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name
) {

}
