package com.example.dianping.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
