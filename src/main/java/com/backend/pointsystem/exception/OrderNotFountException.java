package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFountException extends BusinessException{

    public OrderNotFountException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
