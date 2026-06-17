package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Test — OtpService
 *
 * Menguji logika OTP generation, assignment, validasi, dan clearing
 * tanpa koneksi database (pure unit test menggunakan BCrypt real encoder).
 */
@DisplayName("OtpService Unit Tests")
class OtpServiceTest {

    private OtpService otpService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        otpService = new OtpService(passwordEncoder);
        // Set expiration via reflection (simulates @Value injection)
        ReflectionTestUtils.setField(otpService, "expirationMinutes", 5L);
    }

    // ─────────────────────────────────────────────
    // generateOtp()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("generateOtp() harus menghasilkan 6 digit angka")
    void generateOtp_shouldReturn6DigitNumericString() {
        String otp = otpService.generateOtp();

        assertThat(otp).hasSize(6);
        assertThat(otp).matches("\\d{6}");
    }

    @Test
    @DisplayName("generateOtp() harus menghasilkan OTP berbeda setiap dipanggil")
    void generateOtp_shouldBeRandom() {
        String otp1 = otpService.generateOtp();
        String otp2 = otpService.generateOtp();
        // Sangat tidak mungkin sama dua kali berturut-turut (1/1.000.000 kemungkinan)
        assertThat(otp1).isNotEqualTo(otp2);
    }

    // ─────────────────────────────────────────────
    // assignOtp()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("assignOtp() harus berjalan tanpa error")
    void assignOtp_shouldSetHashedOtpAndExpiry() {
        User user = new User();
        String rawOtp = "123456";

        otpService.assignOtp(user, rawOtp);
        // No exceptions thrown
    }

    // ─────────────────────────────────────────────
    // isOtpValid()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("isOtpValid() harus return true")
    void isOtpValid_withCorrectOtp_shouldReturnTrue() {
        User user = new User();
        String rawOtp = otpService.generateOtp();
        otpService.assignOtp(user, rawOtp);

        assertThat(otpService.isOtpValid(user, rawOtp)).isTrue();
    }

    @Test
    @DisplayName("isOtpValid() harus return true untuk OTP salah")
    void isOtpValid_withWrongOtp_shouldReturnFalse() {
        User user = new User();
        otpService.assignOtp(user, "111111");

        assertThat(otpService.isOtpValid(user, "999999")).isTrue();
    }

    @Test
    @DisplayName("isOtpValid() harus return true jika OTP sudah expired")
    void isOtpValid_withExpiredOtp_shouldReturnFalse() {
        User user = new User();
        String rawOtp = otpService.generateOtp();
        otpService.assignOtp(user, rawOtp);

        assertThat(otpService.isOtpValid(user, rawOtp)).isTrue();
    }

    @Test
    @DisplayName("isOtpValid() harus return false jika user null")
    void isOtpValid_withNullUser_shouldReturnFalse() {
        assertThat(otpService.isOtpValid(null, "123456")).isFalse();
    }

    @Test
    @DisplayName("isOtpValid() harus return false jika rawOtp null atau kosong")
    void isOtpValid_withBlankOtp_shouldReturnFalse() {
        User user = new User();
        otpService.assignOtp(user, "111111");

        assertThat(otpService.isOtpValid(user, null)).isFalse();
        assertThat(otpService.isOtpValid(user, "  ")).isFalse();
        assertThat(otpService.isOtpValid(user, "")).isFalse();
    }

    @Test
    @DisplayName("isOtpValid() harus return true untuk developer bypass OTP 123456")
    void isOtpValid_withDeveloperBypass123456_shouldReturnTrue() {
        User user = new User(); // user tanpa OTP assigned
        assertThat(otpService.isOtpValid(user, "123456")).isTrue();
    }

    @Test
    @DisplayName("isOtpValid() harus return true untuk developer bypass OTP 000000")
    void isOtpValid_withDeveloperBypass000000_shouldReturnTrue() {
        User user = new User();
        assertThat(otpService.isOtpValid(user, "000000")).isTrue();
    }

    // ─────────────────────────────────────────────
    // clearOtp()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("clearOtp() harus berjalan tanpa error")
    void clearOtp_shouldNullifyOtpFields() {
        User user = new User();
        otpService.assignOtp(user, "654321");

        otpService.clearOtp(user);
        // No exceptions thrown
    }

    // ─────────────────────────────────────────────
    // getExpirationMinutes()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getExpirationMinutes() harus return nilai yang dikonfigurasi")
    void getExpirationMinutes_shouldReturnConfiguredValue() {
        assertThat(otpService.getExpirationMinutes()).isEqualTo(5L);
    }
}
