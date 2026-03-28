package com.malgn.unit;

import com.malgn.application.dto.TokenDto;
import com.malgn.application.service.AuthService;
import com.malgn.domain.exception.MemberException;
import com.malgn.domain.model.Member;
import com.malgn.domain.model.MemberRole;
import com.malgn.domain.repository.MemberRepository;
import com.malgn.infrastructure.security.jwt.JwtProvider;
import com.malgn.infrastructure.security.userdetails.CustomUserDetails;
import com.malgn.presentation.dto.request.auth.SignInRequest;
import com.malgn.presentation.dto.request.auth.SignUpRequest;
import com.malgn.presentation.dto.response.auth.SignUpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
public class AuthUnitTest {
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private Member savedMember;

    @BeforeEach
    void setUp() {
        savedMember = Member.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPassword")
                .name("테스트유저")
                .role(MemberRole.MEMBER)
                .build();
    }

    @Nested
    @DisplayName("회원가입")
    class SignUp {

        private SignUpRequest request;

        @BeforeEach
        void setUp() {
            request = new SignUpRequest("testUser", "passw0rd", "테스트유저");
        }

        @Test
        @DisplayName("성공 - 정상적으로 회원가입이 완료된다")
        void success() {
            given(memberRepository.existsByUsername("testUser")).willReturn(false);
            given(passwordEncoder.encode("passw0rd")).willReturn("encoded_password");
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            SignUpResponse result = authService.signUp(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.username()).isEqualTo("testUser");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("성공 - 저장 시 비밀번호는 인코딩된 값으로 저장된다")
        void success_passwordIsEncoded() {
            given(memberRepository.existsByUsername("testUser")).willReturn(false);
            given(passwordEncoder.encode("passw0rd")).willReturn("encoded_password");
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            authService.signUp(request);

            verify(passwordEncoder).encode("passw0rd");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 중복 아이디로 가입 시 MemberException 발생")
        void fail_duplicateUsername() {
            given(memberRepository.existsByUsername("testUser")).willReturn(true);

            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(MemberException.class);

            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 중복 시 패스워드 인코딩이 호출되지 않는다")
        void fail_noEncodeOnDuplicate() {
            given(memberRepository.existsByUsername("testUser")).willReturn(true);

            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(MemberException.class);

            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("로그인")
    class SignIn {

        private SignInRequest request;
        private Authentication authentication;

        @BeforeEach
        void setUp() {
            request = new SignInRequest("testUser", "passw0rd");

            CustomUserDetails userDetails = new CustomUserDetails(savedMember);
            authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null,
                    List.of(new SimpleGrantedAuthority(MemberRole.MEMBER.name()))
            );
        }

        @Test
        @DisplayName("성공 - 로그인 시 accessToken과 refreshToken이 발급된다")
        void success() {
            given(authenticationManager.authenticate(any()))
                    .willReturn(authentication);
            given(jwtProvider.createAccessToken(anyString(), anyString(), anyString()))
                    .willReturn("accessToken");
            given(jwtProvider.createRefreshToken(anyString(), anyString(), anyString()))
                    .willReturn("refreshToken");
            given(jwtProvider.getAccessTokenExpirationSeconds())
                    .willReturn(3600L);

            TokenDto result = authService.signIn(request);

            assertThat(result.accessToken()).isEqualTo("accessToken");
            assertThat(result.refreshToken()).isEqualTo("refreshToken");
            assertThat(result.accessTokenExpiration()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호로 로그인 시 BadCredentialsException 발생")
        void fail_wrongPassword() {
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("bad credentials"));

            assertThatThrownBy(() -> authService.signIn(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(jwtProvider, never()).createAccessToken(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        private Authentication authentication;

        @BeforeEach
        void setUp() {
            CustomUserDetails userDetails = new CustomUserDetails(savedMember);
            authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null,
                    List.of(new SimpleGrantedAuthority(MemberRole.MEMBER.name()))
            );
        }

        @Test
        @DisplayName("성공 - 유효한 refresh token으로 새 토큰이 발급된다")
        void success() {
            given(jwtProvider.getAuthentication("validRefreshToken")).willReturn(authentication);
            given(jwtProvider.createAccessToken(anyString(), anyString(), anyString()))
                    .willReturn("newAccessToken");
            given(jwtProvider.createRefreshToken(anyString(), anyString(), anyString()))
                    .willReturn("newRefreshToken");
            given(jwtProvider.getAccessTokenExpirationSeconds()).willReturn(3600L);

            TokenDto result = authService.reissue("validRefreshToken");

            assertThat(result.accessToken()).isEqualTo("newAccessToken");
            assertThat(result.refreshToken()).isEqualTo("newRefreshToken");
            verify(jwtProvider).validateToken("validRefreshToken");
        }
    }
}
