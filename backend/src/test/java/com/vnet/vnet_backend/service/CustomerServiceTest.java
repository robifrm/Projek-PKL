package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.*;
import com.vnet.vnet_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test — CustomerService
 *
 * Menguji validasi pembuatan customer: cust ID unik,
 * agent aktif, package ada, dan address ada.
 */
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    private CustomerRepository        customerRepository;
    private AgentRepository           agentRepository;
    private InternetPackageRepository packageRepository;
    private AddressRepository         addressRepository;
    private UserRepository            userRepository;
    private CustomerService           customerService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        agentRepository    = mock(AgentRepository.class);
        packageRepository  = mock(InternetPackageRepository.class);
        addressRepository  = mock(AddressRepository.class);
        userRepository     = mock(UserRepository.class);

        customerService = new CustomerService(
                customerRepository, agentRepository, packageRepository, addressRepository, userRepository);
    }

    // ─────────────────────────────────────────────
    // Helper builders
    // ─────────────────────────────────────────────

    private InternetPackage pkg(Long id) {
        InternetPackage p = new InternetPackage();
        p.setId(id); p.setName("Paket 10 Mbps"); p.setSpeed(10.0); p.setPrice(150000.0);
        return p;
    }

    private Address addr(Long id) {
        Address a = new Address();
        a.setId(id); a.setKecamatan("Cikole"); a.setKelurahan("Cikole"); a.setKota("Sukabumi");
        return a;
    }

    private Agent agent(Long id, AgentStatus status) {
        Agent a = new Agent();
        a.setId(id); a.setNama("Agen A"); a.setStatus(status);
        return a;
    }

    private Customer buildCustomer(String custId, Long pkgId, Long addrId, Long agentId) {
        Customer c = new Customer();
        c.setCustId(custId);
        c.setNama("Pelanggan Test");

        InternetPackage p = new InternetPackage(); p.setId(pkgId);
        c.setPkg(p);

        Address a = new Address(); a.setId(addrId);
        c.setAddress(a);

        if (agentId != null) {
            Agent ag = new Agent(); ag.setId(agentId);
            c.setAgent(ag);
        }
        return c;
    }

    // ─────────────────────────────────────────────
    // createCustomer()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("createCustomer() berhasil untuk data lengkap dan valid (tanpa agent)")
    void createCustomer_validWithoutAgent_shouldSaveAndReturn() {
        when(customerRepository.existsByCustId("C001")).thenReturn(false);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg(1L)));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addr(1L)));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.createCustomer(buildCustomer("C001", 1L, 1L, null));

        assertThat(result.getCustId()).isEqualTo("C001");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("createCustomer() berhasil dengan agent yang aktif")
    void createCustomer_validWithActiveAgent_shouldSucceed() {
        when(customerRepository.existsByCustId("C002")).thenReturn(false);
        when(agentRepository.findById(10L)).thenReturn(Optional.of(agent(10L, AgentStatus.AKTIF)));
        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg(1L)));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addr(1L)));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.createCustomer(buildCustomer("C002", 1L, 1L, 10L));

        assertThat(result.getCustId()).isEqualTo("C002");
        assertThat(result.getAgent()).isNotNull();
    }

    @Test
    @DisplayName("createCustomer() harus throw jika custId sudah ada")
    void createCustomer_duplicateCustId_shouldThrow() {
        when(customerRepository.existsByCustId("EXIST")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(buildCustomer("EXIST", 1L, 1L, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("createCustomer() harus throw jika agent tidak ditemukan")
    void createCustomer_agentNotFound_shouldThrow() {
        when(customerRepository.existsByCustId("C003")).thenReturn(false);
        when(agentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.createCustomer(buildCustomer("C003", 1L, 1L, 99L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Agent not found");
    }

    @Test
    @DisplayName("createCustomer() harus throw jika agent tidak aktif (NONAKTIF)")
    void createCustomer_inactiveAgent_shouldThrow() {
        when(customerRepository.existsByCustId("C004")).thenReturn(false);
        when(agentRepository.findById(5L)).thenReturn(Optional.of(agent(5L, AgentStatus.NONAKTIF)));

        assertThatThrownBy(() -> customerService.createCustomer(buildCustomer("C004", 1L, 1L, 5L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not ACTIVE");
    }

    @Test
    @DisplayName("createCustomer() harus throw jika package tidak ditemukan")
    void createCustomer_packageNotFound_shouldThrow() {
        when(customerRepository.existsByCustId("C005")).thenReturn(false);
        when(agentRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(packageRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.createCustomer(buildCustomer("C005", 77L, 1L, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Package not found");
    }

    @Test
    @DisplayName("createCustomer() harus throw jika address tidak ditemukan")
    void createCustomer_addressNotFound_shouldThrow() {
        when(customerRepository.existsByCustId("C006")).thenReturn(false);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(pkg(1L)));
        when(addressRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.createCustomer(buildCustomer("C006", 1L, 88L, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Address not found");
    }

    // ─────────────────────────────────────────────
    // updateStatus()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() harus mengupdate status customer yang ditemukan")
    void updateStatus_existingCustomer_shouldUpdateStatus() {
        Customer c = new Customer();
        c.setId(1L); c.setStatus(CustomerStatus.ACTIVE);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer updated = customerService.updateStatus(1L, CustomerStatus.ISOLIR);

        assertThat(updated.getStatus()).isEqualTo(CustomerStatus.ISOLIR);
    }

    @Test
    @DisplayName("updateStatus() harus throw jika customer tidak ada")
    void updateStatus_notFound_shouldThrow() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateStatus(999L, CustomerStatus.ACTIVE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found");
    }

    // ─────────────────────────────────────────────
    // deleteCustomer()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("deleteCustomer() berhasil jika ID ada")
    void deleteCustomer_existingId_shouldDelete() {
        Customer c = new Customer();
        c.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(c);
    }

    @Test
    @DisplayName("deleteCustomer() harus throw jika ID tidak ada")
    void deleteCustomer_notFound_shouldThrow() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found");
    }

    // ─────────────────────────────────────────────
    // getAllCustomers()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getAllCustomers() harus mengembalikan semua customer dari repository")
    void getAllCustomers_shouldReturnAll() {
        Customer c1 = new Customer(); c1.setId(1L);
        Customer c2 = new Customer(); c2.setId(2L);
        when(customerRepository.findAllWithRelations()).thenReturn(List.of(c1, c2));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
    }
}

