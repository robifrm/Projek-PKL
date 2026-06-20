package com.vnet.vnet_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendOtpEmail(String toEmail, String otpCode, long expiresInMinutes, String purpose) {
        log.info("[OTP DUMMY EMAIL] To: {}, Code: {}, Purpose: {}, Expires in: {} minutes", 
                 toEmail, otpCode, purpose, expiresInMinutes);
    }
}
