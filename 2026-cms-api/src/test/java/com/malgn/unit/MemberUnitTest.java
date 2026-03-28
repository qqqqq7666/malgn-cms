package com.malgn.unit;

import com.malgn.application.service.AdminService;
import com.malgn.domain.exception.AuthException;
import com.malgn.domain.model.Member;
import com.malgn.domain.model.MemberRole;
import com.malgn.domain.repository.MemberRepository;
import com.malgn.presentation.dto.request.member.MemberUpdateRoleRequest;
import com.malgn.presentation.dto.response.member.MemberUpdateRoleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 및 Member 도메인 단위 테스트")
class MemberUnitTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminService adminService;

    private Member targetMember;

    @BeforeEach
    void setUp() {
        targetMember = Member.builder()
                .id(10L)
                .username("targetUser")
                .password("encodedPassword")
                .name("대상유저")
                .role(MemberRole.MEMBER)
                .build();
    }

    @Nested
    @DisplayName("권한 변경")
    class UpdateRole {

        @Test
        @DisplayName("성공 - MEMBER를 ADMIN으로 승격할 수 있다")
        void success_promoteToAdmin() {
            MemberUpdateRoleRequest request = new MemberUpdateRoleRequest(MemberRole.ADMIN);
            given(memberRepository.findById(10L)).willReturn(Optional.of(targetMember));
            given(memberRepository.saveAndFlush(any(Member.class))).willReturn(targetMember);

            MemberUpdateRoleResponse result = adminService.updateRole(10L, request);

            assertThat(result.id()).isEqualTo(10L);
            assertThat(result.username()).isEqualTo("targetUser");
            verify(memberRepository).saveAndFlush(any(Member.class));
        }

        @Test
        @DisplayName("성공 - ADMIN을 MEMBER로 강등할 수 있다")
        void success_demoteToMember() {
            Member adminTarget = Member.builder()
                    .id(11L)
                    .username("admin")
                    .password("encodedPassword")
                    .name("관리자유저")
                    .role(MemberRole.ADMIN)
                    .build();

            MemberUpdateRoleRequest request = new MemberUpdateRoleRequest(MemberRole.MEMBER);
            given(memberRepository.findById(11L)).willReturn(Optional.of(adminTarget));
            given(memberRepository.saveAndFlush(any(Member.class))).willReturn(adminTarget);

            MemberUpdateRoleResponse result = adminService.updateRole(11L, request);

            assertThat(result).isNotNull();
            verify(memberRepository).saveAndFlush(any(Member.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID로 역할 변경 시 AuthException 발생")
        void fail_memberNotFound() {
            MemberUpdateRoleRequest request = new MemberUpdateRoleRequest(MemberRole.ADMIN);
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.updateRole(999L, request))
                    .isInstanceOf(AuthException.class);
        }
    }
}