package com.malgn.presentation.dto.response.content;

import com.malgn.domain.model.Content;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContentUpdateResponse(
        Long id,
        LocalDateTime lastModifiedDate,
        String lastModifiedBy
) {
    public static ContentUpdateResponse from(Content content) {
        return ContentUpdateResponse.builder()
                .id(content.getId())
                .lastModifiedDate(content.getLastModifiedDate())
                .lastModifiedBy(content.getLastModifiedBy())
                .build();
    }
}
