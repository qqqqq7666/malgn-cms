package com.malgn.presentation.dto.request.member;

import com.malgn.domain.model.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberUpdateRoleRequest(
        @Schema(description = "변경할 권한 등급", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "변경할 권한을 선택해주세요.")
        MemberRole role
) {
}
