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

    private User makeUser(Long id, String username, String name, Role role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setName(name);
        u.setRole(role);
        return u;
    }

    // ─────────────────────────────────────────────
    // generateToken()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("generateToken() harus menghasilkan string JWT yang tidak null/kosong")
    void generateToken_shouldReturnNonNullToken() {
        User user = makeUser(1L, "admin", "Admin VNet", Role.SUPER_ADMIN);
        String token = jwtProvider.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        // JWT memiliki tiga bagian dipisahkan titik
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateToken() menghasilkan token berbeda untuk user berbeda")
    void generateToken_differentUsersProduceDifferentTokens() {
        User admin = makeUser(1L, "admin", "Admin", Role.SUPER_ADMIN);
        User noc   = makeUser(2L, "noc",   "NOC",   Role.STAFF);

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
        User user = makeUser(1L, "user", "User", Role.STAFF);
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

        User user = makeUser(1L, "user", "User", Role.STAFF);
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

        User user = makeUser(1L, "user", "User", Role.STAFF);
        String expiredToken = shortLivedProvider.generateToken(user);

        assertThat(jwtProvider.validateToken(expiredToken)).isFalse();
    }

    // ─────────────────────────────────────────────
    // getUsername()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getUsername() harus mengembalikan username yang sama dengan yang digunakan saat generate")
    void getUsername_shouldReturnCorrectUsername() {
        String username = "manager";
        User user = makeUser(3L, username, "Manager", Role.STAFF);
        String token = jwtProvider.generateToken(user);

        assertThat(jwtProvider.getUsername(token)).isEqualTo(username);
    }
}
