package com.lynx.auth_service.controller;

import com.lynx.auth_service.dto.RegisterRequest;
import com.lynx.auth_service.dto.RegisterResponse;
import com.lynx.auth_service.dto.UserResponse;
import com.lynx.auth_service.dto.UserUpdateRequest;
import com.lynx.auth_service.entity.User;
import com.lynx.auth_service.service.AuthService;
import com.lynx.auth_service.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);

        String token = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                //.secure(true)
                .secure(false) // this is false to work on localhost, but in prod it should be set to true
                .path("/")
                .maxAge(3600) // 1 hour
                .sameSite("Strict")
                .build();

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getCreatedAt(),
                user.isActive()
        );

        RegisterResponse response = new RegisterResponse(
                "User registered successfully",
                userResponse
        );

        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        /*String currentUserId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!id.toString().equals(currentUserId)) {
            throw new RuntimeException("Forbidden");
        }*/ // -> when login will exist, this is a security check

        return map(authService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return map(authService.getUser(id));
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return authService.getAllUsers()
                .stream()
                .map(this::map)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {

        /*String currentUserId = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!id.toString().equals(currentUserId)) {
            throw new RuntimeException("Forbidden");
        }*/ //when login will exist, this is a security check

        authService.deleteUser(id);
    }

    private UserResponse map(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getCreatedAt(),
                user.isActive()
        );
    }
}