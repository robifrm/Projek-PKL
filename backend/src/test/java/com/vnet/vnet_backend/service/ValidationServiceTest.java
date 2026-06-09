package com.vnet.vnet_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test — ValidationService
 *
 * Menguji validasi baris data Excel yang diimport:
 * field wajib, normalisasi key, dan penentuan INSERT vs UPDATE.
 */
@DisplayName("ValidationService Unit Tests")
class ValidationServiceTest {

    private com.vnet.vnet_backend.repository.CustomerRepository customerRepository;
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(com.vnet.vnet_backend.repository.CustomerRepository.class);
        validationService = new ValidationService(customerRepository);
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    private Map<String, String> row(String custId, String nama) {
        Map<String, String> r = new LinkedHashMap<>();
        if (custId != null) r.put("Cust ID", custId);
        if (nama   != null) r.put("Nama",    nama);
        return r;
    }

    // ─────────────────────────────────────────────
    // validate() — hasil status
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("validate() row valid baru harus menghasilkan VALID + INSERT")
    void validate_newValidRow_shouldReturnValidInsert() {
        when(customerRepository.existsByCustId("C001")).thenReturn(false);

        List<Map<String, Object>> result = validationService.validate(List.of(row("C001", "Budi")));

        assertThat(result).hasSize(1);
        Map<String, Object> r = result.get(0);
        assertThat(r.get("validationStatus")).isEqualTo("VALID");
        assertThat(r.get("actionType")).isEqualTo("INSERT");
        assertThat(r.get("errorMessage")).isEqualTo("");
    }

    @Test
    @DisplayName("validate() row valid yang custId sudah ada harus menghasilkan VALID + UPDATE")
    void validate_existingCustId_shouldReturnValidUpdate() {
        when(customerRepository.existsByCustId("C002")).thenReturn(true);

        List<Map<String, Object>> result = validationService.validate(List.of(row("C002", "Siti")));

        Map<String, Object> r = result.get(0);
        assertThat(r.get("validationStatus")).isEqualTo("VALID");
        assertThat(r.get("actionType")).isEqualTo("UPDATE");
    }

    @Test
    @DisplayName("validate() row tanpa custId harus menghasilkan ERROR + SKIP")
    void validate_missingCustId_shouldReturnError() {
        List<Map<String, Object>> result = validationService.validate(List.of(row("", "Budi")));

        Map<String, Object> r = result.get(0);
        assertThat(r.get("validationStatus")).isEqualTo("ERROR");
        assertThat(r.get("actionType")).isEqualTo("SKIP");
        assertThat(r.get("errorMessage").toString()).contains("Cust ID");
    }

    @Test
    @DisplayName("validate() row tanpa nama harus menghasilkan ERROR + SKIP")
    void validate_missingNama_shouldReturnError() {
        List<Map<String, Object>> result = validationService.validate(List.of(row("C003", "")));

        Map<String, Object> r = result.get(0);
        assertThat(r.get("validationStatus")).isEqualTo("ERROR");
        assertThat(r.get("errorMessage").toString()).contains("Nama");
    }

    @Test
    @DisplayName("validate() row kosong total harus menghasilkan ERROR dengan 2 pesan")
    void validate_completelyEmptyRow_shouldReport2Errors() {
        List<Map<String, Object>> result = validationService.validate(List.of(row("", "")));

        String errorMsg = result.get(0).get("errorMessage").toString();
        assertThat(errorMsg).contains("Cust ID");
        assertThat(errorMsg).contains("Nama");
    }

    @Test
    @DisplayName("validate() multiple rows harus memproses semua baris independen")
    void validate_multipleRows_shouldProcessAll() {
        when(customerRepository.existsByCustId("C001")).thenReturn(false);
        when(customerRepository.existsByCustId("C002")).thenReturn(true);

        List<Map<String, String>> rows = List.of(
            row("C001", "Alice"),
            row("C002", "Bob"),
            row("",     "Charlie")
        );

        List<Map<String, Object>> result = validationService.validate(rows);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).get("actionType")).isEqualTo("INSERT");
        assertThat(result.get(1).get("actionType")).isEqualTo("UPDATE");
        assertThat(result.get(2).get("actionType")).isEqualTo("SKIP");
    }

    @Test
    @DisplayName("validate() daftar kosong harus mengembalikan daftar kosong")
    void validate_emptyList_shouldReturnEmpty() {
        assertThat(validationService.validate(new ArrayList<>())).isEmpty();
    }

    // ─────────────────────────────────────────────
    // Key normalization — berbagai format header Excel
    // ─────────────────────────────────────────────

    @ParameterizedTest
    @DisplayName("validate() harus mengenali berbagai format header untuk Cust ID")
    @CsvSource({
        "Cust ID",
        "Customer ID",
        "custId",
        "cust_id"
    })
    void validate_shouldRecognizeVariousCustIdHeaders(String headerKey) {
        when(customerRepository.existsByCustId(anyString())).thenReturn(false);
        Map<String, String> r = new LinkedHashMap<>();
        r.put(headerKey, "C999");
        r.put("Nama", "TestUser");

        List<Map<String, Object>> result = validationService.validate(List.of(r));

        assertThat(result.get(0).get("validationStatus")).isEqualTo("VALID");
    }

    @ParameterizedTest
    @DisplayName("validate() harus mengenali berbagai format header untuk Nama")
    @CsvSource({
        "Nama",
        "Customer Name",
        "Name",
        "nama"
    })
    void validate_shouldRecognizeVariousNamaHeaders(String headerKey) {
        when(customerRepository.existsByCustId(anyString())).thenReturn(false);
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Cust ID", "C998");
        r.put(headerKey, "Budi Santoso");

        List<Map<String, Object>> result = validationService.validate(List.of(r));

        assertThat(result.get(0).get("validationStatus")).isEqualTo("VALID");
    }
}
