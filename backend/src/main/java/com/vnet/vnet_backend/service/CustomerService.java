package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.*;
import com.vnet.vnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        // Auto-generate custId jika kosong
        if (customer.getCustId() == null || customer.getCustId().trim().isEmpty()) {
            long count = customerRepository.count() + 1;
            String nextId = "C" + String.format("%03d", count);
            while (customerRepository.existsByCustId(nextId)) {
                count++;
                nextId = "C" + String.format("%03d", count);
            }
            customer.setCustId(nextId);
        } else {
            // Validasi custId unik jika diinput manual
            if (customerRepository.existsByCustId(customer.getCustId())) {
                throw new RuntimeException("Cust ID already exists!");
            }
        }

        // Validasi / Resolve agent jika diisi
        if (customer.getAgent() != null) {
            Agent agent = null;
            if (customer.getAgent().getId() != null) {
                agent = agentRepository.findById(customer.getAgent().getId())
                        .orElseThrow(() -> new RuntimeException("Agent not found"));
            } else if (customer.getAgent().getNama() != null && !customer.getAgent().getNama().trim().isEmpty()) {
                String agentName = customer.getAgent().getNama().trim();
                agent = agentRepository.findByNamaIgnoreCase(agentName)
                        .orElseGet(() -> {
                            Agent newAgent = new Agent();
                            newAgent.setNama(agentName);
                            newAgent.setStatus(AgentStatus.AKTIF);
                            newAgent.setKomisi(0.0);
                            return agentRepository.save(newAgent);
                        });
            }

            if (agent != null) {
                if (agent.getStatus() != AgentStatus.AKTIF) {
                    throw new RuntimeException("Agent is not ACTIVE");
                }
                customer.setAgent(agent);
            } else {
                customer.setAgent(null);
            }
        }

        // Validasi / Resolve package
        if (customer.getPkg() == null || customer.getPkg().getId() == null) {
            throw new RuntimeException("Package is required");
        }
        InternetPackage pkg = internetPackageRepository.findById(customer.getPkg().getId())
                .orElseThrow(() -> new RuntimeException("Package not found"));
        customer.setPkg(pkg);

        // Validasi / Resolve address
        if (customer.getAddress() != null) {
            Address address = null;
            if (customer.getAddress().getId() != null) {
                address = addressRepository.findById(customer.getAddress().getId())
                        .orElseThrow(() -> new RuntimeException("Address not found"));
            } else if (customer.getAddress().getAlamat() != null && !customer.getAddress().getAlamat().trim().isEmpty()) {
                String alamat = customer.getAddress().getAlamat().trim();
                String kelurahan = customer.getAddress().getKelurahan() != null ? customer.getAddress().getKelurahan().trim() : "";
                String kecamatan = customer.getAddress().getKecamatan() != null ? customer.getAddress().getKecamatan().trim() : "";
                String kota = customer.getAddress().getKota() != null ? customer.getAddress().getKota().trim() : "Sukabumi";
                String rtRw = customer.getAddress().getRtRw() != null ? customer.getAddress().getRtRw().trim() : "";
                String kodePos = customer.getAddress().getKodePos() != null ? customer.getAddress().getKodePos().trim() : "";

                address = addressRepository.findFirstByAlamatIgnoreCaseAndKotaIgnoreCase(alamat, kota)
                        .orElseGet(() -> {
                            Address newAddr = new Address();
                            newAddr.setAlamat(alamat);
                            newAddr.setKelurahan(kelurahan);
                            newAddr.setKecamatan(kecamatan);
                            newAddr.setKota(kota);
                            newAddr.setRtRw(rtRw);
                            newAddr.setKodePos(kodePos);
                            return addressRepository.save(newAddr);
                        });
            }
            customer.setAddress(address);
        }

        if (customer.getAddress() == null) {
            throw new RuntimeException("Address is required");
        }

        // Default prices / profit jika belum diisi
        if (customer.getPrice() == null) {
            customer.setPrice(pkg.getPrice());
        }
        if (customer.getProfit() == null) {
            customer.setProfit(pkg.getProfit());
        }
        if (customer.getBiayaPasang() == null) {
            customer.setBiayaPasang(0.0);
        }
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }
        if (customer.getIsolir() == null) {
            customer.setIsolir(false);
        }
        if (customer.getTanggalRegistrasi() == null) {
            customer.setTanggalRegistrasi(LocalDate.now());
        }
        if (customer.getTanggalAktivasi() == null) {
            customer.setTanggalAktivasi(LocalDate.now());
        }

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