package com.vass.authorization.service.exception;

public class InvalidPermissionException extends RuntimeException {

    public InvalidPermissionException(String permission) {
        super("Invalid permission " + permission);
    }
}
