package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends BusinessException{

    public ItemNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
