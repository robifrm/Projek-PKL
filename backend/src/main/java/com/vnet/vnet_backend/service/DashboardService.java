package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.AgentRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import com.vnet.vnet_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final InternetPackageRepository packageRepository;
    private final UserRepository userRepository;

    public DashboardService(
            CustomerRepository customerRepository,
            AgentRepository agentRepository,
            InternetPackageRepository packageRepository,
            UserRepository userRepository
    ) {
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> currentUserOpt = userRepository.findByUsernameIgnoreCase(currentUsername);
        User currentUser = currentUserOpt.orElse(null);

        boolean isAgent = currentUser != null && currentUser.getRole() == Role.AGENT;
        Long agentId = isAgent && currentUser.getAgent() != null ? currentUser.getAgent().getId() : null;

        long totalCustomers;
        long totalActive;
        long totalIsolir;
        long totalAgents;
        long totalPackages;

        if (isAgent && agentId != null) {
            totalCustomers = customerRepository.countByAgentId(agentId);
            totalActive = customerRepository.countByAgentIdAndStatus(agentId, CustomerStatus.ACTIVE);
            totalIsolir = customerRepository.countByAgentIdAndStatus(agentId, CustomerStatus.ISOLIR);
            totalAgents = 1;
            totalPackages = packageRepository.findPackagesSoldByAgentId(agentId).size();
        } else {
            totalCustomers = customerRepository.count();
            totalActive = customerRepository.countByStatus(CustomerStatus.ACTIVE);
            totalIsolir = customerRepository.countByStatus(CustomerStatus.ISOLIR);
            totalAgents = agentRepository.count();
            totalPackages = packageRepository.count();
        }

        summary.put("totalCustomers", totalCustomers);
        summary.put("totalActive", totalActive);
        summary.put("totalIsolir", totalIsolir);
        summary.put("totalAgents", totalAgents);
        summary.put("totalPackages", totalPackages);

        return summary;
    }
}
