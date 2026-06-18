package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.repository.AgentRepository;
import com.vnet.vnet_backend.repository.UserSessionRepository;
import com.vnet.vnet_backend.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserSessionService userSessionService;
    private final AgentRepository agentRepository;
    private final UserSessionRepository userSessionRepository;

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody ProfileUpdateRequest request, HttpServletRequest servletRequest) {
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

        // Update active session token in DB
        String oldToken = null;
        String authHeader = servletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            oldToken = authHeader.substring(7);
        }
        userSessionService.updateSessionToken(oldToken, newToken);

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

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("username", u.getUsername());
            map.put("role", u.getRole());
            map.put("createdAt", u.getCreatedAt());
            if (u.getAgent() != null) {
                Map<String, Object> agentMap = new LinkedHashMap<>();
                agentMap.put("id", u.getAgent().getId());
                agentMap.put("nama", u.getAgent().getNama());
                map.put("agent", agentMap);
            }
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama tidak boleh kosong");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username tidak boleh kosong");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password tidak boleh kosong");
        }
        if (request.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password minimal 6 karakter");
        }
        if (request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role wajib diisi");
        }

        String username = request.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah terdaftar");
        }

        User.UserBuilder userBuilder = User.builder()
                .name(request.getName().trim())
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole());

        if (request.getRole() == Role.AGENT) {
            if (request.getAgentId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agent wajib diisi jika role adalah AGENT");
            }
            Agent agent = agentRepository.findById(request.getAgentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent tidak ditemukan"));
            userBuilder.agent(agent);
        }

        User saved = userRepository.save(userBuilder.build());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", saved.getId());
        map.put("name", saved.getName());
        map.put("username", saved.getUsername());
        map.put("role", saved.getRole());
        map.put("createdAt", saved.getCreatedAt());
        if (saved.getAgent() != null) {
            Map<String, Object> agentMap = new LinkedHashMap<>();
            agentMap.put("id", saved.getAgent().getId());
            agentMap.put("nama", saved.getAgent().getNama());
            map.put("agent", agentMap);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        if (currentUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anda tidak dapat menghapus akun Anda sendiri");
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User yang akan dihapus tidak ditemukan"));

        userSessionRepository.deleteByUserId(id);
        userRepository.delete(targetUser);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "User berhasil dihapus");
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

    @Data
    public static class CreateUserRequest {
        private String name;
        private String username;
        private String password;
        private Role role;
        private Long agentId;
    }
}
