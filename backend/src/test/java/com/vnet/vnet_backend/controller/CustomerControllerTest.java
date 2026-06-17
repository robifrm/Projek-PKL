package com.vnet.vnet_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.entity.Customer;
import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.service.AnalyticsService;
import com.vnet.vnet_backend.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Functional / API Test — CustomerController
 *
 * Menguji endpoint CustomerController menggunakan MockMvc:
 * createCustomer, getAllCustomers, getCustomerDetail, updateStatus, deleteCustomer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CustomerController Functional Tests")
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private CustomerService customerService;
    @MockitoBean private AnalyticsService analyticsService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = User.builder()
                .name("Admin")
                .username("admin")
                .password(passwordEncoder.encode("pass"))
                .role(Role.SUPER_ADMIN)
                .build();
        userRepository.save(admin);
        adminToken = jwtProvider.generateToken(admin);
    }

    @Test
    @DisplayName("POST /api/customers — sukses membuat customer")
    void createCustomer_success() throws Exception {
        Customer c = new Customer();
        c.setId(1L);
        c.setCustId("C001");
        c.setNama("Budi");

        when(customerService.createCustomer(any(Customer.class))).thenReturn(c);

        mockMvc.perform(post("/api/customers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custId").value("C001"))
                .andExpect(jsonPath("$.nama").value("Budi"));

        verify(analyticsService).invalidateCache();
    }

    @Test
    @DisplayName("GET /api/customers — mendapatkan semua customer")
    void getAllCustomers_success() throws Exception {
        Customer c1 = new Customer(); c1.setId(1L); c1.setNama("User 1");
        Customer c2 = new Customer(); c2.setId(2L); c2.setNama("User 2");

        when(customerService.getAllCustomers()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nama").value("User 1"))
                .andExpect(jsonPath("$[1].nama").value("User 2"));
    }

    @Test
    @DisplayName("GET /api/customers/{custId}/detail — mengembalikan detail customer jika ada")
    void getCustomerDetail_found() throws Exception {
        Customer c = new Customer();
        c.setId(1L);
        c.setCustId("C002");
        c.setNama("Siti");
        c.setStatus(CustomerStatus.ACTIVE);

        when(customerService.findByCustId("C002")).thenReturn(Optional.of(c));

        mockMvc.perform(get("/api/customers/C002/detail")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custId").value("C002"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/customers/{custId}/detail — return 404 jika tidak ada")
    void getCustomerDetail_notFound() throws Exception {
        when(customerService.findByCustId("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/UNKNOWN/detail")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/customers/{id}/status — sukses update status")
    void updateStatus_success() throws Exception {
        Customer c = new Customer();
        c.setId(1L);
        c.setStatus(CustomerStatus.ISOLIR);

        when(customerService.updateStatus(1L, CustomerStatus.ISOLIR)).thenReturn(c);

        mockMvc.perform(put("/api/customers/1/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", "ISOLIR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISOLIR"));

        verify(analyticsService).invalidateCache();
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} — sukses menghapus customer")
    void deleteCustomer_success() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        verify(analyticsService).invalidateCache();
    }
}
