package com.salayo.locallifebackend.global.dto;

import com.salayo.locallifebackend.global.success.SuccessCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class CommonResponseDto<T> {

    private final int status;
    private final String message;
    private final T data;

    public static <T> CommonResponseDto<T> success(SuccessCode successCode, T data) {
        return CommonResponseDto.<T>builder()
            .status(successCode.getStatus().value())
            .message(successCode.getMessage())
            .data(data)
            .build();
    }

    public static <T> CommonResponseDto<T> error(HttpStatus status, String message) {
        return CommonResponseDto.<T>builder()
            .status(status.value())
            .message(message)
            .data(null)
            .build();
    }

}
