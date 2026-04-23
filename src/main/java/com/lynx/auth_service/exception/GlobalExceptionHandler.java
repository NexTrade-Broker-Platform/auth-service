package com.lynx.auth_service.exception;

import com.lynx.auth_service.dto.ErrorDetails;
import com.lynx.auth_service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> details = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );

        return new ErrorResponse(
                new ErrorDetails(
                        "VALIDATION_ERROR",
                        "The request payload failed validation.",
                        details
                )
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException ex) {
        return new ErrorResponse(
                new ErrorDetails(
                        "VALIDATION_ERROR",
                        ex.getMessage(),
                        ex.getDetails()
                )
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ResourceAlreadyExistsException ex) {
        return new ErrorResponse(
                new ErrorDetails(
                        "RESOURCE_EXISTS",
                        ex.getMessage(),
                        ex.getDetails()
                )
        );
    }
}