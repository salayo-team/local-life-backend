package com.salayo.locallifebackend.global.success;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode {

    CREATE_SUCCESS(HttpStatus.CREATED, "생성이 완료되었습니다."),
    FETCH_SUCCESS(HttpStatus.OK, "성공적으로 조회되었습니다."),
    UPDATE_SUCCESS(HttpStatus.OK, "성공적으로 수정되었습니다."),
    DELETE_SUCCESS(HttpStatus.OK, "성공적으로 삭제되었습니다."),

    FILE_UPLOAD_SUCCESS(HttpStatus.OK, "파일 업로드 성공");


    private final HttpStatus status;
    private final String message;

    SuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
