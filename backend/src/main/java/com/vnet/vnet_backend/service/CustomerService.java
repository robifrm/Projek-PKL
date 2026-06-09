package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.*;
import com.vnet.vnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final InternetPackageRepository internetPackageRepository;
    private final AddressRepository addressRepository;

    public java.util.Optional<Customer> findByCustId(String custId) { return customerRepository.findByCustId(custId); }

    // CREATE CUSTOMER
    public Customer createCustomer(Customer customer) {

        // Validasi custId unik
        if (customerRepository.existsByCustId(customer.getCustId())) {
            throw new RuntimeException("Cust ID already exists!");
        }

        // Validasi agent jika ada
        if (customer.getAgent() != null) {
            Agent agent = agentRepository.findById(customer.getAgent().getId())
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            if (agent.getStatus() != AgentStatus.AKTIF) {
                throw new RuntimeException("Agent is not ACTIVE");
            }

            customer.setAgent(agent);
        }

        // Validasi package
        InternetPackage pkg = internetPackageRepository.findById(customer.getPkg().getId())
                .orElseThrow(() -> new RuntimeException("Package not found"));
        customer.setPkg(pkg);

        // Validasi address
        Address address = addressRepository.findById(customer.getAddress().getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));
        customer.setAddress(address);

        return customerRepository.save(customer);
    }

    // GET ALL
    public List<Customer> getAllCustomers() { return customerRepository.findAllWithRelations(); }

    public Page<Customer> getCustomersPage(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.searchCustomers(search, pageable);
    }

    // UPDATE STATUS
    public Customer updateStatus(Long id, CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setStatus(status);
        return customerRepository.save(customer);
    }
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) throw new RuntimeException("Customer not found");
        customerRepository.deleteById(id);
    }
}