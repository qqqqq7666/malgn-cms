package com.malgn.presentation.dto.response.member;

import com.malgn.domain.model.Member;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MemberUpdateRoleResponse(
        Long id,
        String username,
        LocalDateTime lastModifiedDate,
        String lastModifiedBy
) {
    public static MemberUpdateRoleResponse from(Member member) {
        return MemberUpdateRoleResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .lastModifiedDate(member.getLastModifiedDate())
                .lastModifiedBy(member.getLastModifiedBy())
                .build();
    }
}
