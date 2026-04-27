package com.lynx.auth_service.repository;

import com.lynx.auth_service.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setUsername("user");
        user.setFirstName("A");
        user.setLastName("B");
        user.setDateOfBirth(LocalDateTime.now());

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
    }

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindByEmail() {
        User user = new User();
        user.setEmail("test1@test.com");
        user.setPassword("pass");
        user.setUsername("user");
        user.setFirstName("A");
        user.setLastName("B");
        user.setDateOfBirth(LocalDateTime.now());

        userRepository.save(user);

        assertTrue(userRepository.findByEmail("test1@test.com").isPresent());
    }

    @Test
    void shouldFindByUsername() {
        User user = new User();
        user.setEmail("test2@test.com");
        user.setPassword("pass");
        user.setUsername("user2");
        user.setFirstName("A");
        user.setLastName("B");
        user.setDateOfBirth(LocalDateTime.now());

        userRepository.save(user);

        assertTrue(userRepository.findByUsername("user2").isPresent());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        assertTrue(userRepository.findByEmail("nope@test.com").isEmpty());
    }
}