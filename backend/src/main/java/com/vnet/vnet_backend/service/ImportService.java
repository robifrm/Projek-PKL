package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.Address;
import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.entity.AgentStatus;
import com.vnet.vnet_backend.entity.Customer;
import com.vnet.vnet_backend.entity.CustomerStatus;
import com.vnet.vnet_backend.entity.InternetPackage;
import com.vnet.vnet_backend.entity.ImportLog;
import com.vnet.vnet_backend.entity.UploadSession;
import com.vnet.vnet_backend.enums.ActionType;
import com.vnet.vnet_backend.enums.ImportLogStatus;
import com.vnet.vnet_backend.enums.ImportStatus;
import com.vnet.vnet_backend.repository.AddressRepository;
import com.vnet.vnet_backend.repository.AgentRepository;
import com.vnet.vnet_backend.repository.CustomerRepository;
import com.vnet.vnet_backend.repository.InternetPackageRepository;
import com.vnet.vnet_backend.repository.ImportLogRepository;
import com.vnet.vnet_backend.repository.UploadSessionRepository;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImportService {

    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final InternetPackageRepository internetPackageRepository;
    private final AddressRepository addressRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final ImportLogRepository importLogRepository;

    public ImportService(
            CustomerRepository customerRepository,
            AgentRepository agentRepository,
            InternetPackageRepository internetPackageRepository,
            AddressRepository addressRepository,
            UploadSessionRepository uploadSessionRepository,
            ImportLogRepository importLogRepository
    ) {
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
        this.internetPackageRepository = internetPackageRepository;
        this.addressRepository = addressRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.importLogRepository = importLogRepository;
    }

    public Map<String, Object> processImport(String fileName, List<Map<String, Object>> rows) {

        int success = 0;
        int failed = 0;

        // 1. Upload Session
        UploadSession session = UploadSession.builder()
                .fileName(fileName)
                .uploadTime(LocalDateTime.now())
                .totalData(rows.size())
                .totalValid(0)
                .totalError(0)
                .status(ImportStatus.UPLOADED)
                .build();

        session = uploadSessionRepository.save(session);

        // 2. Loop setiap row
        for (Map<String, Object> row : rows) {

            String validationStatus =
                    String.valueOf(row.get("validationStatus"));

            String actionType =
                    String.valueOf(row.get("actionType"));

            String custId = first(row, "Cust ID", "Customer ID", "custId", "cust_id");

            // Jika row error, skip
            if ("ERROR".equalsIgnoreCase(validationStatus)) {
                failed++;

                saveLog(
                        session,
                        custId,
                        ImportLogStatus.FAILED,
                        ActionType.SKIP,
                        String.valueOf(row.get("errorMessage"))
                );

                continue;
            }

            try {
                // Mapping row ke Customer
                Customer customer = mapRowToCustomer(row);

                // INSERT OR UPDATE
                customerRepository.save(customer);

                success++;

                ActionType parsedActionType;
                try {
                    parsedActionType = ActionType.valueOf(actionType.toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    parsedActionType = ActionType.SKIP;
                }
                saveLog(
                        session,
                        custId,
                        ImportLogStatus.SUCCESS,
                        parsedActionType,
                        "Import berhasil"
                );

            } catch (Exception e) {
                failed++;

                saveLog(
                        session,
                        custId,
                        ImportLogStatus.FAILED,
                        ActionType.SKIP,
                        e.getMessage()
                );
            }
        }

        // 3. Update UploadSession
        session.setTotalValid(success);
        session.setTotalError(failed);
        session.setStatus(
                failed > 0
                        ? ImportStatus.COMPLETED
                        : ImportStatus.COMPLETED
        );

        uploadSessionRepository.save(session);

        // 4. Return summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("uploadSessionId", session.getId());
        summary.put("totalData", rows.size());
        summary.put("success", success);
        summary.put("failed", failed);

        return summary;
    }

    public Map<String, Object> getImportStats() {
        List<UploadSession> sessions = uploadSessionRepository.findAll();
        Optional<UploadSession> latest = sessions.stream()
                .filter(session -> session.getUploadTime() != null)
                .max(Comparator.comparing(UploadSession::getUploadTime));

        int totalRows = sessions.stream().mapToInt(session -> Optional.ofNullable(session.getTotalData()).orElse(0)).sum();
        int totalValid = sessions.stream().mapToInt(session -> Optional.ofNullable(session.getTotalValid()).orElse(0)).sum();
        int totalError = sessions.stream().mapToInt(session -> Optional.ofNullable(session.getTotalError()).orElse(0)).sum();
        long activeQueue = sessions.stream()
                .filter(session -> session.getStatus() != null)
                .filter(session -> !Set.of(ImportStatus.COMPLETED, ImportStatus.CONFIRMED, ImportStatus.FAILED).contains(session.getStatus()))
                .count();

        long totalCustomers = customerRepository.count();
        long quota = 50_000L;
        long quotaPct = quota == 0 ? 0 : Math.round((totalCustomers * 100.0) / quota);
        double confidence = totalRows == 0 ? 100.0 : (totalValid * 100.0) / totalRows;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("lastActivityLabel", latest.map(session -> timeAgo(session.getUploadTime())).orElse("Belum ada import"));
        stats.put("lastActivityDetail", latest.map(session ->
                Optional.ofNullable(session.getTotalValid()).orElse(0) + " valid, " +
                        Optional.ofNullable(session.getTotalError()).orElse(0) + " gagal dari " +
                        Optional.ofNullable(session.getTotalData()).orElse(0) + " rows"
        ).orElse("Upload Excel untuk mulai sinkronisasi data"));
        stats.put("lastTimestamp", latest.map(session -> session.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).orElse("-"));
        stats.put("queueStatus", activeQueue > 0 ? "Processing" : "Idle");
        stats.put("queueDetail", activeQueue > 0 ? activeQueue + " session menunggu konfirmasi" : "Tidak ada import yang tertunda");
        stats.put("queueMeta", activeQueue > 0 ? "ACTION REQUIRED" : "ALL IMPORTS SETTLED");
        stats.put("storageUsed", totalCustomers);
        stats.put("storageQuota", quota);
        stats.put("storageUsedLabel", compactNumber(totalCustomers));
        stats.put("storageQuotaLabel", compactNumber(quota));
        stats.put("storagePct", Math.min(100, quotaPct));
        stats.put("engineConfidence", Math.round(confidence * 10.0) / 10.0);
        stats.put("totalImportedRows", totalRows);
        stats.put("totalValidRows", totalValid);
        stats.put("totalErrorRows", totalError);
        return stats;
    }

    private void saveLog(
            UploadSession session,
            String custId,
            ImportLogStatus status,
            ActionType actionType,
            String message
    ) {
        ImportLog log = ImportLog.builder()
                .uploadSession(session)
                .custId(custId)
                .status(status)
                .actionType(actionType)
                .message(message)
                .build();

        importLogRepository.save(log);
    }

    private String timeAgo(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(time, now);
        if (minutes < 1) {
            return "Baru saja";
        }
        if (minutes < 60) {
            return minutes + " menit lalu";
        }
        long hours = ChronoUnit.HOURS.between(time, now);
        if (hours < 24) {
            return hours + " jam lalu";
        }
        long days = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate());
        return days + " hari lalu";
    }

    private String compactNumber(long value) {
        if (value >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", value / 1_000_000.0);
        }
        if (value >= 1_000) {
            return String.format(Locale.US, "%.1fk", value / 1_000.0);
        }
        return NumberFormat.getNumberInstance(new Locale("id", "ID")).format(value);
    }

    private Customer mapRowToCustomer(Map<String, Object> row) {
        String custId = first(row, "Cust ID", "Customer ID", "custId", "cust_id");
        String nama = first(row, "Nama", "Nama (Customer Name)", "Customer Name", "Name", "nama");

        Optional<Customer> existing =
                customerRepository.findByCustId(custId);

        Customer customer = existing.orElse(new Customer());

        customer.setCustId(custId);
        customer.setNama(nama);
        customer.setNoTelpon(first(row, "No Telpon", "No Telepon", "Nomor Telpon", "Nomor Telepon", "Telepon", "Phone", "Nomor HP"));
        customer.setEmail(first(row, "Email", "email"));
        customer.setTanggalRegistrasi(parseDate(first(row, "Tanggal Registrasi", "Tgl Registrasi", "Registration Date", "Tanggal Register")));
        customer.setTanggalAktivasi(parseDate(first(row, "Tanggal Aktivasi", "Tgl Aktivasi", "Tanggal Aktif", "Activation Date")));
        customer.setStatus(parseStatus(first(row, "Status", "Customer Status")));
        customer.setIsolir(customer.getStatus() == CustomerStatus.ISOLIR);
        customer.setPrice(parseDouble(first(row, "Price", "Harga", "Tarif")));
        customer.setProfit(parseDouble(first(row, "Profit", "Laba")));
        customer.setBiayaPasang(parseDouble(first(row, "Biaya Pasang", "Installation Fee")));

        String packageName = first(row, "Package", "Paket", "Paket Internet", "Service");
        if (!packageName.isBlank()) {
            InternetPackage pkg = resolvePackage(packageName, customer.getPrice(), customer.getProfit());
            customer.setPkg(pkg);
            if (customer.getPrice() == null) {
                customer.setPrice(pkg.getPrice());
            }
            if (customer.getProfit() == null) {
                customer.setProfit(pkg.getProfit());
            }
        }

        String agentName = first(row, "Agen", "Agent", "Nama Agen");
        if (!agentName.isBlank()) {
            customer.setAgent(resolveAgent(agentName));
        }

        Address address = resolveAddress(row);
        if (address != null) {
            customer.setAddress(address);
        }

        return customer;
    }

    private InternetPackage resolvePackage(String packageName, Double price, Double profit) {
        Double speed = parseSpeed(packageName);
        if (speed != null) {
            List<InternetPackage> candidates = internetPackageRepository.findBySpeed(speed);
            for (InternetPackage p : candidates) {
                double p1 = price != null ? price : 0;
                double p2 = p.getPrice() != null ? p.getPrice() : 0;
                if (Math.abs(p1 - p2) < 1.0) {
                    return p;
                }
            }
        }
        
        return internetPackageRepository.findByNameIgnoreCase(packageName)
                .orElseGet(() -> {
                    InternetPackage pkg = new InternetPackage();
                    
                    String cleanName = packageName;
                    if (speed != null) {
                        cleanName = speed + " Mbps" + (price != null && price > 0 ? " - Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(price) : "");
                    }
                    
                    pkg.setName(cleanName);
                    pkg.setSpeed(speed);
                    pkg.setPrice(price);
                    pkg.setProfit(profit);
                    pkg.setDescription("Created from import");
                    return internetPackageRepository.save(pkg);
                });
    }

    private Agent resolveAgent(String agentName) {
        return agentRepository.findByNamaIgnoreCase(agentName)
                .orElseGet(() -> {
                    Agent agent = new Agent();
                    agent.setNama(agentName);
                    agent.setStatus(AgentStatus.AKTIF);
                    return agentRepository.save(agent);
                });
    }

    private Address resolveAddress(Map<String, Object> row) {
        String alamat = first(row, "Alamat", "Address", "Street");
        String kota = first(row, "Kota", "City");
        String kelurahan = first(row, "Kelurahan", "Kelurahan Desa", "Kelurahan / Desa", "Desa", "Village");
        String kecamatan = first(row, "Kecamatan", "District");
        String rtRw = first(row, "RT/RW", "Rt Rw", "RTRW");
        String kodePos = first(row, "Kode Pos", "Kodepos", "Postal Code");
        if (kota.isBlank()) {
            kota = first(row, "Kota Kab", "Kota / Kab", "Kota Kabupaten", "Kabupaten", "Regency");
        }

        if (alamat.isBlank() && kota.isBlank() && kelurahan.isBlank() && kecamatan.isBlank()) {
            return null;
        }

        String resolvedKota = kota;
        return addressRepository.findFirstByAlamatIgnoreCaseAndKotaIgnoreCase(alamat, resolvedKota)
                .orElseGet(() -> {
                    Address address = new Address();
                    address.setAlamat(alamat.isBlank() ? "-" : alamat);
                    address.setKota(resolvedKota);
                    address.setKelurahan(kelurahan);
                    address.setKecamatan(kecamatan);
                    address.setRtRw(rtRw);
                    address.setKodePos(kodePos);
                    return addressRepository.save(address);
                });
    }

    private String first(Map<String, Object> row, String... keys) {
        for (String targetKey : keys) {
            String cleanTarget = normalizeKey(targetKey);
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String cleanKey = normalizeKey(entry.getKey());
                if (cleanKey.equals(cleanTarget)) {
                    Object value = entry.getValue();
                    if (value != null && !String.valueOf(value).isBlank()) {
                        return String.valueOf(value).trim();
                    }
                }
            }
        }
        return "";
    }

    private String normalizeKey(String value) {
        return Optional.ofNullable(value)
                .orElse("")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private CustomerStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return CustomerStatus.ACTIVE;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("ISOLIR") || normalized.contains("SUSPEND")) {
            return CustomerStatus.ISOLIR;
        }
        return CustomerStatus.ACTIVE;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("d-M-yyyy"),
                DateTimeFormatter.ofPattern("d/M/yy"),
                DateTimeFormatter.ofPattern("M/d/yy"),
                DateTimeFormatter.ofPattern("d-M-yy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID")),
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")),
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID")),
                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID")),
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.forLanguageTag("id-ID")),
                DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.forLanguageTag("id-ID")),
                DateTimeFormatter.ofPattern("d-MMM-yy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH)
        );

        String trimmed = value.trim();
        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next known spreadsheet format.
            }
        }

        LocalDate localizedDate = parseLocalizedMonthDate(trimmed);
        if (localizedDate != null) {
            return localizedDate;
        }

        if (trimmed.matches("\\d+(\\.\\d+)?")) {
            try {
                long serial = Math.round(Double.parseDouble(trimmed));
                if (serial > 20_000 && serial < 80_000) {
                    return LocalDate.of(1899, 12, 30).plusDays(serial);
                }
            } catch (NumberFormatException ignored) {
                // Fall through to null.
            }
        }

        return null;
    }

    private LocalDate parseLocalizedMonthDate(String value) {
        Map<String, Integer> months = Map.ofEntries(
                Map.entry("januari", 1), Map.entry("jan", 1), Map.entry("january", 1),
                Map.entry("februari", 2), Map.entry("feb", 2), Map.entry("february", 2),
                Map.entry("maret", 3), Map.entry("mar", 3), Map.entry("march", 3),
                Map.entry("april", 4), Map.entry("apr", 4),
                Map.entry("mei", 5), Map.entry("may", 5),
                Map.entry("juni", 6), Map.entry("jun", 6), Map.entry("june", 6),
                Map.entry("juli", 7), Map.entry("jul", 7), Map.entry("july", 7),
                Map.entry("agustus", 8), Map.entry("agu", 8), Map.entry("aug", 8), Map.entry("august", 8),
                Map.entry("september", 9), Map.entry("sep", 9),
                Map.entry("oktober", 10), Map.entry("okt", 10), Map.entry("oct", 10), Map.entry("october", 10),
                Map.entry("november", 11), Map.entry("nov", 11),
                Map.entry("desember", 12), Map.entry("des", 12), Map.entry("dec", 12), Map.entry("december", 12)
        );
        Matcher matcher = Pattern.compile("(?i)^(\\d{1,2})[-\\s/]+([\\p{L}.]+)[-\\s/]+(\\d{2,4})$").matcher(value.replace(",", " ").replaceAll("\\s+", " ").trim());
        if (!matcher.find()) {
            return null;
        }
        String monthKey = matcher.group(2).replace(".", "").toLowerCase(Locale.ROOT);
        Integer month = months.get(monthKey);
        if (month == null) {
            return null;
        }
        int year = Integer.parseInt(matcher.group(3));
        if (year < 100) year += 2000;
        return LocalDate.of(year, month, Integer.parseInt(matcher.group(1)));
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.replace("Rp", "")
                .replace("rp", "")
                .trim();
        if (normalized.contains(",")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else {
            String[] parts = normalized.split("\\.");
            if (parts.length > 1 && parts[parts.length - 1].length() > 2) {
                normalized = normalized.replace(".", "");
            }
        }
        normalized = normalized.replaceAll("[^0-9.\\-]", "");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Double parseSpeed(String packageName) {
        Matcher matcher = Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(packageName);
        if (!matcher.find()) {
            return null;
        }
        return parseDouble(matcher.group(1));
    }
}
