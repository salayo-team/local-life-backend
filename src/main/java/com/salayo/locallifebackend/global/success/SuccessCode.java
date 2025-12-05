package com.salayo.locallifebackend.global.success;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode {

    CREATE_SUCCESS(HttpStatus.CREATED, "생성이 완료되었습니다."),
    FETCH_SUCCESS(HttpStatus.OK, "성공적으로 조회되었습니다."),
    UPDATE_SUCCESS(HttpStatus.OK, "성공적으로 수정되었습니다."),
    DELETE_SUCCESS(HttpStatus.OK, "성공적으로 삭제되었습니다."),

    FILE_UPLOAD_SUCCESS(HttpStatus.OK, "파일 업로드 성공"),

    EMAIL_SEND_SUCCESS(HttpStatus.OK, "이메일을 성공적으로 전송했습니다."),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK, "이메일 인증에 성공했습니다."),

    SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인이 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃이 완료되었습니다."),

    VERIFICATION_SUCCESS(HttpStatus.OK, "검증이 완료되었습니다."),
    REFUND_SUCCESS(HttpStatus.OK, "결제 환불이 완료되었습니다."),

    ONBOARDING_START_SUCCESS(HttpStatus.OK, "온보딩이 시작되었습니다."),
    SELECT_SUCCESS(HttpStatus.OK, "선택이 완료되었습니다."),
    ONBOARDING_COMPLETE_SUCCESS(HttpStatus.OK, "온보딩이 완료되었습니다."),

    RECOMMENDATION_SUCCESS(HttpStatus.OK,"추천이 완료되었습니다."),

    AI_APTITUDE_TEST_START_SUCCESS(HttpStatus.OK, "AI 적성 검사가 시작되었습니다."),
    SUBMIT_ANSWER_SUCCESS(HttpStatus.OK, "성공적으로 답변을 제출했습니다."),
    RESUME_SUCCESS(HttpStatus.OK, "이어하기가 성공적으로 처리되었습니다.")

    ;

    private final HttpStatus status;
    private final String message;

    SuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
