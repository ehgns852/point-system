package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class CartItemNotFountException extends BusinessException{

    public CartItemNotFountException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
