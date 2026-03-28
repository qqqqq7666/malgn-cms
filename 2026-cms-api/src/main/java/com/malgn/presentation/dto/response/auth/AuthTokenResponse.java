package com.malgn.presentation.dto.response.auth;

import com.malgn.application.dto.TokenDto;
import lombok.Builder;

@Builder
public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long accessTokenExpiration
) {
    public static AuthTokenResponse from(TokenDto tokenDto) {
        return AuthTokenResponse.builder()
                .accessToken(tokenDto.accessToken())
                .tokenType("Bearer")
                .accessTokenExpiration(tokenDto.accessTokenExpiration())
                .build();
    }
}
