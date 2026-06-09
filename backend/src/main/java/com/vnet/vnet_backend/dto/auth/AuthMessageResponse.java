package com.vnet.vnet_backend.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthMessageResponse {
    private String message;
    private String email;
    private Long expiresInMinutes;
    private Boolean verified;
}
