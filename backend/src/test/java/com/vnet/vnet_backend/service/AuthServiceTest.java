package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.dto.auth.*;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test — AuthService
 *
 * Menguji alur login dengan mock dependencies.
 */
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    private UserRepository    userRepository;
    private PasswordEncoder   passwordEncoder;
    private JwtProvider       jwtProvider;
    private AuthService       authService;

    @BeforeEach
    void setUp() {
        userRepository  = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtProvider     = new JwtProvider();

        ReflectionTestUtils.setField(jwtProvider, "jwtSecret",
                "test_secret_key_at_least_32_chars_long_for_hs256");
        ReflectionTestUtils.setField(jwtProvider, "tokenValidityInMilliseconds", 3_600_000L);

        UserSessionService userSessionService = mock(UserSessionService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtProvider, userSessionService);
    }

    // ─────────────────────────────────────────────
    // login()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("login() berhasil untuk credential yang benar")
    void login_correctCredentials_shouldReturnToken() {
        User user = new User();
        user.setId(1L);
        user.setName("Admin");
        user.setUsername("admin");
        user.setRole(Role.SUPER_ADMIN);
        user.setPassword(passwordEncoder.encode("rahasia123"));

        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("rahasia123");

        AuthResponse resp = authService.login(req);

        assertThat(resp.getToken()).isNotBlank();
        assertThat(resp.getUsername()).isEqualTo("admin");
        assertThat(resp.getRole()).isEqualTo(Role.SUPER_ADMIN);
    }

    @Test
    @DisplayName("login() harus throw UNAUTHORIZED jika password salah")
    void login_wrongPassword_shouldThrowUnauthorized() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("benar"));
        user.setRole(Role.STAFF);
        when(userRepository.findByUsernameIgnoreCase("user")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setUsername("user");
        req.setPassword("salah");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(401);
    }

    @Test
    @DisplayName("login() harus throw NOT_FOUND jika username tidak ditemukan")
    void login_unknownEmail_shouldThrowNotFound() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setUsername("ghost");
        req.setPassword("pass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(404);
    }
}
