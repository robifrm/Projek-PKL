package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.service.AnalyticsService;
import com.vnet.vnet_backend.service.ExcelService;
import com.vnet.vnet_backend.service.ImportService;
import com.vnet.vnet_backend.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    public ImportController(
            ExcelService excelService,
            ValidationService validationService,
            ImportService importService,
            AnalyticsService analyticsService
    ) {
        this.excelService = excelService;
        this.validationService = validationService;
        this.importService = importService;
        this.analyticsService = analyticsService;
    }

    /**
     * Upload Excel dan tampilkan preview hasil validasi.
     */
    @PostMapping("/preview")
    public ResponseEntity<List<Map<String, Object>>> preview(
            @RequestParam("file") MultipartFile file
    ) {
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
        String fileName = (String) payload.getOrDefault("fileName", "manual-confirm");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) payload.get("rows");

        Map<String, Object> result =
                importService.processImport(fileName, rows);

        // Invalidate analytics cache so dashboard shows fresh data immediately
        analyticsService.invalidateCache();

        return ResponseEntity.ok(result);
    }
}
