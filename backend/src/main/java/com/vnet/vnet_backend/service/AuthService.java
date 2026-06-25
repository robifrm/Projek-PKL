package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.dto.auth.AuthResponse;
import com.vnet.vnet_backend.dto.auth.LoginRequest;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserSessionService userSessionService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        return login(request, null, null);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = findByUsernameOrThrow(request.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username atau password salah");
        }

        String token = jwtProvider.generateToken(user);
        userSessionService.createSession(user, token, userAgent, ipAddress);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getUsername())
                .name(user.getName())
                .username(user.getUsername())
                .phone("")
                .role(user.getRole())
                .isVerified(true)
                .build();
    }

    @Transactional
    public void logout(String token) {
        userSessionService.revokeSessionByToken(token);
    }

    private User findByUsernameOrThrow(String username) {
        String input = requireText(username, "Username wajib diisi").trim().toLowerCase(Locale.ROOT);
        return userRepository.findByUsernameIgnoreCase(input)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }
}
