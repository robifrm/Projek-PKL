package com.vnet.vnet_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.service.EmailService;
import com.vnet.vnet_backend.service.OtpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test — AuthFlowIntegrationTest
 *
 * Menguji alur autentikasi lengkap secara end-to-end dengan H2 database:
 * Register → Mendapatkan OTP → Verifikasi OTP → Login.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication End-to-End Flow Integration Tests")
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UserRepository userRepository;
    @Autowired private OtpService otpService;
    @Autowired private PasswordEncoder passwordEncoder;

    // Mock EmailService agar tidak memanggil real API Resend yang akan error dengan dummy key
    @MockitoBean private EmailService emailService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        doNothing().when(emailService).sendOtpEmail(any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Alur Otorisasi: Register diblokir -> Seed User Manual -> Login Sukses")
    void disabledRegisterAndSuccessfulLogin() throws Exception {
        // 1. REGISTER HARUS DIBLOKIR (403)
        Map<String, String> regBody = Map.of(
            "name", "Zayn Malik",
            "username", "zayn123",
            "email", "zayn@vnet.id",
            "password", "zaynpass"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regBody)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Fitur registrasi dinonaktifkan"));

        // 2. SEED USER MANUAL
        User user = User.builder()
                .name("Zayn Malik")
                .username("zayn123")
                .password(passwordEncoder.encode("zaynpass"))
                .role(Role.STAFF)
                .build();
        userRepository.save(user);

        // 3. LOGIN HARUS SUKSES
        Map<String, String> loginBody = Map.of(
            "email", "zayn123",
            "password", "zaynpass"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("zayn123"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }
}
