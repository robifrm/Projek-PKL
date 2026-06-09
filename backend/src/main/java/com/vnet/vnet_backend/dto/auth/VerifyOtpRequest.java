package com.vnet.vnet_backend.dto.auth;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otpCode;
}
