package com.vnet.vnet_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ImportController Functional Tests")
class ImportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private ExcelService excelService;
    @MockitoBean private ValidationService validationService;
    @MockitoBean private ImportService importService;
    @MockitoBean private AnalyticsService analyticsService;
    @MockitoBean private SystemConfigService systemConfigService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = User.builder()
                .name("Super Admin")
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .role(Role.SUPER_ADMIN)
                .build();
        userRepository.save(admin);
        adminToken = "Bearer " + jwtProvider.generateToken(admin);
    }

    @Test
    @DisplayName("preview() harus ditolak dengan 400 jika maintenanceMode aktif")
    void preview_shouldFail_whenMaintenanceModeActive() throws Exception {
        when(systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                "dummy content".getBytes());

        mockMvc.perform(multipart("/api/import/preview")
                .file(file)
                .header("Authorization", adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sistem sedang dalam maintenance mode"));

        verify(excelService, never()).parseFile(any());
    }

    @Test
    @DisplayName("preview() harus sukses jika maintenanceMode nonaktif")
    void preview_shouldSucceed_whenMaintenanceModeInactive() throws Exception {
        when(systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)).thenReturn(false);
        when(excelService.parseFile(any())).thenReturn(List.of(Map.of("name", "John Doe")));
        when(validationService.validate(any())).thenReturn(List.of(Map.of("name", "John Doe", "status", "VALID")));

        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                "dummy content".getBytes());

        mockMvc.perform(multipart("/api/import/preview")
                .file(file)
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("confirm() harus ditolak dengan 400 jika maintenanceMode aktif")
    void confirm_shouldFail_whenMaintenanceModeActive() throws Exception {
        when(systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)).thenReturn(true);

        Map<String, Object> payload = Map.of("fileName", "test.xlsx", "rows", List.of(Map.of("name", "John Doe")));

        mockMvc.perform(post("/api/import/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
                .header("Authorization", adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sistem sedang dalam maintenance mode"));

        verify(importService, never()).processImport(anyString(), anyList());
    }

    @Test
    @DisplayName("confirm() harus ditolak jika rows melebihi maxImportBatch")
    void confirm_shouldFail_whenRowsExceedMaxImportBatch() throws Exception {
        when(systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)).thenReturn(false);
        when(systemConfigService.getInt(SystemConfigService.KEY_MAX_IMPORT_BATCH, 1200)).thenReturn(2);

        List<Map<String, Object>> rows = List.of(
                Map.of("name", "User 1"),
                Map.of("name", "User 2"),
                Map.of("name", "User 3")
        );
        Map<String, Object> payload = Map.of("fileName", "test.xlsx", "rows", rows);

        mockMvc.perform(post("/api/import/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
                .header("Authorization", adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Jumlah baris data melebihi batas maksimum import (2 baris)"));

        verify(importService, never()).processImport(anyString(), anyList());
    }

    @Test
    @DisplayName("confirm() harus sukses jika di bawah maxImportBatch dan maintenanceMode nonaktif")
    void confirm_shouldSucceed_whenValid() throws Exception {
        when(systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)).thenReturn(false);
        when(systemConfigService.getInt(SystemConfigService.KEY_MAX_IMPORT_BATCH, 1200)).thenReturn(5);
        when(importService.processImport(anyString(), anyList())).thenReturn(Map.of("successCount", 2));

        List<Map<String, Object>> rows = List.of(
                Map.of("name", "User 1"),
                Map.of("name", "User 2")
        );
        Map<String, Object> payload = Map.of("fileName", "test.xlsx", "rows", rows);

        mockMvc.perform(post("/api/import/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(2));

        verify(importService).processImport(eq("test.xlsx"), anyList());
        verify(analyticsService).invalidateCache();
    }
}
