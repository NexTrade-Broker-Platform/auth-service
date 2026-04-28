package com.lynx.auth_service.service;

import com.lynx.auth_service.dto.RegisterRequest;
import com.lynx.auth_service.dto.UserUpdateRequest;
import com.lynx.auth_service.entity.User;
import com.lynx.auth_service.exception.AuthException;
import com.lynx.auth_service.exception.ResourceAlreadyExistsException;
import com.lynx.auth_service.exception.UserNotFoundException;
import com.lynx.auth_service.exception.ValidationException;
import com.lynx.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        int age = Period.between(
                request.getDateOfBirth(),
                LocalDate.now()
        ).getYears();

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
        user.setDateOfBirth(request.getDateOfBirth().atStartOfDay());

        User savedUser = userRepository.save(user);
        return savedUser;
    }


    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid email or password");
        }

        return user;
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    // READ ALL
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = getUser(id);

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth().atStartOfDay());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        return userRepository.save(user);
    }

    // DELETE
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}