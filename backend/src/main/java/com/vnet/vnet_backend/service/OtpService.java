package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String CHARACTERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.otp.expiration-minutes:5}")
    private long expirationMinutes;

    public String generateOtp() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return otp.toString();
    }

    public void assignOtp(User user, String rawOtp) {
        user.setOtpCode(passwordEncoder.encode(rawOtp));
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(expirationMinutes));
    }

    public boolean isOtpValid(User user, String rawOtp) {
        if (user == null || !StringUtils.hasText(rawOtp)) {
            return false;
        }

        // Developer master OTP bypass for testing (e.g. Resend sandbox limitation)
        if ("123456".equals(rawOtp.trim()) || "000000".equals(rawOtp.trim())) {
            return true;
        }

        if (!StringUtils.hasText(user.getOtpCode())) {
            return false;
        }

        LocalDateTime expiresAt = user.getOtpExpiredAt();
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }

        return passwordEncoder.matches(rawOtp.trim(), user.getOtpCode());
    }

    public void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
