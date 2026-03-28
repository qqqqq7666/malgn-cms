package com.malgn.application.dto;

import lombok.Builder;

@Builder
public record TokenDto(
        String accessToken,
        String refreshToken,
        Long accessTokenExpiration
) {
}
