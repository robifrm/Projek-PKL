package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllConfigs());
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateConfigs(@RequestBody Map<String, Object> payload) {
        systemConfigService.saveAllConfigs(payload);
        return ResponseEntity.ok(systemConfigService.getAllConfigs());
    }
}
