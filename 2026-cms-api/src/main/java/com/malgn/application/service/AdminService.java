package com.malgn.application.service;

import com.malgn.domain.exception.AuthException;
import com.malgn.domain.model.ErrorCode;
import com.malgn.domain.model.Member;
import com.malgn.domain.repository.MemberRepository;
import com.malgn.presentation.dto.request.member.MemberUpdateRoleRequest;
import com.malgn.presentation.dto.response.member.MemberUpdateRoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    @Transactional
    public MemberUpdateRoleResponse updateRole(Long id, MemberUpdateRoleRequest request) {
        Member targetMember = memberRepository.findById(id)
                .orElseThrow(() -> new AuthException(ErrorCode.MEMBER_NOT_FOUND));

        targetMember.updateRole(request.role());

        memberRepository.saveAndFlush(targetMember);

        return MemberUpdateRoleResponse.from(targetMember);
    }
}
