package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.entity.Customer;
import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.service.AnalyticsService;
import com.vnet.vnet_backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AnalyticsService analyticsService;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"));

    // CREATE
    @PostMapping(consumes = "application/json", produces = "application/json")
    public Customer createCustomer(@RequestBody Customer customer) {
        Customer saved = customerService.createCustomer(customer);
        analyticsService.invalidateCache();
        return saved;
    }

    // GET ALL
    @GetMapping
    public List<Customer> getAllCustomers() { return customerService.getAllCustomers(); }

    @GetMapping("/page")
    public Page<Customer> getCustomersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return customerService.getCustomersPage(search, pageable);
    }

    // GET DETAIL BY CUST-ID — returns rich detail map for the CustomerDetailPanel
    @GetMapping("/{custId}/detail")
    public ResponseEntity<Map<String, Object>> getCustomerDetail(@PathVariable String custId) {
        Optional<Customer> opt = customerService.findByCustId(custId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Customer c = opt.get();

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id",          c.getId());
        detail.put("custId",      c.getCustId());
        detail.put("nama",        c.getNama());
        detail.put("noTelpon",    c.getNoTelpon());
        detail.put("email",       c.getEmail());
        detail.put("status",      c.getStatus() != null ? c.getStatus().name() : "UNKNOWN");
        detail.put("isolir",      Boolean.TRUE.equals(c.getIsolir()));
        detail.put("price",       c.getPrice());
        detail.put("profit",      c.getProfit());
        detail.put("biayaPasang", c.getBiayaPasang());

        detail.put("tanggalRegistrasi",
                c.getTanggalRegistrasi() != null ? c.getTanggalRegistrasi().format(DATE_FMT) : null);
        detail.put("tanggalAktivasi",
                c.getTanggalAktivasi() != null ? c.getTanggalAktivasi().format(DATE_FMT) : null);
        if (c.getTanggalRegistrasi() != null) {
            detail.put("memberSinceDays",
                    ChronoUnit.DAYS.between(c.getTanggalRegistrasi(), LocalDate.now()));
        }

        // Package
        if (c.getPkg() != null) {
            Map<String, Object> pkg = new LinkedHashMap<>();
            pkg.put("name",        c.getPkg().getName());
            pkg.put("speed",       c.getPkg().getSpeed());
            pkg.put("price",       c.getPkg().getPrice());
            pkg.put("profit",      c.getPkg().getProfit());
            pkg.put("description", c.getPkg().getDescription());
            detail.put("package",  pkg);
        }

        // Address
        if (c.getAddress() != null) {
            Map<String, Object> addr = new LinkedHashMap<>();
            addr.put("alamat",    c.getAddress().getAlamat());
            addr.put("rtRw",      c.getAddress().getRtRw());
            addr.put("kelurahan", c.getAddress().getKelurahan());
            addr.put("kecamatan", c.getAddress().getKecamatan());
            addr.put("kota",      c.getAddress().getKota());
            addr.put("kodePos",   c.getAddress().getKodePos());
            detail.put("address", addr);
        }

        // Agent
        if (c.getAgent() != null) {
            Agent ag = c.getAgent();
            Map<String, Object> agent = new LinkedHashMap<>();
            agent.put("nama",     ag.getNama());
            agent.put("noTelpon", ag.getNoTelpon());
            agent.put("area",     ag.getArea());
            agent.put("status",   ag.getStatus() != null ? ag.getStatus().name() : "-");
            detail.put("agent",   agent);
        }

        return ResponseEntity.ok(detail);
    }

    // UPDATE STATUS
    @PutMapping("/{id}/status")
    public Customer updateStatus(
            @PathVariable Long id,
            @RequestParam CustomerStatus status
    ) {
        Customer saved = customerService.updateStatus(id, status);
        analyticsService.invalidateCache();
        return saved;
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        analyticsService.invalidateCache();
    }
}
