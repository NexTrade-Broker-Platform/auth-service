package com.lynx.auth_service.controller;

import com.lynx.auth_service.dto.*;
import com.lynx.auth_service.entity.User;
import com.lynx.auth_service.exception.AuthException;
import com.lynx.auth_service.exception.ForbiddenException;
import com.lynx.auth_service.service.AuthService;
import com.lynx.auth_service.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    private void validateKey(String key){
        if (!Objects.equals(internalApiKey, key)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid secret API key");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestHeader("X-INTERNAL-KEY") String key,
            @Valid @RequestBody RegisterRequest request) {
        validateKey(key);

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
            restTemplate.postForEntity(
                    "http://wallet-service:8082/funds/create-wallet",
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
    public ResponseEntity<?> login(
            @RequestHeader("X-INTERNAL-KEY") String key,
            @Valid @RequestBody LoginRequest request) {
        validateKey(key);
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
            @RequestHeader("X-INTERNAL-KEY") String key,
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        validateKey(key);
        checkOwnership(id);
        return map(authService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    public UserResponse getUser(
            @RequestHeader("X-INTERNAL-KEY") String key,
            @PathVariable UUID id) {
        validateKey(key);
        checkOwnership(id);
        return map(authService.getUser(id));
    }

   @GetMapping
    public List<UserResponse> getAllUsers(@RequestHeader("X-INTERNAL-KEY") String key) {
        validateKey(key);
        return authService.getAllUsers()
                .stream()
                .map(this::map)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(
            @RequestHeader("X-INTERNAL-KEY") String key,
            @PathVariable UUID id) {
        validateKey(key);
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
    public ResponseEntity<?> logout(@RequestHeader("X-INTERNAL-KEY") String key) {
        validateKey(key);
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
    public UserResponse getCurrentUser(@RequestHeader("X-INTERNAL-KEY") String key) {
        validateKey(key);
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AuthException("Unauthorized");
        }

        String userId = (String) auth.getPrincipal();

        return map(authService.getUser(UUID.fromString(userId)));
    }
}