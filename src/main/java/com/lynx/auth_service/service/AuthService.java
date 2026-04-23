package com.lynx.auth_service.service;

import com.lynx.auth_service.dto.RegisterRequest;
import com.lynx.auth_service.entity.User;
import com.lynx.auth_service.exception.ResourceAlreadyExistsException;
import com.lynx.auth_service.exception.ValidationException;
import com.lynx.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.user.min-age}")
    private int minAge;

    public User register(RegisterRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        Map<String, String> conflictErrors = new HashMap<>();


        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            conflictErrors.put("email", "A user with this email already exists.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            conflictErrors.put("username", "A user with this username already exists.");
        }

        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();

        if (age < minAge) {
            validationErrors.put("date_of_birth", "User must be at least " + minAge + " years old.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }

        if (!conflictErrors.isEmpty()) {
            throw new ResourceAlreadyExistsException(conflictErrors);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());

        User savedUser = userRepository.save(user);
        return savedUser;
    }
}