package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class PointRatioSettingException extends BusinessException{

    public PointRatioSettingException(String message, HttpStatus status) {
        super(message, status);
    }
}
