package com.lynx.auth_service.controller;

import com.lynx.auth_service.dto.*;
import com.lynx.auth_service.entity.User;
import com.lynx.auth_service.exception.AuthException;
import com.lynx.auth_service.exception.ForbiddenException;
import com.lynx.auth_service.service.AuthService;
import com.lynx.auth_service.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                user.isActive(),
                user.isAdmin()
        );

        RegisterResponse response = new RegisterResponse(
                "User registered successfully",
                userResponse
        );

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> walletRequest = new HashMap<>();
            walletRequest.put("userId", user.getId());
            walletRequest.put("email", user.getEmail());
            restTemplate.postForEntity(
                    "http://localhost:8081/funds/create-wallet",
                    walletRequest,
                    Void.class
            );
        } catch (Exception ex) {
            System.out.println("User registered, but wallet creation failed");
        }

        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        User user = authService.login(request.getEmail(), request.getPassword());

        String token = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // localhost
                .path("/")
                .maxAge(3600)
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
                user.isActive(),
                user.isAdmin()
        );

        LoginResponse response = new LoginResponse(
                "User logged in successfully",
                userResponse
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }


    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
       checkOwnership(id);
        return map(authService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        checkOwnership(id);
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
        checkOwnership(id);
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
                user.isActive(),
                user.isAdmin()
        );
    }

    private void checkOwnership(UUID id) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {

            throw new AuthException("Unauthorized");
        }

        String currentUserId = (String) auth.getPrincipal();

        if (!id.toString().equals(currentUserId)) {
            throw new ForbiddenException();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // localhost
                .path("/")
                .maxAge(0) // -> delete cookie
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AuthException("Unauthorized");
        }

        String userId = (String) auth.getPrincipal();

        return map(authService.getUser(UUID.fromString(userId)));
    }
}