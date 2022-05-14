package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class LockOfMoneyException extends BusinessException{
    public LockOfMoneyException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
