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
        if (seedAdminEnabled && userRepository.count() == 0) {
            if (!StringUtils.hasText(seedAdminPassword)) {
                throw new IllegalStateException("app.seed.admin.password wajib diisi saat seed admin diaktifkan");
            }

            User admin = User.builder()
                    .name(seedAdminName)
                    .username("admin")
                    .password(passwordEncoder.encode(seedAdminPassword))
                    .role(Role.SUPER_ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("[SEEDER] Default Admin seeded (username: admin)");
        }

        // 2. Seed Internet Packages
        if (packageRepository.count() == 0) {
            List<InternetPackage> packages = List.of(
                    new InternetPackage(null, "VNet Lite", 10.0, 150000.0, 45000.0, "Paket internet hemat untuk kebutuhan harian ringan."),
                    new InternetPackage(null, "VNet Family", 30.0, 275000.0, 85000.0, "Paket internet ideal untuk kebutuhan keluarga kecil."),
                    new InternetPackage(null, "VNet Pro", 50.0, 385000.0, 125000.0, "Paket internet cepat untuk kerja dan hiburan tanpa batas."),
                    new InternetPackage(null, "VNet Gamer", 100.0, 550000.0, 180000.0, "Kecepatan maksimal dan latensi rendah untuk para gamer profesional.")
            );
            packageRepository.saveAll(packages);
            System.out.println("[SEEDER] Default Internet Packages seeded");
        }

        // 3. Seed Agents
        /*
        if (agentRepository.count() == 0) {
            List<Agent> agents = List.of(
                    Agent.builder().nama("Budi Santoso").noTelpon("08123456789").area("Bandung Tengah").komisi(25000.0).status(AgentStatus.AKTIF).build(),
                    Agent.builder().nama("Siti Aminah").noTelpon("08234567890").area("Jakarta Selatan").komisi(30000.0).status(AgentStatus.AKTIF).build(),
                    Agent.builder().nama("Joko Widodo").noTelpon("08345678901").area("Surabaya Barat").komisi(35000.0).status(AgentStatus.AKTIF).build(),
                    Agent.builder().nama("Rina Wijaya").noTelpon("08456789012").area("Medan Kota").komisi(40000.0).status(AgentStatus.AKTIF).build()
            );
            agentRepository.saveAll(agents);
            System.out.println("[SEEDER] Default Agents seeded");
        }
        */

        // 4. Seed Addresses
        /*
        if (addressRepository.count() == 0) {
            List<Address> addresses = List.of(
                    Address.builder().alamat("Jl. Kebon Sirih No. 12").rtRw("03/04").kelurahan("Kebon Sirih").kecamatan("Menteng").kota("Jakarta Pusat").kodePos("10340").build(),
                    Address.builder().alamat("Jl. Asia Afrika No. 80").rtRw("05/01").kelurahan("Braga").kecamatan("Sumur Bandung").kota("Bandung").kodePos("40111").build(),
                    Address.builder().alamat("Jl. Tunjungan No. 45").rtRw("02/03").kelurahan("Genteng").kecamatan("Genteng").kota("Surabaya").kodePos("60275").build()
            );
            addressRepository.saveAll(addresses);
            System.out.println("[SEEDER] Default Addresses seeded");
        }
        */
    }
}
