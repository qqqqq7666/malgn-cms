package com.malgn.presentation.dto.response.auth;

import com.malgn.domain.model.Member;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SignUpResponse(
        Long id,
        String username,
        LocalDateTime createdDate
) {
    public static SignUpResponse from(Member member) {
        return SignUpResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .createdDate(member.getCreatedDate())
                .build();
    }
}
