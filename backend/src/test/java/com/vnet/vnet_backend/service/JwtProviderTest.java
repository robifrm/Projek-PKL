package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.config.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Test — JwtProvider
 *
 * Menguji JWT token generation, validasi, dan extraction tanpa Spring context.
 */
@DisplayName("JwtProvider Unit Tests")
class JwtProviderTest {

    private JwtProvider jwtProvider;

    // Secret minimal 32 karakter untuk HS256
    private static final String TEST_SECRET = "test_secret_key_at_least_32_chars_long_for_hs256";
    private static final long TOKEN_VALIDITY_MS = 3_600_000L; // 1 jam

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtProvider, "tokenValidityInMilliseconds", TOKEN_VALIDITY_MS);
    }

    private User makeUser(Long id, String email, String name, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setName(name);
        u.setRole(role);
        u.setIsVerified(true);
        return u;
    }

    // ─────────────────────────────────────────────
    // generateToken()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("generateToken() harus menghasilkan string JWT yang tidak null/kosong")
    void generateToken_shouldReturnNonNullToken() {
        User user = makeUser(1L, "admin@vnet.id", "Admin VNet", Role.ADMIN);
        String token = jwtProvider.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        // JWT memiliki tiga bagian dipisahkan titik
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateToken() menghasilkan token berbeda untuk user berbeda")
    void generateToken_differentUsersProduceDifferentTokens() {
        User admin = makeUser(1L, "admin@vnet.id", "Admin", Role.ADMIN);
        User noc   = makeUser(2L, "noc@vnet.id",   "NOC",   Role.NOC);

        String tokenAdmin = jwtProvider.generateToken(admin);
        String tokenNoc   = jwtProvider.generateToken(noc);

        assertThat(tokenAdmin).isNotEqualTo(tokenNoc);
    }

    // ─────────────────────────────────────────────
    // validateToken()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("validateToken() harus return true untuk token valid yang baru dibuat")
    void validateToken_withValidToken_shouldReturnTrue() {
        User user = makeUser(1L, "user@vnet.id", "User", Role.NOC);
        String token = jwtProvider.generateToken(user);

        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken() harus return false untuk string acak bukan JWT")
    void validateToken_withGarbageString_shouldReturnFalse() {
        assertThat(jwtProvider.validateToken("bukan.jwt.valid")).isFalse();
        assertThat(jwtProvider.validateToken("")).isFalse();
        assertThat(jwtProvider.validateToken("eyJhbGciOiJIUzI1NiJ9.fake.sig")).isFalse();
    }

    @Test
    @DisplayName("validateToken() harus return false untuk token dengan secret berbeda")
    void validateToken_withWrongSecret_shouldReturnFalse() {
        // Buat provider lain dengan secret berbeda
        JwtProvider otherProvider = new JwtProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret", "different_secret_key_at_least_32chars_");
        ReflectionTestUtils.setField(otherProvider, "tokenValidityInMilliseconds", TOKEN_VALIDITY_MS);

        User user = makeUser(1L, "user@vnet.id", "User", Role.NOC);
        String tokenFromOther = otherProvider.generateToken(user);

        // Provider asli tidak bisa validasi token dari secret lain
        assertThat(jwtProvider.validateToken(tokenFromOther)).isFalse();
    }

    @Test
    @DisplayName("validateToken() harus return false untuk token yang sudah expired")
    void validateToken_withExpiredToken_shouldReturnFalse() {
        JwtProvider shortLivedProvider = new JwtProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedProvider, "tokenValidityInMilliseconds", -1000L); // sudah expired

        User user = makeUser(1L, "user@vnet.id", "User", Role.NOC);
        String expiredToken = shortLivedProvider.generateToken(user);

        assertThat(jwtProvider.validateToken(expiredToken)).isFalse();
    }

    // ─────────────────────────────────────────────
    // getEmail()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getEmail() harus mengembalikan email yang sama dengan yang digunakan saat generate")
    void getEmail_shouldReturnCorrectEmail() {
        String email = "manager@vnet.id";
        User user = makeUser(3L, email, "Manager", Role.MANAGER);
        String token = jwtProvider.generateToken(user);

        assertThat(jwtProvider.getEmail(token)).isEqualTo(email);
    }
}
