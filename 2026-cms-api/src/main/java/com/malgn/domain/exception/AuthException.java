package com.malgn.domain.exception;

import com.malgn.domain.model.ErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends BusinessException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
