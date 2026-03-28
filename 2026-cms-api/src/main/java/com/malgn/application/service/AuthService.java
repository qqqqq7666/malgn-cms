package com.malgn.application.service;

import com.malgn.application.dto.TokenDto;
import com.malgn.domain.exception.MemberException;
import com.malgn.domain.model.ErrorCode;
import com.malgn.domain.model.Member;
import com.malgn.domain.model.MemberRole;
import com.malgn.domain.repository.MemberRepository;
import com.malgn.infrastructure.security.jwt.JwtProvider;
import com.malgn.infrastructure.security.userdetails.CustomUserDetails;
import com.malgn.presentation.dto.request.auth.SignInRequest;
import com.malgn.presentation.dto.request.auth.SignUpRequest;
import com.malgn.presentation.dto.response.auth.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByUsername(request.username()))
            throw new MemberException(ErrorCode.MEMBER_DUPLICATE);

        Member member = Member.createMember(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.name()
        );

        return SignUpResponse.from(memberRepository.save(member));
    }

    public TokenDto signIn(SignInRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        return createToken(authentication);
    }

    public TokenDto reissue(String refreshToken) {
        jwtProvider.validateToken(refreshToken);

        Authentication authentication = jwtProvider.getAuthentication(refreshToken);

        return createToken(authentication);
    }

    private TokenDto createToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = userDetails.getUsername();
        String name = userDetails.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(MemberRole.MEMBER.name());

        String accessToken = jwtProvider.createAccessToken(username, name, role);
        String refreshToken = jwtProvider.createRefreshToken(username, name, role);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiration(jwtProvider.getAccessTokenExpirationSeconds())
                .build();
    }
}
