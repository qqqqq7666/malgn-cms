package com.malgn.presentation.dto.request.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ContentCreateRequest(
        @Schema(description = "콘텐츠 제목 (최대 100자)", example = "test title", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,
        @Schema(description = "콘텐츠 내용 (최대 255자)", example = "test description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "설명은 최대 255자까지 입력 가능합니다.")
        String description
) {
}
