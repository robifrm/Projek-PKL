package com.vnet.vnet_backend.security;

import com.vnet.vnet_backend.config.JwtFilter;
import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Test — Endpoint Authorization
 *
 * Menguji bahwa setiap endpoint menerapkan kontrol akses yang benar:
 * - Endpoint publik bisa diakses tanpa token
 * - Endpoint terproteksi membutuhkan JWT valid
 * - Role-based access control diterapkan dengan benar
 * - JWT invalid/expired di-reject
 * - SQL injection di param tidak merusak sistem
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Endpoint Tests")
class SecurityEndpointTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private JwtProvider  jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Save users to DB so JwtFilter can load them
        saveUser("admin@test.id", "admin", Role.SUPER_ADMIN);
        saveUser("staff@test.id", "staff", Role.STAFF);

        adminToken   = generateTokenFor(Role.SUPER_ADMIN,   "admin");
        staffToken   = generateTokenFor(Role.STAFF,         "staff");
    }

    private void saveUser(String email, String username, Role role) {
        User u = User.builder()
                .name("Test User")
                .username(username)
                .password(passwordEncoder.encode("password"))
                .role(role)
                .build();
        userRepository.save(u);
    }

    private String generateTokenFor(Role role, String username) {
        User u = new User();
        u.setId(1L); u.setName("Test");
        u.setUsername(username); u.setRole(role);
        u.setPassword("hashed");
        return jwtProvider.generateToken(u);
    }

    // ═══════════════════════════════════════════════
    // PUBLIC ENDPOINTS — harus bisa diakses tanpa token
    // ═══════════════════════════════════════════════

    @Test
    @DisplayName("SEC-01: GET /api/auth/captcha harus bisa diakses tanpa token (public)")
    void publicCaptchaEndpoint_shouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-02: POST /api/auth/login harus bisa diakses tanpa token (public)")
    void publicLoginEndpoint_shouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"x@x.com\",\"password\":\"wrongpass\"}"))
                // Endpoint bisa diakses; service boleh return 401/404 tapi BUKAN 403 dari security
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 403 : "Login endpoint should not block with 403 (security layer should pass it through)";
                });
    }

    // ═══════════════════════════════════════════════
    // PROTECTED ENDPOINTS — harus 401 tanpa token
    // ═══════════════════════════════════════════════

    @Test
    @DisplayName("SEC-03: GET /api/customers tanpa token harus return 401")
    void customersEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-04: GET /api/dashboard tanpa token harus return 401")
    void dashboardEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-05: GET /api/agents tanpa token harus return 401")
    void agentsEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-06: DELETE /api/customers/1 tanpa token harus return 401")
    void deleteCustomer_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════
    // JWT INVALID — harus 401
    // ═══════════════════════════════════════════════

    @Test
    @DisplayName("SEC-07: JWT token palsu/invalid harus return 401")
    void request_withFakeJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer ini.bukan.jwt.valid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-08: JWT token dengan format salah (tanpa Bearer) harus return 401")
    void request_withoutBearerPrefix_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/customers")
                .header("Authorization", adminToken)) // tanpa "Bearer "
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-09: JWT expired harus return 401")
    void request_withExpiredToken_shouldReturn401() throws Exception {
        // Buat token yang sudah expired (-1ms validity)
        JwtProvider shortLived = new JwtProvider();
        org.springframework.test.util.ReflectionTestUtils.setField(shortLived, "jwtSecret",
                "test_secret_key_at_least_32_chars_long_for_hs256");
        org.springframework.test.util.ReflectionTestUtils.setField(shortLived, "tokenValidityInMilliseconds", -1000L);

        User u = new User();
        u.setId(1L); u.setRole(Role.SUPER_ADMIN);
        u.setUsername("x"); u.setName("X"); u.setPassword("h");
        String expiredToken = shortLived.generateToken(u);

        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════
    // ROLE-BASED ACCESS CONTROL
    // ═══════════════════════════════════════════════

    @Test
    @DisplayName("SEC-10: STAFF tidak boleh mengakses DELETE /api/customers — harus 403")
    void deleteCustomer_asStaff_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/customers/1")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-12: STAFF tidak boleh akses /api/agents — harus 403")
    void agents_asStaff_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/agents")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════
    // SQL INJECTION PROTECTION
    // ═══════════════════════════════════════════════

    @Test
    @DisplayName("SEC-13: SQL injection di search param harus aman (tidak error 500)")
    void searchParam_sqlInjection_shouldNotCrash() throws Exception {
        // Payload SQL injection standar
        String sqlInjection = "' OR '1'='1";

        mockMvc.perform(get("/api/customers/page")
                .header("Authorization", "Bearer " + adminToken)
                .param("search", sqlInjection)
                .param("page", "0")
                .param("size", "10"))
                // Harus return 200 dengan hasil aman, BUKAN 500
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 500 : "SQL injection should not cause server error (500)";
                });
    }

    @Test
    @DisplayName("SEC-14: SQL injection di custId path param harus ditangani dengan aman")
    void custIdPath_sqlInjection_shouldNotCrash() throws Exception {
        mockMvc.perform(get("/api/customers/'; DROP TABLE customers; --/detail")
                .header("Authorization", "Bearer " + adminToken))
                // Harus return 404 (customer tidak ada) atau 400, BUKAN 500
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 500 : "SQL injection in path should not cause server error";
                });
    }

    // ═══════════════════════════════════════════════
    // CORS HEADER CHECK
    // ═══════════════════════════════════════════════

    @Test
    @DisplayName("SEC-15: OPTIONS preflight harus return 200 (CORS preflight support)")
    void corsPreflightRequest_shouldReturn200() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:5174")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk());
    }
}
