package com.backend.pointsystem.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUserException extends BusinessException{

    public DuplicateUserException(String message) {

        super(message, HttpStatus.BAD_REQUEST);
    }

}
