package com.vnet.vnet_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Smoke Test — VNet Backend Application
 *
 * Memverifikasi bahwa Spring Boot Application Context
 * berhasil di-load tanpa error (menggunakan H2 in-memory,
 * tidak membutuhkan MySQL).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Smoke Test")
class VnetBackendApplicationTests {

    @Test
    @DisplayName("Spring Application Context harus berhasil di-load tanpa error")
    void contextLoads() {
        // Jika context berhasil di-load, test ini otomatis PASS
        assertThatCode(() -> {}).doesNotThrowAnyException();
    }
}
