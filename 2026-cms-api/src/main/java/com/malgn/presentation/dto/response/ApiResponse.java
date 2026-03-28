package com.malgn.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.malgn.domain.model.ErrorCode;

public record ApiResponse<T>(
        boolean success,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        ErrorResponse error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> error(int status, String errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(status, errorCode, message));
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        ));
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                message
        ));
    }

    public record ErrorResponse(int status, String errorCode, String message) {
    }
}
