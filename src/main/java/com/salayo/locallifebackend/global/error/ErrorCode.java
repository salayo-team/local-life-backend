package com.salayo.locallifebackend.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // 403 Forbidden
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근이 거부됐습니다."),

    // 404 NOT_FOUND
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),

    // 409 CONFLICT
    DUPLICATE_VALUE(HttpStatus.CONFLICT, "중복된 정보입니다."),

    // 429 TOO_MANY_REQUESTS
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과했습니다."),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "redis 서버에서 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }

}
