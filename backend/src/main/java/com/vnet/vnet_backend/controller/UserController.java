package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody ProfileUpdateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama tidak boleh kosong");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username tidak boleh kosong");
        }

        String newUsername = request.getUsername().trim().toLowerCase();
        if (!user.getUsername().equalsIgnoreCase(newUsername)) {
            Optional<User> duplicate = userRepository.findByUsernameIgnoreCase(newUsername);
            if (duplicate.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah digunakan oleh user lain");
            }
            user.setUsername(newUsername);
        }

        user.setName(request.getName().trim());
        User saved = userRepository.save(user);

        // Regenerate JWT token since username might have changed
        String newToken = jwtProvider.generateToken(saved);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Profil berhasil diperbarui");
        response.put("token", newToken);
        response.put("user", saved);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank() ||
            request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semua kolom password wajib diisi");
        }

        if (request.getNewPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password baru minimal 6 karakter");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password saat ini salah");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Password berhasil diperbarui");
        return ResponseEntity.ok(response);
    }

    @Data
    public static class ProfileUpdateRequest {
        private String name;
        private String username;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }
}
