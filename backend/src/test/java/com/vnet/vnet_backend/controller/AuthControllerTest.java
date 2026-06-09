package com.vnet.vnet_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.dto.auth.AuthMessageResponse;
import com.vnet.vnet_backend.dto.auth.AuthResponse;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.service.AuthService;
import com.vnet.vnet_backend.service.CaptchaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Functional / API Test — AuthController
 *
 * Menguji semua endpoint autentikasi menggunakan MockMvc:
 * register, login, verify-otp, forgot-password, reset-password.
 * Menggunakan Spring Boot 4 @MockitoBean (pengganti @MockBean yang dihapus di SB4).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Functional Tests")
class AuthControllerTest {

    @Autowired private MockMvc      mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean   private AuthService   authService;
    @MockitoBean   private CaptchaService captchaService;

    // ─────────────────────────────────────────────
    // Helper — buat AuthMessageResponse
    // ─────────────────────────────────────────────

    private AuthMessageResponse msgResp(String message, String email, Boolean verified) {
        return AuthMessageResponse.builder()
                .message(message).email(email)
                .expiresInMinutes(5L).verified(verified).build();
    }

    // ─────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register — berhasil mengembalikan 201 CREATED")
    void register_success_shouldReturn201() throws Exception {
        when(authService.register(any())).thenReturn(msgResp("OTP dikirim", "user@vnet.id", false));

        Map<String, String> body = Map.of(
            "name", "Budi", "username", "budi123",
            "email", "user@vnet.id", "password", "pass123"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@vnet.id"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register — email duplikat mengembalikan 409 CONFLICT")
    void register_duplicateEmail_shouldReturn409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar"));

        Map<String, String> body = Map.of(
            "name", "A", "username", "a1", "email", "ada@vnet.id", "password", "pass123"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    // ─────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login — berhasil mengembalikan 200 dengan token")
    void login_success_shouldReturn200WithToken() throws Exception {
        AuthResponse authResp = AuthResponse.builder()
                .id(1L).token("jwt.token.here")
                .email("admin@vnet.id").name("Admin").username("admin")
                .role(Role.ADMIN).isVerified(true).build();
        when(authService.login(any())).thenReturn(authResp);

        Map<String, String> body = Map.of("email", "admin@vnet.id", "password", "pass123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login — password salah mengembalikan 401")
    void login_wrongPassword_shouldReturn401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah"));

        Map<String, String> body = Map.of("email", "x@vnet.id", "password", "salah");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login — akun belum verifikasi mengembalikan 403")
    void login_unverifiedAccount_shouldReturn403() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Email belum diverifikasi"));

        Map<String, String> body = Map.of("email", "new@vnet.id", "password", "pass123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // POST /api/auth/verify-otp
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/verify-otp — OTP valid mengembalikan 200 verified=true")
    void verifyOtp_valid_shouldReturn200() throws Exception {
        when(authService.verifyOtp(any())).thenReturn(msgResp("Berhasil diverifikasi", "u@vnet.id", true));

        Map<String, String> body = Map.of("email", "u@vnet.id", "otpCode", "123456");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/verify-otp — OTP salah mengembalikan 400")
    void verifyOtp_invalid_shouldReturn400() throws Exception {
        when(authService.verifyOtp(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP tidak valid"));

        Map<String, String> body = Map.of("email", "u@vnet.id", "otpCode", "999999");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────
    // POST /api/auth/forgot-password
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/forgot-password — selalu mengembalikan 200 (tidak bocorkan apakah email ada)")
    void forgotPassword_shouldAlwaysReturn200() throws Exception {
        when(authService.forgotPassword(any()))
                .thenReturn(msgResp("Jika email terdaftar, OTP dikirim", "ghost@vnet.id", null));

        Map<String, String> body = Map.of("email", "ghost@vnet.id");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    // ─────────────────────────────────────────────
    // POST /api/auth/reset-password
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/reset-password — berhasil mengembalikan 200")
    void resetPassword_success_shouldReturn200() throws Exception {
        when(authService.resetPassword(any()))
                .thenReturn(msgResp("Password berhasil direset", "u@vnet.id", true));

        Map<String, String> body = Map.of(
            "email", "u@vnet.id", "otpCode", "000000", "newPassword", "newPass123"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password berhasil direset"));
    }

    // ─────────────────────────────────────────────
    // GET /api/auth/captcha
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/auth/captcha — harus mengembalikan 200 dengan captchaId dan question")
    void getCaptcha_shouldReturn200WithCaptchaData() throws Exception {
        when(captchaService.generateCaptcha())
                .thenReturn(Map.of("captchaId", "abc123", "question", "3 + 4 = ?"));

        mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.captchaId").exists())
                .andExpect(jsonPath("$.question").exists());
    }
}

