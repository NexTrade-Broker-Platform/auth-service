package com.lynx.auth_service.exception;

import java.util.Map;

public class ValidationException extends AuthException {

    private final Map<String, String> details;

    public ValidationException(Map<String, String> details) {
        super("The request payload failed validation.");
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}