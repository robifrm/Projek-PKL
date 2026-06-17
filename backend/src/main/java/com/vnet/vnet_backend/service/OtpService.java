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
        // OTP is disabled, do nothing
    }

    public boolean isOtpValid(User user, String rawOtp) {
        if (user == null || !org.springframework.util.StringUtils.hasText(rawOtp)) {
            return false;
        }
        return true;
    }

    public void clearOtp(User user) {
        // OTP is disabled, do nothing
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
