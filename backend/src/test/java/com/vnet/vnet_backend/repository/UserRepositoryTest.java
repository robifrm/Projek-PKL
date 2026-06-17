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
                .password("$2a$10$hashedPasswordHere")
                .role(Role.STAFF)
                .build();

        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("findByUsernameIgnoreCase() harus menemukan user dengan username yang benar")
    void findByUsernameIgnoreCase_shouldFind() {
        Optional<User> result = userRepository.findByUsernameIgnoreCase("BUDI123");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Budi Santoso");
    }

    @Test
    @DisplayName("findByUsernameIgnoreCase() harus return empty jika username tidak ada")
    void findByUsernameIgnoreCase_notFound_shouldReturnEmpty() {
        Optional<User> result = userRepository.findByUsernameIgnoreCase("notexist");
        assertThat(result).isEmpty();
    }

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
                .password("hashed")
                .role(Role.STAFF)
                .build();
        userRepository.save(second);

        assertThat(userRepository.count()).isEqualTo(2);
    }
}
