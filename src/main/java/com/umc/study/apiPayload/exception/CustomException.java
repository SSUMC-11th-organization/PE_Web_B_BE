package com.umc.study.apiPayload.exception;

import com.umc.study.apiPayload.code.ErrorCode;
import lombok.Getter;

/** 서비스에서 업무 규칙 위반을 알릴 때 던지는 예외 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
