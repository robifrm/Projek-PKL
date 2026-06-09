package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.dto.auth.*;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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
 * Menguji semua alur autentikasi: register, login, verifyOtp,
 * forgotPassword, resetPassword dengan mock dependencies.
 */
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    private UserRepository    userRepository;
    private PasswordEncoder   passwordEncoder;
    private JwtProvider       jwtProvider;
    private OtpService        otpService;
    private EmailService      emailService;
    private AuthService       authService;

    @BeforeEach
    void setUp() {
        userRepository  = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtProvider     = new JwtProvider();
        otpService      = new OtpService(passwordEncoder);
        emailService    = mock(EmailService.class);

        ReflectionTestUtils.setField(jwtProvider, "jwtSecret",
                "test_secret_key_at_least_32_chars_long_for_hs256");
        ReflectionTestUtils.setField(jwtProvider, "tokenValidityInMilliseconds", 3_600_000L);
        ReflectionTestUtils.setField(otpService,  "expirationMinutes", 5L);

        authService = new AuthService(userRepository, passwordEncoder, jwtProvider, otpService, emailService);
    }

    // ─────────────────────────────────────────────
    // register()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("register() berhasil untuk user baru")
    void register_newUser_shouldSucceed() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest req = new RegisterRequest();
        req.setName("Budi");
        req.setUsername("budi123");
        req.setEmail("budi@vnet.id");
        req.setPhone("08123456789");
        req.setPassword("password123");

        AuthMessageResponse resp = authService.register(req);

        assertThat(resp.getMessage()).contains("OTP");
        assertThat(resp.getEmail()).isEqualTo("budi@vnet.id");
        assertThat(resp.getVerified()).isFalse();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() harus throw CONFLICT jika email sudah terverifikasi")
    void register_duplicateVerifiedEmail_shouldThrowConflict() {
        User existing = new User();
        existing.setEmail("ada@vnet.id");
        existing.setIsVerified(true);
        when(userRepository.findByEmailIgnoreCase("ada@vnet.id")).thenReturn(Optional.of(existing));

        RegisterRequest req = new RegisterRequest();
        req.setName("Budi"); req.setUsername("budi2"); req.setEmail("ada@vnet.id"); req.setPassword("pass123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("sudah terdaftar");
    }

    @Test
    @DisplayName("register() harus throw BAD_REQUEST jika password kurang dari 6 karakter")
    void register_shortPassword_shouldThrowBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("A"); req.setUsername("a"); req.setEmail("a@b.com"); req.setPassword("123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("register() harus throw BAD_REQUEST untuk email tanpa '@'")
    void register_invalidEmailFormat_shouldThrowBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("A"); req.setUsername("a"); req.setEmail("bukan-email"); req.setPassword("pass123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("register() harus throw BAD_REQUEST jika nama kosong")
    void register_emptyName_shouldThrowBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("  "); req.setUsername("budi"); req.setEmail("b@b.com"); req.setPassword("pass123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ─────────────────────────────────────────────
    // login()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("login() berhasil untuk credential yang benar")
    void login_correctCredentials_shouldReturnToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@vnet.id");
        user.setName("Admin");
        user.setUsername("admin");
        user.setRole(Role.ADMIN);
        user.setIsVerified(true);
        user.setPassword(passwordEncoder.encode("rahasia123"));

        when(userRepository.findByEmailIgnoreCase("admin@vnet.id")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@vnet.id");
        req.setPassword("rahasia123");

        AuthResponse resp = authService.login(req);

        assertThat(resp.getToken()).isNotBlank();
        assertThat(resp.getEmail()).isEqualTo("admin@vnet.id");
        assertThat(resp.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("login() harus throw UNAUTHORIZED jika password salah")
    void login_wrongPassword_shouldThrowUnauthorized() {
        User user = new User();
        user.setEmail("user@vnet.id");
        user.setIsVerified(true);
        user.setPassword(passwordEncoder.encode("benar"));
        user.setRole(Role.NOC);
        when(userRepository.findByEmailIgnoreCase("user@vnet.id")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@vnet.id");
        req.setPassword("salah");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(401);
    }

    @Test
    @DisplayName("login() harus throw FORBIDDEN jika akun belum diverifikasi")
    void login_unverifiedUser_shouldThrowForbidden() {
        User user = new User();
        user.setEmail("new@vnet.id");
        user.setIsVerified(false);
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setRole(Role.NOC);
        when(userRepository.findByEmailIgnoreCase("new@vnet.id")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("new@vnet.id");
        req.setPassword("pass123");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(403);
    }

    @Test
    @DisplayName("login() harus throw NOT_FOUND jika email tidak ditemukan")
    void login_unknownEmail_shouldThrowNotFound() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@vnet.id");
        req.setPassword("pass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(404);
    }

    // ─────────────────────────────────────────────
    // verifyOtp()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("verifyOtp() berhasil dengan OTP bypass 123456 untuk user yang belum terverifikasi")
    void verifyOtp_withBypassOtp_shouldVerifyUser() {
        User user = new User();
        user.setEmail("pending@vnet.id");
        user.setIsVerified(false);
        user.setRole(Role.NOC);
        when(userRepository.findByEmailIgnoreCase("pending@vnet.id")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("pending@vnet.id");
        req.setOtpCode("123456");

        AuthMessageResponse resp = authService.verifyOtp(req);

        assertThat(resp.getVerified()).isTrue();
        assertThat(user.getIsVerified()).isTrue();
    }

    @Test
    @DisplayName("verifyOtp() harus throw BAD_REQUEST jika OTP salah")
    void verifyOtp_withWrongOtp_shouldThrowBadRequest() {
        User user = new User();
        user.setEmail("user@vnet.id");
        user.setIsVerified(false);
        user.setRole(Role.NOC);
        // Set OTP tidak cocok (assign otp 111111 tetapi kirim 999999)
        ReflectionTestUtils.setField(otpService, "expirationMinutes", 5L);
        otpService.assignOtp(user, "111111");
        when(userRepository.findByEmailIgnoreCase("user@vnet.id")).thenReturn(Optional.of(user));

        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("user@vnet.id");
        req.setOtpCode("999999");

        assertThatThrownBy(() -> authService.verifyOtp(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(400);
    }

    // ─────────────────────────────────────────────
    // resetPassword()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("resetPassword() harus berhasil mengubah password dengan OTP bypass")
    void resetPassword_withBypassOtp_shouldChangePassword() {
        User user = new User();
        user.setEmail("reset@vnet.id");
        user.setIsVerified(true);
        user.setRole(Role.NOC);
        user.setPassword(passwordEncoder.encode("oldPass123"));
        when(userRepository.findByEmailIgnoreCase("reset@vnet.id")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("reset@vnet.id");
        req.setOtpCode("000000");
        req.setNewPassword("newPass456");

        AuthMessageResponse resp = authService.resetPassword(req);

        assertThat(resp.getMessage()).contains("berhasil");
        // Password harus berubah — tidak sama dengan lama
        assertThat(passwordEncoder.matches("newPass456", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("resetPassword() harus throw BAD_REQUEST jika password baru terlalu pendek")
    void resetPassword_shortNewPassword_shouldThrowBadRequest() {
        User user = new User();
        user.setEmail("u@v.id");
        user.setIsVerified(true);
        user.setRole(Role.NOC);
        when(userRepository.findByEmailIgnoreCase("u@v.id")).thenReturn(Optional.of(user));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("u@v.id");
        req.setOtpCode("000000");
        req.setNewPassword("123");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatusCode().value())
                .isEqualTo(400);
    }
}
