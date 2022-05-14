package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughStockException extends BusinessException{
    public NotEnoughStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
