package com.lynx.auth_service.exception;

import java.util.Map;

public class ResourceAlreadyExistsException extends AuthException {

    private final Map<String, String> details;

    public ResourceAlreadyExistsException(Map<String, String> details) {
        super("One or more resources already exist.");
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
