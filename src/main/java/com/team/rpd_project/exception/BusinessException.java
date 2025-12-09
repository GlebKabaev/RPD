package com.team.rpd_project.exception;

import lombok.Getter;

public class BusinessException extends RuntimeException {
    @Getter
    private final String field;
    public BusinessException(String field, String message) {
        super(message);
        this.field = field;
    }
}