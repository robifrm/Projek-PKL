package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.entity.Customer;
import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.service.AnalyticsService;
import com.vnet.vnet_backend.service.CustomerService;
import com.vnet.vnet_backend.service.ExcelService;
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
import com.vnet.vnet_backend.repository.CustomerRegistrationRepository;
import com.vnet.vnet_backend.repository.BaAktivasiRepository;
import com.vnet.vnet_backend.entity.CustomerRegistration;
import com.vnet.vnet_backend.entity.BaAktivasi;
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
    private final ExcelService excelService;
    private final CustomerRegistrationRepository customerRegistrationRepository;
    private final BaAktivasiRepository baAktivasiRepository;

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

    // EXPORT EXCEL
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers(
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer week
    ) {
        List<Customer> allCustomers = customerService.getAllCustomers();
        
        // Filter in memory
        List<Customer> filteredCustomers = allCustomers;
        if (filterType != null && !filterType.equalsIgnoreCase("all")) {
            filteredCustomers = new java.util.ArrayList<>();
            for (Customer c : allCustomers) {
                LocalDate regDate = c.getTanggalRegistrasi();
                if (regDate == null) {
                    continue;
                }
                
                boolean match = true;
                if (filterType.equalsIgnoreCase("month")) {
                    if (year != null && regDate.getYear() != year) match = false;
                    if (month != null && regDate.getMonthValue() != month) match = false;
                } else if (filterType.equalsIgnoreCase("quarter")) {
                    if (year != null && regDate.getYear() != year) match = false;
                    if (quarter != null) {
                        int q = (regDate.getMonthValue() - 1) / 3 + 1;
                        if (q != quarter) match = false;
                    }
                } else if (filterType.equalsIgnoreCase("year")) {
                    if (year != null && regDate.getYear() != year) match = false;
                } else if (filterType.equalsIgnoreCase("week") || filterType.equalsIgnoreCase("weekly")) {
                    if (year != null && regDate.getYear() != year) match = false;
                    if (month != null && regDate.getMonthValue() != month) match = false;
                    if (week != null) {
                        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
                        int w = regDate.get(weekFields.weekOfMonth());
                        if (w != week) match = false;
                    }
                }
                
                if (match) {
                    filteredCustomers.add(c);
                }
            }
        }
        
        // Build map of custId -> BaAktivasi
        Map<String, BaAktivasi> baMap = new java.util.HashMap<>();
        for (Customer c : filteredCustomers) {
            if (c.getCustId() != null) {
                Optional<CustomerRegistration> regOpt = customerRegistrationRepository.findByCustId(c.getCustId());
                if (regOpt.isPresent()) {
                    Optional<BaAktivasi> baOpt = baAktivasiRepository.findByRegistrationId(regOpt.get().getId());
                    if (baOpt.isPresent()) {
                        baMap.put(c.getCustId(), baOpt.get());
                    }
                }
            }
        }

        byte[] excelBytes = excelService.generateCustomerExport(filteredCustomers, baMap);
        
        String filename = "subscribers_export";
        if (("week".equalsIgnoreCase(filterType) || "weekly".equalsIgnoreCase(filterType)) && year != null && month != null && week != null) {
            filename += "_" + year + "_" + String.format("%02d", month) + "_W" + week;
        } else if ("month".equalsIgnoreCase(filterType) && year != null && month != null) {
            filename += "_" + year + "_" + String.format("%02d", month);
        } else if ("quarter".equalsIgnoreCase(filterType) && year != null && quarter != null) {
            filename += "_" + year + "_Q" + quarter;
        } else if ("year".equalsIgnoreCase(filterType) && year != null) {
            filename += "_" + year;
        } else {
            filename += "_all";
        }
        
        String currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        filename += "_" + currentDate + ".xlsx";
        
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
