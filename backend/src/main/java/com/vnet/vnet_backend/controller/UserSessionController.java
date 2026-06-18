package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.repository.UserRepository;
import com.vnet.vnet_backend.service.UserSessionService;
import com.vnet.vnet_backend.service.UserSessionService.UserSessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserSessionController {

    private final UserSessionService userSessionService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserSessionResponse>> getSessions(HttpServletRequest request) {
        User user = getCurrentUser();
        String token = extractToken(request);
        return ResponseEntity.ok(userSessionService.getActiveSessions(user, token));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> revokeSession(@PathVariable Long id) {
        User user = getCurrentUser();
        userSessionService.revokeSession(id, user);
        
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Sesi berhasil dikeluarkan");
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
