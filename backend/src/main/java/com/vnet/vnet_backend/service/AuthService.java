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

    @Transactional
    public AuthMessageResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        validatePassword(request.getPassword());

        String name = requireText(request.getName(), "Nama wajib diisi");
        String username = requireText(request.getUsername(), "Username wajib diisi").trim().toLowerCase(Locale.ROOT);
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;

        // Check email conflicts
        Optional<User> existingUserByEmail = userRepository.findByEmailIgnoreCase(email);
        if (existingUserByEmail.filter(user -> Boolean.TRUE.equals(user.getIsVerified())).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar");
        }

        // Check username conflicts
        Optional<User> existingUserByUsername = userRepository.findByUsernameIgnoreCase(username);
        if (existingUserByUsername.filter(user -> Boolean.TRUE.equals(user.getIsVerified())).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah digunakan");
        }

        // Check if both exist but belong to different unverified users
        if (existingUserByEmail.isPresent() && existingUserByUsername.isPresent()) {
            if (!existingUserByEmail.get().getId().equals(existingUserByUsername.get().getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username atau Email sudah terdaftar pada proses verifikasi lain");
            }
        }

        User user = existingUserByEmail.orElseGet(() -> 
            existingUserByUsername.orElseGet(User::new)
        );

        user.setName(name.trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(resolveRegistrationRole(request));
        user.setIsVerified(false);

        String rawOtp = otpService.generateOtp();
        otpService.assignOtp(user, rawOtp);
        userRepository.save(user);

        // Send email AFTER commit — do not let email failure rollback user save
        sendOtpEmailSafe(email, rawOtp, "verifikasi email");

        return AuthMessageResponse.builder()
                .message("Kode OTP registrasi telah dikirim ke email")
                .email(email)
                .expiresInMinutes(otpService.getExpirationMinutes())
                .verified(false)
                .build();
    }

    @Transactional
    public AuthMessageResponse verifyOtp(VerifyOtpRequest request) {
        User user = findByEmailOrThrow(request.getEmail());
        if (!otpService.isOtpValid(user, request.getOtpCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP tidak valid atau sudah kedaluwarsa");
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            user.setIsVerified(true);
            otpService.clearOtp(user);
            userRepository.save(user);

            return AuthMessageResponse.builder()
                    .message("Email berhasil diverifikasi")
                    .email(user.getEmail())
                    .verified(true)
                    .build();
        }

        return AuthMessageResponse.builder()
                .message("OTP valid")
                .email(user.getEmail())
                .verified(true)
                .build();
    }

    @Transactional
    public AuthMessageResponse resendOtp(String emailOrUsername) {
        User user = findByEmailOrThrow(emailOrUsername);

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Akun sudah terverifikasi");
        }

        String rawOtp = otpService.generateOtp();
        otpService.assignOtp(user, rawOtp);
        userRepository.save(user);

        // Send email AFTER commit
        sendOtpEmailSafe(user.getEmail(), rawOtp, "verifikasi email");

        return AuthMessageResponse.builder()
                .message("Kode OTP baru telah dikirim ke email")
                .email(user.getEmail())
                .expiresInMinutes(otpService.getExpirationMinutes())
                .verified(false)
                .build();
    }

    @Transactional
    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {
        String emailOrUsername = request.getEmail();
        String[] rawOtpHolder = new String[1];
        String[] emailHolder = new String[1];

        Optional<User> userOpt;
        String input = requireText(emailOrUsername, "Email atau Username wajib diisi").trim().toLowerCase(Locale.ROOT);
        if (input.contains("@")) {
            userOpt = userRepository.findByEmailIgnoreCase(input);
        } else {
            userOpt = userRepository.findByUsernameIgnoreCase(input);
        }

        userOpt.ifPresent(user -> {
            String rawOtp = otpService.generateOtp();
            otpService.assignOtp(user, rawOtp);
            userRepository.save(user);
            rawOtpHolder[0] = rawOtp;
            emailHolder[0] = user.getEmail();
        });

        // Send email outside transaction
        if (rawOtpHolder[0] != null && emailHolder[0] != null) {
            sendOtpEmailSafe(emailHolder[0], rawOtpHolder[0], "reset password");
        }

        return AuthMessageResponse.builder()
                .message("Jika email/username terdaftar, kode OTP reset password telah dikirim")
                .email(emailHolder[0] != null ? emailHolder[0] : emailOrUsername)
                .expiresInMinutes(otpService.getExpirationMinutes())
                .build();
    }

    @Transactional
    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        User user = findByEmailOrThrow(request.getEmail());
        validatePassword(request.getNewPassword());

        if (!otpService.isOtpValid(user, request.getOtpCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP tidak valid atau sudah kedaluwarsa");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        otpService.clearOtp(user);
        userRepository.save(user);

        return AuthMessageResponse.builder()
                .message("Password berhasil direset")
                .email(user.getEmail())
                .verified(user.getIsVerified())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = findByEmailOrThrow(request.getEmail());

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email belum diverifikasi");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah");
        }

        return AuthResponse.builder()
                .id(user.getId())
                .token(jwtProvider.generateToken(user))
                .email(user.getEmail())
                .name(user.getName())
                .username(user.getUsername())
                .phone(user.getPhone())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .build();
    }

    private User findByEmailOrThrow(String emailOrUsername) {
        String input = requireText(emailOrUsername, "Email atau Username wajib diisi").trim().toLowerCase(Locale.ROOT);
        Optional<User> userOpt;
        if (input.contains("@")) {
            userOpt = userRepository.findByEmailIgnoreCase(input);
        } else {
            userOpt = userRepository.findByUsernameIgnoreCase(input);
        }
        return userOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
    }

    private String normalizeEmail(String email) {
        String value = requireText(email, "Email wajib diisi").trim().toLowerCase(Locale.ROOT);
        if (!value.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format email tidak valid");
        }
        return value;
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password minimal 6 karakter");
        }
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
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
