package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.service.DashboardService;
import com.vnet.vnet_backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AnalyticsService analyticsService;

    public DashboardController(DashboardService dashboardService, AnalyticsService analyticsService) {
        this.dashboardService = dashboardService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(analyticsService.getDashboardOverview(month));
    }
}
