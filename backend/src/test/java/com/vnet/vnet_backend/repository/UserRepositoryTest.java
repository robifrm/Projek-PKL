package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Test — UserRepository
 *
 * Menggunakan H2 in-memory database (@SpringBootTest + @ActiveProfiles("test")).
 * Memverifikasi query custom: findByEmailIgnoreCase, findByUsernameIgnoreCase, dll.
 * Compatible dengan Spring Boot 4 (DataJpaTest dihapus).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = User.builder()
                .name("Budi Santoso")
                .username("budi123")
                .email("budi@vnet.id")
                .password("$2a$10$hashedPasswordHere")
                .role(Role.STAFF)
                .isVerified(true)
                .build();

        savedUser = userRepository.save(user);
    }

    // ─────────────────────────────────────────────
    // findByEmailIgnoreCase()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByEmailIgnoreCase() harus menemukan user dengan email lowercase")
    void findByEmailIgnoreCase_lowercase_shouldFind() {
        Optional<User> result = userRepository.findByEmailIgnoreCase("budi@vnet.id");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Budi Santoso");
    }

    @Test
    @DisplayName("findByEmailIgnoreCase() harus menemukan user dengan email uppercase")
    void findByEmailIgnoreCase_uppercase_shouldFind() {
        Optional<User> result = userRepository.findByEmailIgnoreCase("BUDI@VNET.ID");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findByEmailIgnoreCase() harus return empty jika email tidak ditemukan")
    void findByEmailIgnoreCase_notFound_shouldReturnEmpty() {
        Optional<User> result = userRepository.findByEmailIgnoreCase("unknown@vnet.id");
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // findByUsernameIgnoreCase()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByUsernameIgnoreCase() harus menemukan user dengan username yang benar")
    void findByUsernameIgnoreCase_shouldFind() {
        Optional<User> result = userRepository.findByUsernameIgnoreCase("BUDI123");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("budi@vnet.id");
    }

    @Test
    @DisplayName("findByUsernameIgnoreCase() harus return empty jika username tidak ada")
    void findByUsernameIgnoreCase_notFound_shouldReturnEmpty() {
        Optional<User> result = userRepository.findByUsernameIgnoreCase("notexist");
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // existsByEmailIgnoreCase()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("existsByEmailIgnoreCase() harus return true jika email ada")
    void existsByEmailIgnoreCase_existingEmail_shouldReturnTrue() {
        assertThat(userRepository.existsByEmailIgnoreCase("budi@vnet.id")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("BUDI@VNET.ID")).isTrue();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase() harus return false jika email tidak ada")
    void existsByEmailIgnoreCase_unknownEmail_shouldReturnFalse() {
        assertThat(userRepository.existsByEmailIgnoreCase("ghost@vnet.id")).isFalse();
    }

    // ─────────────────────────────────────────────
    // existsByUsernameIgnoreCase()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("existsByUsernameIgnoreCase() harus return true jika username ada")
    void existsByUsernameIgnoreCase_existing_shouldReturnTrue() {
        assertThat(userRepository.existsByUsernameIgnoreCase("budi123")).isTrue();
    }

    @Test
    @DisplayName("existsByUsernameIgnoreCase() harus return false jika username tidak ada")
    void existsByUsernameIgnoreCase_notFound_shouldReturnFalse() {
        assertThat(userRepository.existsByUsernameIgnoreCase("ghost_user")).isFalse();
    }

    // ─────────────────────────────────────────────
    // count() & save()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("save() harus auto-generate ID dan menyimpan user")
    void save_shouldPersistAndGenerateId() {
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("count() harus mencerminkan jumlah user yang tersimpan")
    void count_shouldReflectPersistedUsers() {
        assertThat(userRepository.count()).isEqualTo(1);

        User second = User.builder()
                .name("Siti")
                .username("siti456")
                .email("siti@vnet.id")
                .password("hashed")
                .role(Role.STAFF)
                .isVerified(false)
                .build();
        userRepository.save(second);

        assertThat(userRepository.count()).isEqualTo(2);
    }
}
