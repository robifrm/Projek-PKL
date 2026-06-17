package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.service.AnalyticsService;
import com.vnet.vnet_backend.service.ExcelService;
import com.vnet.vnet_backend.service.ImportService;
import com.vnet.vnet_backend.service.ValidationService;
import com.vnet.vnet_backend.service.SystemConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
public class ImportController {

    private final ExcelService excelService;
    private final ValidationService validationService;
    private final ImportService importService;
    private final AnalyticsService analyticsService;
    private final SystemConfigService systemConfigService;

    public ImportController(
            ExcelService excelService,
            ValidationService validationService,
            ImportService importService,
            AnalyticsService analyticsService,
            SystemConfigService systemConfigService
    ) {
        this.excelService = excelService;
        this.validationService = validationService;
        this.importService = importService;
        this.analyticsService = analyticsService;
        this.systemConfigService = systemConfigService;
    }

    /**
     * Upload Excel dan tampilkan preview hasil validasi.
     */
    @PostMapping("/preview")
    public ResponseEntity<List<Map<String, Object>>> preview(
            @RequestParam("file") MultipartFile file
    ) {
        // Cek apakah sistem sedang dalam Maintenance Mode
        if (systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sistem sedang dalam maintenance mode");
        }

        List<Map<String, String>> parsedData =
                excelService.parseFile(file);

        List<Map<String, Object>> validatedData =
                validationService.validate(parsedData);

        return ResponseEntity.ok(validatedData);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(importService.getImportStats());
    }

    /**
     * Simpan data hasil preview ke database.
     * Setelah berhasil, cache analytics di-invalidate agar dashboard refresh.
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(
            @RequestBody Map<String, Object> payload
    ) {
        // Cek apakah sistem sedang dalam Maintenance Mode
        if (systemConfigService.getBool(SystemConfigService.KEY_MAINTENANCE_MODE, false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sistem sedang dalam maintenance mode");
        }

        String fileName = (String) payload.getOrDefault("fileName", "manual-confirm");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) payload.get("rows");

        // Cek kapasitas maks import batch
        int maxImportBatch = systemConfigService.getInt(SystemConfigService.KEY_MAX_IMPORT_BATCH, 1200);
        if (rows != null && rows.size() > maxImportBatch) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Jumlah baris data melebihi batas maksimum import (" + maxImportBatch + " baris)");
        }

        Map<String, Object> result =
                importService.processImport(fileName, rows);

        // Invalidate analytics cache so dashboard shows fresh data immediately
        analyticsService.invalidateCache();

        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getReason());
        body.put("message", ex.getReason());
        body.put("status", ex.getStatusCode().value());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
