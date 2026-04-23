package com.lynx.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorDetails {
    private String code;
    private String message;
    private Map<String, String> details;
}
