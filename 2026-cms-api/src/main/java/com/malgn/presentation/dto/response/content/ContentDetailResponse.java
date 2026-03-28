package com.malgn.presentation.dto.response.content;

import com.malgn.domain.model.Content;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContentDetailResponse(
        Long id,
        String title,
        String description,
        Long viewCount,
        LocalDateTime createdDate,
        String createdBy,
        LocalDateTime lastModifiedDate,
        String lastModifiedBy
) {
    public static ContentDetailResponse from(Content content) {
        return ContentDetailResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .viewCount(content.getViewCount())
                .createdDate(content.getCreatedDate())
                .createdBy(content.getCreatedBy())
                .lastModifiedDate(content.getLastModifiedDate())
                .lastModifiedBy(content.getLastModifiedBy())
                .build();
    }
}
