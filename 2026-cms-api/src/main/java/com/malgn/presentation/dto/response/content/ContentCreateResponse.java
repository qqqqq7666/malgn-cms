package com.malgn.presentation.dto.response.content;

import com.malgn.domain.model.Content;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContentCreateResponse(
        Long id,
        LocalDateTime createdDate,
        String createdBy
) {
    public static ContentCreateResponse from(Content content) {
        return ContentCreateResponse.builder()
                .id(content.getId())
                .createdDate(content.getCreatedDate())
                .createdBy(content.getCreatedBy())
                .build();
    }
}
