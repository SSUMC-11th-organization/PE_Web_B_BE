package com.umc.study.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 실패 응답에 쓰는 code / message / HTTP 상태를 한곳에서 관리합니다.
 * code 규칙: {도메인}{HTTP 상태}_{일련번호} (예: AUTH401_1, RATING403_1)
 * → 코드만 보고도 "어느 도메인에서, 몇 번 상태가, 몇 번째 사유로" 났는지 알 수 있습니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON4001", "잘못된 입력입니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON4002", "요청 본문(JSON)을 읽을 수 없습니다."),
    INVALID_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "COMMON4003", "경로 값의 형식이 올바르지 않습니다."),
    DUPLICATE_DATA(HttpStatus.CONFLICT, "COMMON4091", "이미 존재하는 데이터입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다."),

    // [7주차] 인증
    // 이메일이 없을 때와 비밀번호가 틀릴 때를 같은 코드로 응답해 가입 여부를 숨깁니다.
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_1", "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH401_2", "로그인이 필요한 요청입니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401_3", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401_4", "토큰이 만료되었습니다. 다시 로그인해주세요."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "해당 회원을 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER409_1", "이미 사용 중인 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER409_2", "이미 사용 중인 이메일입니다."),

    // 평점
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "RATING404_1", "해당 평점을 찾을 수 없습니다."),
    RATING_FORBIDDEN(HttpStatus.FORBIDDEN, "RATING403_1", "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다."),
    DUPLICATE_RATING(HttpStatus.CONFLICT, "RATING409_1", "이미 이 영화에 평점을 남기셨습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
