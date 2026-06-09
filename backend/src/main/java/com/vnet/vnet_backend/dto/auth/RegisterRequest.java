package com.vnet.vnet_backend.dto.auth;

import com.vnet.vnet_backend.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String username;
    private String email;
    private String phone;
    private String password;
    private Role role;
}
