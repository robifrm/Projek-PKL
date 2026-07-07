package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.entity.BaAktivasi;
import com.vnet.vnet_backend.entity.CustomerRegistration;
import com.vnet.vnet_backend.entity.RegistrationStatus;
import com.vnet.vnet_backend.service.BaAktivasiService;
import com.vnet.vnet_backend.service.CustomerRegistrationService;
import com.vnet.vnet_backend.service.ExcelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class CustomerRegistrationController {

    private final CustomerRegistrationService service;
    private final BaAktivasiService baAktivasiService;
    private final ExcelService excelService;

    public CustomerRegistrationController(
            CustomerRegistrationService service,
            BaAktivasiService baAktivasiService,
            ExcelService excelService
    ) {
        this.service = service;
        this.baAktivasiService = baAktivasiService;
        this.excelService = excelService;
    }

    /**
     * Endpoint publik untuk mengirim registrasi baru.
     * Menerima data JSON sebagai form field 'data' dan file foto identitas opsional.
     */
    @PostMapping(value = "/public", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<CustomerRegistration> registerCustomer(
            @RequestPart(value = "data", required = false) String dataJson,
            @RequestParam(value = "data", required = false) String dataParam,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws Exception {
        String json = dataJson != null ? dataJson : dataParam;
        ObjectMapper mapper = new ObjectMapper();
        CustomerRegistration registration = mapper.readValue(json, CustomerRegistration.class);

        if (photo != null && !photo.isEmpty()) {
            String mimeType = photo.getContentType() != null ? photo.getContentType() : "image/jpeg";
            String b64 = Base64.getEncoder().encodeToString(photo.getBytes());
            registration.setIdentityPhotoUrl("data:" + mimeType + ";base64," + b64);
        }

        CustomerRegistration saved = service.submitRegistration(registration);
        return ResponseEntity.ok(saved);
    }

    /**
     * Mendapatkan daftar semua registrasi dengan opsi filter status dan search.
     * Membutuhkan peran SUPER_ADMIN, STAFF, atau AGENT.
     */
    @GetMapping
    public ResponseEntity<List<CustomerRegistration>> getAllRegistrations(
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(required = false) String search
    ) {
        List<CustomerRegistration> list = service.getRegistrations(status, search);
        return ResponseEntity.ok(list);
    }

    /**
     * Mendapatkan detail registrasi berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerRegistration> getRegistrationById(@PathVariable Long id) {
        CustomerRegistration reg = service.getRegistrationById(id);
        return ResponseEntity.ok(reg);
    }

    /**
     * Endpoint untuk export data registrasi SELESAI ke Excel.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRegistrations() {
        List<CustomerRegistration> completed = service.getRegistrations(RegistrationStatus.SELESAI, null);
        java.util.Map<Long, BaAktivasi> baMap = new java.util.HashMap<>();
        for (CustomerRegistration reg : completed) {
            baAktivasiService.getBaByRegistrationId(reg.getId()).ifPresent(ba -> baMap.put(reg.getId(), ba));
        }

        byte[] excelBytes = excelService.generateRegistrationExport(completed, baMap);

        String dateStr = java.time.LocalDate.now().toString();
        String filename = "registrasi_selesai_" + dateStr + ".xlsx";

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }


    /**
     * Memperbarui status registrasi (misal: DIPROSES, SELESAI, DIBATALKAN).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerRegistration> updateStatus(
            @PathVariable Long id,
            @RequestParam RegistrationStatus status,
            @RequestParam(required = false) String tanggalJadwal
    ) {
        java.time.LocalDateTime parsedJadwal = null;
        if (tanggalJadwal != null && !tanggalJadwal.trim().isEmpty()) {
            try {
                String cleanVal = tanggalJadwal.replace(" ", "T");
                if (cleanVal.length() == 16) {
                    cleanVal += ":00";
                }
                parsedJadwal = java.time.LocalDateTime.parse(cleanVal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        CustomerRegistration updated = service.updateStatus(id, status, parsedJadwal);
        return ResponseEntity.ok(updated);
    }

    /**
     * Menghapus data registrasi.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id) {
        service.deleteRegistration(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/ba-setup")
    public ResponseEntity<CustomerRegistration> updateBaSetup(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload
    ) {
        CustomerRegistration updated = service.updateBaSetup(id, payload);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerRegistration> updateRegistration(
            @PathVariable Long id,
            @RequestBody CustomerRegistration updatedData
    ) {
        CustomerRegistration updated = service.updateRegistration(id, updatedData);
        return ResponseEntity.ok(updated);
    }

    /**
     * Mendapatkan data BA Aktivasi berdasarkan ID registrasi.
     */
    @GetMapping("/{id}/ba")
    public ResponseEntity<BaAktivasi> getBaAktivasi(@PathVariable Long id) {
        return ResponseEntity.ok(baAktivasiService.getBaByRegistrationId(id).orElse(null));
    }

    @PostMapping("/{id}/ba")
    public ResponseEntity<?> saveBaAktivasi(
            @PathVariable Long id,
            @RequestBody BaAktivasi data
    ) {
        try {
            BaAktivasi saved = baAktivasiService.saveBa(id, data);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Terjadi kesalahan internal"));
        }
    }
}
