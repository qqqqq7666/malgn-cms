package com.malgn.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Content
    CONTENT_NOT_FOUND(404, "C001", "콘텐츠가 존재하지 않습니다."),

    // Member
    MEMBER_NOT_FOUND(404, "M001", "존재하지 않는 사용자입니다."),
    MEMBER_DUPLICATE(409, "M002", "이미 존재하는 회원 아이디입니다."),

    // Auth
    ACCESS_DENIED(403, "A001", "해당 요청에 대한 권한이 없습니다."),
    INVALID_TOKEN(401, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A003", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(401, "A004", "지원되지 않는 토큰 형식입니다."),
    TOKEN_SIGNATURE_ERROR(401, "A005", "토큰이 서명이 올바르지 않습니다."),
    TOKEN_NOT_FOUND(401, "A006", "토큰이 존재하지 않습니다."),
    INVALID_CREDENTIALS(401, "A007", "아이디 또는 비밀번호가 일치하지 않습니다"),

    // Common
    INTERNAL_SERVER_ERROR(500, "S001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(400, "S002", "잘못된 입력값입니다."),
    NOT_FOUND(404, "S003", "요청에 대한 데이터가 존재하지 않습니다.");;

    private final int status;
    private final String code;
    private final String message;
}
