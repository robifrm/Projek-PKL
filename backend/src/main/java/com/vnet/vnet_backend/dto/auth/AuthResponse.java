package com.vnet.vnet_backend.dto.auth;

import com.vnet.vnet_backend.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long id;
    private String token;
    private String name;
    private String username;
    private Role role;
}
