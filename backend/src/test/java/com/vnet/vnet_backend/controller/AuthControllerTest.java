package com.vnet.vnet_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.dto.auth.AuthResponse;
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
 * Menguji endpoint autentikasi login dan captcha menggunakan MockMvc.
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
    // POST /api/auth/login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login — berhasil mengembalikan 200 dengan token")
    void login_success_shouldReturn200WithToken() throws Exception {
        AuthResponse authResp = AuthResponse.builder()
                .id(1L).token("jwt.token.here")
                .email("admin@vnet.id").name("Admin").username("admin")
                .role(Role.SUPER_ADMIN).isVerified(true).build();
        when(authService.login(any(), any(), any())).thenReturn(authResp);

        Map<String, String> body = Map.of("username", "admin", "password", "pass123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login — password salah mengembalikan 401")
    void login_wrongPassword_shouldReturn401() throws Exception {
        when(authService.login(any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username atau password salah"));

        Map<String, String> body = Map.of("username", "admin", "password", "salah");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login — akun belum verifikasi mengembalikan 403")
    void login_unverifiedAccount_shouldReturn403() throws Exception {
        when(authService.login(any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Akun belum diverifikasi"));

        Map<String, String> body = Map.of("username", "new_user", "password", "pass123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
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

