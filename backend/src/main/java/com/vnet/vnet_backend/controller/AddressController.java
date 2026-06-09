package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AnalyticsService analyticsService;

    public AddressController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> insights() {
        return ResponseEntity.ok(analyticsService.getAddressInsights());
    }
}
