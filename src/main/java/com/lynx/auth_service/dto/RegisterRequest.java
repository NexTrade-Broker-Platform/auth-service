package com.lynx.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegisterRequest {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number"
    )
    private String password;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
}