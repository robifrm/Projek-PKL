package com.vnet.vnet_backend.config;

import com.vnet.vnet_backend.entity.*;
import com.vnet.vnet_backend.enums.Role;
import com.vnet.vnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InternetPackageRepository packageRepository;
    private final AgentRepository agentRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.enabled:false}")
    private boolean seedAdminEnabled;

    @Value("${app.seed.admin.name:Super Admin VNet}")
    private String seedAdminName;

    @Value("${app.seed.admin.email:admin@victorynetwork.id}")
    private String seedAdminEmail;

    @Value("${app.seed.admin.password:}")
    private String seedAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed User Admin
        if (seedAdminEnabled && userRepository.findByUsernameIgnoreCase(seedAdminEmail).isEmpty()) {
            if (!StringUtils.hasText(seedAdminPassword)) {
                throw new IllegalStateException("app.seed.admin.password wajib diisi saat seed admin diaktifkan");
            }

            User admin = User.builder()
                    .name(seedAdminName)
                    .username(seedAdminEmail)
                    .password(passwordEncoder.encode(seedAdminPassword))
                    .role(Role.SUPER_ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("[SEEDER] Default Admin seeded (username: " + seedAdminEmail + ")");
        }

        // 2. Seed default Technician if none exists
        if (userRepository.findByUsernameIgnoreCase("teknisi1").isEmpty()) {
            User technician = User.builder()
                    .name("Teknisi Lapangan 1")
                    .username("teknisi1")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.TECHNICIAN)
                    .build();
            userRepository.save(technician);
            System.out.println("[SEEDER] Default Technician seeded (username: teknisi1, password: password123)");
        }
    }
}
