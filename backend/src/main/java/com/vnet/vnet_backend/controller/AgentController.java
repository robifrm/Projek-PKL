package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.entity.AgentStatus;
import com.vnet.vnet_backend.repository.AgentRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;
import com.vnet.vnet_backend.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AnalyticsService analyticsService;
    private final AgentRepository agentRepository;
    private final CustomerRepository customerRepository;

    public AgentController(
            AnalyticsService analyticsService,
            AgentRepository agentRepository,
            CustomerRepository customerRepository
    ) {
        this.analyticsService = analyticsService;
        this.agentRepository = agentRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> performance(@RequestParam(defaultValue = "month") String period) {
        return ResponseEntity.ok(analyticsService.getAgentPerformance(period));
    }

    @PostMapping
    public ResponseEntity<Agent> create(@RequestBody Agent agent) {
        if (agent.getStatus() == null) {
            agent.setStatus(AgentStatus.AKTIF);
        }
        Agent saved = agentRepository.save(agent);
        analyticsService.invalidateCache();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agent> update(@PathVariable Long id, @RequestBody Agent payload) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setNama(payload.getNama());
        agent.setKomisi(payload.getKomisi());
        if (payload.getStatus() != null) {
            agent.setStatus(payload.getStatus());
        }
        Agent saved = agentRepository.save(agent);
        analyticsService.invalidateCache();
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Agent> updateStatus(@PathVariable Long id, @RequestParam AgentStatus status) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setStatus(status);
        Agent saved = agentRepository.save(agent);
        analyticsService.invalidateCache();
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        Map<String, Object> result = new LinkedHashMap<>();
        if (customerRepository.countByAgentId(id) > 0) {
            agent.setStatus(AgentStatus.NONAKTIF);
            agentRepository.save(agent);
            result.put("deleted", false);
            result.put("status", "NONAKTIF");
            result.put("message", "Agent has customers, status changed to NONAKTIF.");
        } else {
            agentRepository.delete(agent);
            result.put("deleted", true);
            result.put("message", "Agent deleted.");
        }

        analyticsService.invalidateCache();
        return ResponseEntity.ok(result);
    }
}
