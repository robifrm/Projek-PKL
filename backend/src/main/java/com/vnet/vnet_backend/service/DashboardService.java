package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.repository.AgentRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final InternetPackageRepository packageRepository;

    public DashboardService(
            CustomerRepository customerRepository,
            AgentRepository agentRepository,
            InternetPackageRepository packageRepository
    ) {
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
        this.packageRepository = packageRepository;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        long totalCustomers = customerRepository.count();
        long totalActive = customerRepository.countByStatus(CustomerStatus.ACTIVE);
        long totalIsolir = customerRepository.countByStatus(CustomerStatus.ISOLIR);

        long totalAgents = agentRepository.count();
        long totalPackages = packageRepository.count();

        summary.put("totalCustomers", totalCustomers);
        summary.put("totalActive", totalActive);
        summary.put("totalIsolir", totalIsolir);
        summary.put("totalAgents", totalAgents);
        summary.put("totalPackages", totalPackages);

        return summary;
    }
}
