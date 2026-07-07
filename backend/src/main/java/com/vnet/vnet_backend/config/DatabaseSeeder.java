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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InternetPackageRepository packageRepository;
    private final AgentRepository agentRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRegistrationRepository customerRegistrationRepository;
    private final BaAktivasiRepository baAktivasiRepository;
    private final CustomerRepository customerRepository;

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

        // 3. Seed Internet Packages if none exist
        if (packageRepository.count() == 0) {
            packageRepository.save(new InternetPackage(null, "VNet 10 Mbps", 10.0, 150000.0, 50000.0, "Paket Internet 10 Mbps"));
            packageRepository.save(new InternetPackage(null, "VNet 20 Mbps", 20.0, 200000.0, 70000.0, "Paket Internet 20 Mbps"));
            packageRepository.save(new InternetPackage(null, "VNet 30 Mbps", 30.0, 250000.0, 90000.0, "Paket Internet 30 Mbps"));
            System.out.println("[SEEDER] Default Internet Packages seeded");
        }

        // 4. Seed Agents if none exist
        if (agentRepository.count() == 0) {
            Agent agent1 = new Agent();
            agent1.setNama("Agen Sukabumi");
            agent1.setStatus(AgentStatus.AKTIF);
            agentRepository.save(agent1);

            Agent agent2 = new Agent();
            agent2.setNama("Agen Cisaat");
            agent2.setStatus(AgentStatus.AKTIF);
            agentRepository.save(agent2);
            System.out.println("[SEEDER] Default Agents seeded");
        }

        // 5. Seed 2 dummy records for each RegistrationStatus if none exist
        if (customerRegistrationRepository.findByCustId("VN33021").isEmpty()) {
            baAktivasiRepository.deleteAll();
            customerRepository.deleteAll();
            customerRegistrationRepository.deleteAll();

            List<InternetPackage> packages = packageRepository.findAll();
            InternetPackage pkg = packages.isEmpty() ? null : packages.get(0);
            List<Agent> agents = agentRepository.findAll();
            Agent agent = agents.isEmpty() ? null : agents.get(0);

            long vnCounter = 33021;
            
            // Loop over all statuses
            for (RegistrationStatus status : RegistrationStatus.values()) {
                for (int i = 1; i <= 2; i++) {
                    String suffix = status.name().toLowerCase() + "_" + i;
                    String custId = "VN" + vnCounter++;

                    CustomerRegistration reg = new CustomerRegistration();
                    reg.setCustId(custId);
                    reg.setFirstName("Customer");
                    reg.setLastName(suffix.toUpperCase());
                    reg.setEmail(suffix + "@gmail.com");
                    reg.setJenisIdentitas("KTP");
                    reg.setNomorIdentitas("320101" + (100000 + vnCounter) + "0001");
                    reg.setNomorSelulerUtama("0812" + (10000000 + vnCounter));
                    reg.setAlamatDetail("Jl. Raya Sukabumi No. " + i);
                    reg.setRt("0" + i);
                    reg.setRw("0" + (i + 1));
                    reg.setKelurahan("Cikole");
                    reg.setKecamatan("Cikole");
                    reg.setKota("Sukabumi");
                    reg.setProvinsi("Jawa Barat");
                    reg.setKodePos("43111");
                    reg.setStatus(status);
                    reg.setPkg(pkg);
                    reg.setHarga(pkg != null ? pkg.getPrice() : 150000.0);
                    reg.setWaktu(LocalDateTime.now().minusDays(10 - i));
                    reg.setTanggalJadwal(LocalDateTime.now().minusDays(5 - i));
                    reg.setKoordinat("-6.921" + i + ", 106.92" + i);
                    
                    CustomerRegistration savedReg = customerRegistrationRepository.save(reg);

                    // Seed BaAktivasi for the registration
                    BaAktivasi ba = BaAktivasi.builder()
                            .registration(savedReg)
                            .namaTeknisi("Teknisi Lapangan 1")
                            .catatanTeknisi("Instalasi berjalan lancar untuk " + suffix)
                            .qtyOnt(1)
                            .snOnt("ONT" + vnCounter + "XYZ")
                            .qtyRouter(1)
                            .snRouter("RT" + vnCounter + "ABC")
                            .qtyKabel(150)
                            .qtyRoset(1)
                            .qtyAksesoris(2)
                            .panjangKabel(150)
                            .kodeOdp("ODP-SMN-" + i)
                            .portOdp(i)
                            .portOdpTerpakai(String.valueOf(i))
                            .pop("POP Sukabumi")
                            .olt("OLT-GP-" + i)
                            .rosset("1")
                            .pigtail(1)
                            .patchcore(1)
                            .splicing(1)
                            .redamanOutputKabel("-19." + i)
                            .jumlahBracket(3)
                            .jumlahSClamp(2)
                            .statusAktivasi(status == RegistrationStatus.SELESAI ? "Done" : "Pending")
                            .build();
                    baAktivasiRepository.save(ba);

                    // If status is SELESAI, also seed a Customer record so it is fully functional
                    if (status == RegistrationStatus.SELESAI) {
                        Address address = new Address();
                        address.setAlamat(savedReg.getAlamatDetail());
                        address.setKelurahan(savedReg.getKelurahan());
                        address.setKecamatan(savedReg.getKecamatan());
                        address.setKota(savedReg.getKota());
                        address.setRtRw(savedReg.getRt() + "/" + savedReg.getRw());
                        address.setKodePos(savedReg.getKodePos());
                        Address savedAddress = addressRepository.save(address);

                        Customer customer = new Customer();
                        customer.setCustId(savedReg.getCustId());
                        customer.setNama(savedReg.getFirstName() + " " + savedReg.getLastName());
                        customer.setEmail(savedReg.getEmail());
                        customer.setNoTelpon(savedReg.getNomorSelulerUtama());
                        customer.setTanggalRegistrasi(savedReg.getWaktu().toLocalDate());
                        customer.setTanggalAktivasi(LocalDate.now());
                        customer.setStatus(CustomerStatus.ACTIVE);
                        customer.setIsolir(false);
                        customer.setPkg(pkg);
                        customer.setPrice(pkg != null ? pkg.getPrice() : 150000.0);
                        customer.setProfit(pkg != null ? pkg.getProfit() : 50000.0);
                        customer.setBiayaPasang(0.0);
                        customer.setAddress(savedAddress);
                        customer.setKoordinat(savedReg.getKoordinat());
                        customer.setAgent(agent);
                        customerRepository.save(customer);
                    }
                }
            }
            System.out.println("[SEEDER] 2 Dummy registrations for each status successfully seeded");
        }
    }
}
