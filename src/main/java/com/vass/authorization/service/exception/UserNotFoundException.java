package com.vass.authorization.service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User not found for id " + userId);
    }
}
