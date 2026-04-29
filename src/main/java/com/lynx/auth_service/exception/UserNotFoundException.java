package com.lynx.auth_service.exception;

public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("User not found");
    }
}