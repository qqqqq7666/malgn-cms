package com.malgn.domain.exception;

import com.malgn.domain.model.ErrorCode;
import lombok.Getter;

@Getter
public class MemberException extends BusinessException {
    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
