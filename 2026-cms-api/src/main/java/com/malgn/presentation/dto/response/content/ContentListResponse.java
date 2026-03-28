package com.malgn.presentation.dto.response.content;

import com.malgn.domain.model.Content;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContentListResponse(
        Long id,
        String title,
        Long viewCount,
        LocalDateTime createdDate,
        String createdBy
) {
    public static ContentListResponse from(Content content) {
        return ContentListResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .viewCount(content.getViewCount())
                .createdDate(content.getCreatedDate())
                .createdBy(content.getCreatedBy())
                .build();
    }
}
