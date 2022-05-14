package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException{

    public UserNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
