package com.salayo.locallifebackend.global.error.exception;

import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest httpServletRequest) {
        return ErrorResponse.toResponseEntity(
            e.getErrorCode(),
            httpServletRequest.getRequestURI(),
            httpServletRequest.getMethod()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest httpServletRequest) {
        String uri = httpServletRequest.getRequestURI();

        if (uri != null && uri.startsWith("/actuator")) {
            throw new RuntimeException(e);
        }

        return ErrorResponse.toResponseEntity(
            ErrorCode.INTERNAL_SERVER_ERROR,
            uri,
            httpServletRequest.getMethod()
        );
    }

}
