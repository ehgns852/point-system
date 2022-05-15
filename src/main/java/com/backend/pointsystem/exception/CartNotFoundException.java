package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class CartNotFoundException extends BusinessException{

    public CartNotFoundException(String message) {

        super(message, HttpStatus.NOT_FOUND);
    }

}
