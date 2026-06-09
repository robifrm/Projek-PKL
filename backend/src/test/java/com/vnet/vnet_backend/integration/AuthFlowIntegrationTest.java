package com.vnet.vnet_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.service.EmailService;
import com.vnet.vnet_backend.service.OtpService;
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

    // Mock EmailService agar tidak memanggil real API Resend yang akan error dengan dummy key
    @MockitoBean private EmailService emailService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        doNothing().when(emailService).sendOtpEmail(any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Alur Lengkap: Register -> Verify OTP -> Login")
    void fullAuthFlow_shouldRegisterVerifyAndLogin() throws Exception {
        // 1. REGISTER
        Map<String, String> regBody = Map.of(
            "name", "Zayn Malik",
            "username", "zayn123",
            "email", "zayn@vnet.id",
            "password", "zaynpass"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("zayn@vnet.id"))
                .andExpect(jsonPath("$.verified").value(false));

        // Verifikasi user tersimpan di database tapi belum verified
        Optional<User> optUser = userRepository.findByEmailIgnoreCase("zayn@vnet.id");
        assertThat(optUser).isPresent();
        User user = optUser.get();
        assertThat(user.getIsVerified()).isFalse();

        // 2. DAPATKAN OTP (karena EmailService di-mock, kita generate & assign manual)
        String otpCode = otpService.generateOtp();
        otpService.assignOtp(user, otpCode);
        userRepository.save(user);
        assertThat(otpCode).isNotNull();

        // 3. VERIFY OTP
        Map<String, String> verifyBody = Map.of(
            "email", "zayn@vnet.id",
            "otpCode", otpCode
        );

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        // User sekarang harusnya verified
        user = userRepository.findByEmailIgnoreCase("zayn@vnet.id").get();
        assertThat(user.getIsVerified()).isTrue();

        // 4. LOGIN
        Map<String, String> loginBody = Map.of(
            "email", "zayn@vnet.id",
            "password", "zaynpass"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("zayn@vnet.id"));
    }
}
