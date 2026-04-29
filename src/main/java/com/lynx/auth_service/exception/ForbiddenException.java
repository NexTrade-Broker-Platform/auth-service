package com.lynx.auth_service.exception;

public class ForbiddenException extends AuthException {

    public ForbiddenException() {
        super("Access denied");
    }
}