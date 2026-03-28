package com.malgn.infrastructure.security.jwt;

import com.malgn.domain.exception.AuthException;
import com.malgn.domain.model.ErrorCode;
import com.malgn.infrastructure.security.userdetails.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private final Key key;
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.expiration.access}")
    private Long accessTokenExpiration;
    @Value("${jwt.expiration.refresh}")
    private Long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            CustomUserDetailsService customUserDetailsService) {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.userDetailsService = customUserDetailsService;
    }

    public String createAccessToken(String username, String name, String role) {
        Long now = System.currentTimeMillis();

        return buildToken(username, name, role)
                .setExpiration(new Date(now + accessTokenExpiration))
                .compact();
    }

    public String createRefreshToken(String username, String name, String role) {
        Long now = System.currentTimeMillis();

        return buildToken(username, name, role)
                .setExpiration(new Date(now + refreshTokenExpiration))
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token)
                .getSubject();
    }

    public String getName(String token) {
        return parseClaims(token)
                .get("name", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token)
                .get("role", String.class);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();

        if (username == null || username.isBlank()) {
            throw new AuthException(ErrorCode.INVALID_TOKEN, "토큰에 사용자 정보가 없습니다.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public Long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000; // 초 단위로 변환해서 전달
    }

    public String extractToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer "))
            return authHeader.substring(7).trim();
        else {
            return null;
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("wrong jwt sign key", e);
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("expired token", e);
            throw new AuthException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("unsupported jwt token", e);
            throw new AuthException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("token not found", e);
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }
    }

    private JwtBuilder buildToken(String username, String name, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("name", name)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
