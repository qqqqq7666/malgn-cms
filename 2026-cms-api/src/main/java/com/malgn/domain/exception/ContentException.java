package com.malgn.domain.exception;

import com.malgn.domain.model.ErrorCode;
import lombok.Getter;

@Getter
public class ContentException extends BusinessException {
    public ContentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ContentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
