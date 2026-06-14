package dev.gcanul.corebanking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTests {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inyectamos la clave manualmente ya que no estamos levantando el contexto de Spring
        String SECRET_KEY = "c3ByaW5nLWFwcGxpY2F0aW9uLXNlY3VyZV9rZXktbXVzdC1iZS1hdC1sZWFzdC0zMi1jaGFyYWN0ZXJz";
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
    }

    @Test
    @DisplayName("Should extract user name from Token successfully")
    void shouldExtractUsernameFromToken() {
        UserDetails userDetails = new User("admin", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("admin");
    }

    @DisplayName("Should validate token correctly")
    @Test
    void shouldValidateTokenCorrectly() {
        UserDetails userDetails = new User("admin", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }
}