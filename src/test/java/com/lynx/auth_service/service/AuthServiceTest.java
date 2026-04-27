package com.lynx.auth_service.service;

import com.lynx.auth_service.dto.RegisterRequest;
import com.lynx.auth_service.dto.UserUpdateRequest;
import com.lynx.auth_service.entity.User;
import com.lynx.auth_service.exception.ResourceAlreadyExistsException;
import com.lynx.auth_service.exception.UserNotFoundException;
import com.lynx.auth_service.exception.ValidationException;
import com.lynx.auth_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest createRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("test@test.com");
        r.setPassword("password1");
        r.setUsername("user1");
        r.setFirstName("Test");
        r.setLastName("User");
        r.setDateOfBirth(LocalDate.of(2000,1,1));
        return r;
    }

    @Test
    void shouldRegisterUser() {
        ReflectionTestUtils.setField(authService, "minAge", 18);

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        RegisterRequest req = createRequest();

        User user = authService.register(req);

        assertNotNull(user);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowWhenEmailExists() {
        ReflectionTestUtils.setField(authService, "minAge", 18);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(createRequest()));
    }

    @Test
    void shouldGetUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = authService.getUser(id);

        assertEquals(id, result.getId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.getUser(id));
    }

    @Test
    void shouldUpdateUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("newUser");

        User updated = authService.updateUser(id, req);

        assertEquals("newUser", updated.getUsername());
    }

    @Test
    void shouldDeleteUser() {
        UUID id = UUID.randomUUID();

        authService.deleteUser(id);

        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldThrowWhenUnderage() {
        ReflectionTestUtils.setField(authService, "minAge", 18);

        RegisterRequest req = createRequest();
        req.setDateOfBirth(LocalDate.now());

        assertThrows(ValidationException.class,
                () -> authService.register(req));
    }
}