package com.lynx.auth_service.exception;

import com.lynx.auth_service.dto.ErrorDetails;
import com.lynx.auth_service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFormat() {

        return new ErrorResponse(
                new ErrorDetails(
                        "VALIDATION_ERROR",
                        "The request payload failed validation.",
                        Map.of("request", "Malformed JSON or invalid field format.")
                )
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException ex) {
        return new ErrorResponse(
                new ErrorDetails(
                        "USER_NOT_FOUND",
                        ex.getMessage(),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException ex) {
        return new ErrorResponse(
                new ErrorDetails(
                        "FORBIDDEN",
                        ex.getMessage(),
                        Map.of()
                )
        );
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuth(AuthException ex) {
        return new ErrorResponse(
                new ErrorDetails(
                        "INVALID_CREDENTIALS",
                        ex.getMessage(),
                        Map.of()
                )
        );
    }


}