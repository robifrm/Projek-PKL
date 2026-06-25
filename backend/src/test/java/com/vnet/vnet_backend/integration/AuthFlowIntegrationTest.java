package com.vnet.vnet_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.service.EmailService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test — AuthFlowIntegrationTest
 *
 * Menguji alur autentikasi login dengan H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Login Integration Tests")
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Mock EmailService agar tidak memanggil real API Resend yang akan error dengan dummy key
    @MockitoBean private EmailService emailService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        doNothing().when(emailService).sendOtpEmail(any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Alur Otorisasi: Seed User Manual -> Login Sukses")
    void successfulLogin() throws Exception {
        // 1. SEED USER MANUAL
        User user = User.builder()
                .name("Zayn Malik")
                .username("zayn123")
                .password(passwordEncoder.encode("zaynpass"))
                .role(Role.STAFF)
                .build();
        userRepository.save(user);

        // 2. LOGIN HARUS SUKSES
        Map<String, String> loginBody = Map.of(
            "username", "zayn123",
            "password", "zaynpass"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("zayn123"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }
}
