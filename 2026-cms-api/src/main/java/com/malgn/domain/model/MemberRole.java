package com.malgn.domain.model;

import lombok.Getter;

public enum MemberRole {
    ADMIN("관리자"),
    MEMBER("회원");

    @Getter
    private final String name;

    MemberRole(String name) {
        this.name = name;
    }
}
