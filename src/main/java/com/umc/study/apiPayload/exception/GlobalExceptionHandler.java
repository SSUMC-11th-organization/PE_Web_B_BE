package com.umc.study.apiPayload.exception;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.apiPayload.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스 검증 실패 (404 / 403 / 409)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustom(CustomException e) {
        return toResponse(e.getErrorCode());
    }

    // DTO 검증 실패 (@Valid) → 400, 첫 번째 오류 메시지를 돌려줍니다
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.INVALID_INPUT, message));
    }

    // JSON 문법 오류, score에 "abc" 같은 타입 불일치 → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.INVALID_JSON);
    }

    // /members/abc/ratings 처럼 경로 값이 숫자가 아닐 때 → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(ErrorCode.INVALID_PATH_VARIABLE);
    }

    /**
     * DB 제약 조건 위반 → 409
     * 서비스의 사전 중복 체크를 동시에 통과한 요청은 INSERT 시점에 UNIQUE 제약에 걸립니다.
     * MySQL 메시지("Duplicate entry '...' for key 'member.email'")의 키 이름으로
     * 어떤 제약이 깨졌는지 구분해, 사전 체크와 같은 code/message로 응답합니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        String rootMessage = e.getMostSpecificCause().getMessage();
        String key = extractConstraintKey(rootMessage);
        log.warn("DB 제약 조건 위반: key={}, message={}", key, rootMessage);

        ErrorCode errorCode;
        if (key.endsWith("email")) {
            errorCode = ErrorCode.DUPLICATE_EMAIL;
        } else if (key.endsWith("nickname")) {
            errorCode = ErrorCode.DUPLICATE_NICKNAME;
        } else if (key.endsWith("uk_rating_member_movie")) {
            errorCode = ErrorCode.DUPLICATE_RATING;
        } else {
            errorCode = ErrorCode.DUPLICATE_DATA;
        }
        return toResponse(errorCode);
    }

    /**
     * 그 밖의 예외.
     * ResponseStatusException, 없는 경로(NoResourceFoundException → 404),
     * 지원하지 않는 메서드(→ 405) 같은 Spring 표준 예외는 ErrorResponse를 구현하므로
     * 원래 상태 코드를 유지한 채 공통 형식으로 감쌉니다.
     * 그 외 예상하지 못한 오류만 500으로 응답합니다 (내부 메시지는 노출하지 않음).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            String message = errorResponse.getBody().getDetail();
            return ResponseEntity.status(status)
                    .body(new ApiResponse<>(false, "COMMON" + status, message, null));
        }
        log.error("처리되지 않은 예외", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.failure(errorCode));
    }

    // "... for key 'member.email'" → "member.email"
    private String extractConstraintKey(String message) {
        if (message == null) {
            return "";
        }
        String marker = "for key '";
        int start = message.lastIndexOf(marker);
        if (start < 0) {
            return "";
        }
        start += marker.length();
        int end = message.indexOf("'", start);
        return end < 0 ? "" : message.substring(start, end);
    }
}
