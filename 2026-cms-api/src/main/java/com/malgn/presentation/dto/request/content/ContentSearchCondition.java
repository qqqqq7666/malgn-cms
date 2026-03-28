package com.malgn.presentation.dto.request.content;

import io.swagger.v3.oas.annotations.media.Schema;

public record ContentSearchCondition(
        @Schema(description = "콘텐츠 제목", example = "게시물", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String title,
        @Schema(description = "작성자 이름", example = "user1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String createdBy
) {
}
