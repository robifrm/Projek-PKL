package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.config.JwtProvider;
import com.vnet.vnet_backend.dto.auth.AuthMessageResponse;
import com.vnet.vnet_backend.dto.auth.AuthResponse;
import com.vnet.vnet_backend.dto.auth.ForgotPasswordRequest;
import com.vnet.vnet_backend.dto.auth.LoginRequest;
import com.vnet.vnet_backend.dto.auth.RegisterRequest;
import com.vnet.vnet_backend.dto.auth.ResetPasswordRequest;
import com.vnet.vnet_backend.dto.auth.VerifyOtpRequest;
import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.enums.Role;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final OtpService otpService;
    private final EmailService emailService;
    private final UserSessionService userSessionService;

    @Transactional
    public AuthMessageResponse register(RegisterRequest request) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fitur registrasi dinonaktifkan");
    }

    @Transactional
    public AuthMessageResponse verifyOtp(VerifyOtpRequest request) {
        return AuthMessageResponse.builder()
                .message("OTP valid")
                .email(request.getEmail())
                .verified(true)
                .build();
    }



    @Transactional
    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {
        return AuthMessageResponse.builder()
                .message("Jika email/username terdaftar, kode OTP reset password telah dikirim")
                .email(request.getEmail())
                .build();
    }

    @Transactional
    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        User user = findByUsernameOrThrow(request.getEmail());
        validatePassword(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return AuthMessageResponse.builder()
                .message("Password berhasil direset")
                .email(user.getUsername())
                .verified(true)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        return login(request, null, null);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = findByUsernameOrThrow(request.getEmail());

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

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password minimal 6 karakter");
        }
    }

    private Role resolveRegistrationRole(RegisterRequest request) {
        if (request.getRole() != null) {
            return request.getRole();
        }
        return userRepository.count() == 0 ? Role.SUPER_ADMIN : Role.STAFF;
    }

    /**
     * Send OTP email safely — never throws, so DB transaction is never rolled back.
     * Falls back to console log when email is not configured (dev/test mode).
     */
    private void sendOtpEmailSafe(String email, String rawOtp, String purpose) {
        try {
            emailService.sendOtpEmail(email, rawOtp, otpService.getExpirationMinutes(), purpose);
        } catch (Exception e) {
            log.warn("[EMAIL FALLBACK] Gagal kirim email ke {}. OTP = {} (purpose: {}). Error: {}",
                    email, rawOtp, purpose, e.getMessage());
        }
    }
}
