package com.salayo.locallifebackend.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    DUPLICATE_LOGIN_DETECTED(HttpStatus.BAD_REQUEST, "다른 기기에서 로그인되어 자동 로그아웃 되었습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기는 최대 10MB까지 가능합니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다"),
    INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    MISSING_REQUIRED_FILE(HttpStatus.BAD_REQUEST, "필수 파일이 누락되었습니다."),
    INVALID_FILE_PURPOSE_MAPPING(HttpStatus.BAD_REQUEST, "파일 개수와 파일 목적 개수가 일치하지 않습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었거나 잘못되었습니다."),
    INVALID_LOGIN(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_START_DATE(HttpStatus.BAD_REQUEST, "시작일 값이 잘못되었습니다."),
    INVALID_END_DATE(HttpStatus.BAD_REQUEST, "종료일 값이 잘못되었습니다."),
    INVALID_CAPACITY_RANGE(HttpStatus.BAD_REQUEST, "정원 값이 잘못되었습니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "가격 값이 잘못되었습니다."),


    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "잘못된 형식의 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    TOKEN_ILLEGAL(HttpStatus.UNAUTHORIZED, "토큰이 비어있거나 잘못되었습니다."),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "토큰 서명이 유효하지 않습니다."),

    // 403 Forbidden
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근이 거부됐습니다."),
    CREATOR_NOT_APPROVED(HttpStatus.FORBIDDEN, "관리자 승인 후 로그인 가능합니다."),
    LOCAL_CREATOR_NOT_APPROVED(HttpStatus.FORBIDDEN, "로컬 크리에이터로 승인되지 않은 유저입니다."),
    // 404 NOT_FOUND
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.NOT_FOUND, "인증 코드가 만료되었거나 존재하지 않습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다."),
    APTITUDE_NOT_FOUND(HttpStatus.NOT_FOUND, "적성을 찾을 수 없습니다." ),

    // 408 REQUEST_TIMEOUT
    AI_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."),

    // 409 CONFLICT
    DUPLICATE_VALUE(HttpStatus.CONFLICT, "중복된 정보입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),

    // 429 TOO_MANY_REQUESTS
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과했습니다."),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다."),
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    AI_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 처리 중 오류가 발생했습니다."),

    ;


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
