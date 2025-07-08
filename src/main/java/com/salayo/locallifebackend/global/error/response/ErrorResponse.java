package com.salayo.locallifebackend.global.error.response;

import com.salayo.locallifebackend.global.error.ErrorCode;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    private final String error;
    private final String path;
    private final String method;
    private final LocalDateTime timestamp;

    public ErrorResponse(int status,
        String code,
        String message,
        String error,
        String path,
        String method) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.error = error;
        this.path = path;
        this.method = method;
        this.timestamp = LocalDateTime.now();
    }

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String path,
        String method) {
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                errorCode.getHttpStatus().name(),
                path,
                method
            ));
    }

    public static ErrorResponse of(ErrorCode errorCode, String path, String method) {
        return new ErrorResponse(
            errorCode.getHttpStatus().value(),
            errorCode.name(),
            errorCode.getMessage(),
            errorCode.getHttpStatus().name(),
            path,
            method
        );
    }

}
