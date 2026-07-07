package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.*;
import com.vnet.vnet_backend.repository.CustomerRegistrationRepository;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationService {

    private final CustomerRegistrationRepository repository;
    private final InternetPackageRepository packageRepository;
    private final CustomerService customerService;

    @Transactional
    public CustomerRegistration submitRegistration(CustomerRegistration registration) {
        if (registration.getPkg() == null || registration.getPkg().getId() == null) {
            throw new RuntimeException("Paket internet wajib dipilih!");
        }

        InternetPackage pkg = packageRepository.findById(registration.getPkg().getId())
                .orElseThrow(() -> new RuntimeException("Paket internet tidak ditemukan!"));

        registration.setPkg(pkg);
        registration.setWaktu(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.MASUK);

        // Auto-generate custId starting around VN33021
        if (registration.getCustId() == null || registration.getCustId().trim().isEmpty()) {
            long count = repository.count() + 33021;
            String nextId = "VN" + count;
            while (repository.existsByCustId(nextId)) {
                count++;
                nextId = "VN" + count;
            }
            registration.setCustId(nextId);
        }

        return repository.save(registration);
    }

    public List<CustomerRegistration> getRegistrations(RegistrationStatus status, String search) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return repository.searchRegistrations(status, querySearch);
    }

    public CustomerRegistration getRegistrationById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registrasi tidak ditemukan!"));
    }

    @Transactional
    public CustomerRegistration updateStatus(Long id, RegistrationStatus newStatus, LocalDateTime tanggalJadwal) {
        CustomerRegistration reg = getRegistrationById(id);
        RegistrationStatus oldStatus = reg.getStatus();

        if (oldStatus == newStatus && (tanggalJadwal == null || tanggalJadwal.equals(reg.getTanggalJadwal()))) {
            return reg;
        }

        reg.setStatus(newStatus);
        if (tanggalJadwal != null) {
            reg.setTanggalJadwal(tanggalJadwal);
        }

        CustomerRegistration saved = repository.save(reg);

        // Jika status diubah menjadi SELESAI, otomatis buat Customer baru
        if (newStatus == RegistrationStatus.SELESAI && oldStatus != RegistrationStatus.SELESAI) {
            Customer customer = new Customer();
            customer.setCustId(reg.getCustId()); // Keep the same VN ID generated earlier
            customer.setNama(reg.getFirstName() + " " + reg.getLastName());
            customer.setEmail(reg.getEmail());
            customer.setNoTelpon(reg.getNomorSelulerUtama());
            customer.setTanggalRegistrasi(reg.getWaktu() != null ? reg.getWaktu().toLocalDate() : LocalDate.now());
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setIsolir(false);
            customer.setPkg(reg.getPkg());
            customer.setKoordinat(reg.getKoordinat());
            customer.setAfiliator(reg.getAfiliator());

            // Build Address
            Address address = new Address();
            address.setAlamat(reg.getAlamatDetail() != null && !reg.getAlamatDetail().trim().isEmpty() ? reg.getAlamatDetail() : "-");
            address.setKelurahan(reg.getKelurahan() != null ? reg.getKelurahan() : "");
            address.setKecamatan(reg.getKecamatan() != null ? reg.getKecamatan() : "");
            
            String kota = reg.getKota() != null && !reg.getKota().trim().isEmpty() ? reg.getKota() : "Sukabumi";
            address.setKota(kota);
            
            // Format RT/RW (e.g. 03/04)
            String rtStr = reg.getRt() != null ? reg.getRt().trim() : "";
            String rwStr = reg.getRw() != null ? reg.getRw().trim() : "";
            String rtRwCombined = "";
            if (!rtStr.isEmpty() || !rwStr.isEmpty()) {
                rtRwCombined = rtStr + "/" + rwStr;
            }
            address.setRtRw(rtRwCombined);
            address.setKodePos(reg.getKodePos() != null ? reg.getKodePos() : "");

            customer.setAddress(address);

            // Auto-generate prices from package if null
            if (reg.getHarga() != null) {
                customer.setPrice(reg.getHarga());
                if (reg.getPkg() != null) {
                    double cost = reg.getPkg().getPrice() - reg.getPkg().getProfit();
                    customer.setProfit(reg.getHarga() - cost);
                } else {
                    customer.setProfit(reg.getHarga());
                }
            } else if (reg.getPkg() != null) {
                customer.setPrice(reg.getPkg().getPrice());
                customer.setProfit(reg.getPkg().getProfit());
            }
            customer.setBiayaPasang(0.0);

            customerService.createCustomer(customer);
        }

        return saved;
    }

    @Transactional
    public CustomerRegistration updateBaSetup(Long id, java.util.Map<String, Object> payload) {
        CustomerRegistration reg = getRegistrationById(id);
        
        if (payload.containsKey("custId")) {
            reg.setCustId((String) payload.get("custId"));
        }
        if (payload.containsKey("tanggalJadwal")) {
            String val = (String) payload.get("tanggalJadwal");
            if (val != null && !val.trim().isEmpty()) {
                try {
                    // Try parsing as ISO LocalDateTime or space-separated
                    String cleanVal = val.replace(" ", "T");
                    if (cleanVal.length() == 16) {
                        cleanVal += ":00"; // Add seconds if missing
                    }
                    reg.setTanggalJadwal(LocalDateTime.parse(cleanVal));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                reg.setTanggalJadwal(null);
            }
        }
        if (payload.containsKey("harga")) {
            Object hVal = payload.get("harga");
            if (hVal instanceof Number) {
                reg.setHarga(((Number) hVal).doubleValue());
            } else if (hVal instanceof String) {
                try {
                    reg.setHarga(Double.parseDouble((String) hVal));
                } catch (Exception e) {}
            }
        }
        if (payload.containsKey("noteRequest")) {
            reg.setNoteRequest((String) payload.get("noteRequest"));
        }
        if (payload.containsKey("noteTeknisi")) {
            reg.setNoteTeknisi((String) payload.get("noteTeknisi"));
        }
        
        return repository.save(reg);
    }

    @Transactional
    public CustomerRegistration updateRegistration(Long id, CustomerRegistration updatedData) {
        CustomerRegistration reg = getRegistrationById(id);

        reg.setFirstName(updatedData.getFirstName());
        reg.setLastName(updatedData.getLastName());
        reg.setEmail(updatedData.getEmail());
        reg.setJenisIdentitas(updatedData.getJenisIdentitas());
        reg.setNomorIdentitas(updatedData.getNomorIdentitas());
        reg.setNomorSelulerUtama(updatedData.getNomorSelulerUtama());
        reg.setNomorCadangan(updatedData.getNomorCadangan());
        reg.setAlamatDetail(updatedData.getAlamatDetail());
        reg.setRt(updatedData.getRt());
        reg.setRw(updatedData.getRw());
        reg.setKelurahan(updatedData.getKelurahan());
        reg.setKecamatan(updatedData.getKecamatan());
        reg.setKota(updatedData.getKota());
        reg.setProvinsi(updatedData.getProvinsi());
        reg.setKodePos(updatedData.getKodePos());

        if (updatedData.getPkg() != null && updatedData.getPkg().getId() != null) {
            InternetPackage pkg = packageRepository.findById(updatedData.getPkg().getId())
                    .orElseThrow(() -> new RuntimeException("Paket internet tidak ditemukan!"));
            reg.setPkg(pkg);
        }

        reg.setKoordinat(updatedData.getKoordinat());
        reg.setAfiliator(updatedData.getAfiliator());

        return repository.save(reg);
    }

    @Transactional
    public void deleteRegistration(Long id) {
        CustomerRegistration reg = getRegistrationById(id);
        repository.delete(reg);
    }
}
