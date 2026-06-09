package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Test — CustomerRepository
 *
 * Menggunakan H2 in-memory database (@DataJpaTest).
 * Memverifikasi query custom: existsByCustId, searchCustomers, countByStatus.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CustomerRepository Integration Tests")
class CustomerRepositoryTest {

    @Autowired private CustomerRepository        customerRepository;
    @Autowired private InternetPackageRepository packageRepository;
    @Autowired private AddressRepository         addressRepository;

    private InternetPackage savedPkg;
    private Address         savedAddr;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        packageRepository.deleteAll();
        addressRepository.deleteAll();

        InternetPackage pkg = new InternetPackage();
        pkg.setName("Paket 10 Mbps"); pkg.setSpeed(10.0); pkg.setPrice(150000.0);
        savedPkg = packageRepository.save(pkg);

        Address addr = new Address();
        addr.setAlamat("Jl. Cikole No. 12");
        addr.setKecamatan("Cikole"); addr.setKelurahan("Cikole"); addr.setKota("Sukabumi");
        savedAddr = addressRepository.save(addr);
    }

    private Customer makeCustomer(String custId, String nama, CustomerStatus status) {
        Customer c = new Customer();
        c.setCustId(custId); c.setNama(nama);
        c.setStatus(status);
        c.setPkg(savedPkg); c.setAddress(savedAddr);
        c.setTanggalRegistrasi(LocalDate.now());
        return customerRepository.save(c);
    }

    // ─────────────────────────────────────────────
    // existsByCustId()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("existsByCustId() harus return true jika custId ada")
    void existsByCustId_existing_shouldReturnTrue() {
        makeCustomer("C001", "Budi", CustomerStatus.ACTIVE);
        assertThat(customerRepository.existsByCustId("C001")).isTrue();
    }

    @Test
    @DisplayName("existsByCustId() harus return false jika custId tidak ada")
    void existsByCustId_notFound_shouldReturnFalse() {
        assertThat(customerRepository.existsByCustId("GHOST")).isFalse();
    }

    // ─────────────────────────────────────────────
    // findByCustId()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByCustId() harus menemukan customer dengan custId yang benar")
    void findByCustId_existingId_shouldReturnCustomer() {
        makeCustomer("C002", "Siti", CustomerStatus.ACTIVE);
        Optional<Customer> result = customerRepository.findByCustId("C002");

        assertThat(result).isPresent();
        assertThat(result.get().getNama()).isEqualTo("Siti");
    }

    @Test
    @DisplayName("findByCustId() harus return empty jika tidak ada")
    void findByCustId_notFound_shouldReturnEmpty() {
        assertThat(customerRepository.findByCustId("ZZZZ")).isEmpty();
    }

    // ─────────────────────────────────────────────
    // countByStatus()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("countByStatus() harus menghitung customer berdasarkan status dengan tepat")
    void countByStatus_shouldReturnCorrectCount() {
        makeCustomer("C003", "Alice", CustomerStatus.ACTIVE);
        makeCustomer("C004", "Bob",   CustomerStatus.ACTIVE);
        makeCustomer("C005", "Carol", CustomerStatus.ISOLIR);

        assertThat(customerRepository.countByStatus(CustomerStatus.ACTIVE)).isEqualTo(2);
        assertThat(customerRepository.countByStatus(CustomerStatus.ISOLIR)).isEqualTo(1);
    }

    // ─────────────────────────────────────────────
    // searchCustomers()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("searchCustomers() harus menemukan customer berdasarkan nama (case-insensitive)")
    void searchCustomers_byName_shouldFindCorrect() {
        makeCustomer("C006", "Ahmad Fauzi", CustomerStatus.ACTIVE);
        makeCustomer("C007", "Budi Santoso", CustomerStatus.ACTIVE);

        Page<Customer> result = customerRepository.searchCustomers("ahmad", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNama()).isEqualTo("Ahmad Fauzi");
    }

    @Test
    @DisplayName("searchCustomers() harus menemukan customer berdasarkan custId (case-insensitive)")
    void searchCustomers_byCustId_shouldFindCorrect() {
        makeCustomer("VNT-001", "Dewi", CustomerStatus.ACTIVE);
        makeCustomer("VNT-002", "Eko",  CustomerStatus.ACTIVE);

        Page<Customer> result = customerRepository.searchCustomers("vnt-001", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCustId()).isEqualTo("VNT-001");
    }

    @Test
    @DisplayName("searchCustomers() harus return semua hasil untuk query kosong")
    void searchCustomers_emptySearch_shouldReturnAll() {
        makeCustomer("C010", "User A", CustomerStatus.ACTIVE);
        makeCustomer("C011", "User B", CustomerStatus.ACTIVE);

        Page<Customer> result = customerRepository.searchCustomers("", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    // ─────────────────────────────────────────────
    // findTop5ByOrderByTanggalRegistrasiDesc()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findTop5ByOrderByTanggalRegistrasiDesc() harus return maksimal 5 customer terbaru")
    void findTop5_shouldReturnAtMost5() {
        for (int i = 1; i <= 7; i++) {
            Customer c = makeCustomer("TOP-" + i, "User " + i, CustomerStatus.ACTIVE);
            c.setTanggalRegistrasi(LocalDate.now().minusDays(7 - i));
            customerRepository.save(c);
        }

        List<Customer> top5 = customerRepository.findTop5ByOrderByTanggalRegistrasiDesc();

        assertThat(top5).hasSizeLessThanOrEqualTo(5);
        // Pastikan diurutkan DESC (tanggal terbaru duluan)
        if (top5.size() >= 2) {
            assertThat(top5.get(0).getTanggalRegistrasi())
                    .isAfterOrEqualTo(top5.get(1).getTanggalRegistrasi());
        }
    }
}

