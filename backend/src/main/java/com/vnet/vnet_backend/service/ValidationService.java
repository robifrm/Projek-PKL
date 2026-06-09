package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ValidationService {

    private final CustomerRepository customerRepository;

    public ValidationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Map<String, Object>> validate(List<Map<String, String>> rows) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, String> row : rows) {
            Map<String, Object> validatedRow = new LinkedHashMap<>(row);

            List<String> errors = new ArrayList<>();

            // Ambil field penting dengan skema toleran
            String custId = getValue(row, "Cust ID", "Customer ID", "custId", "cust_id");
            String nama = getValue(row, "Nama", "Nama (Customer Name)", "Customer Name", "Name", "nama");

            // Validasi wajib isi
            if (custId.isBlank()) {
                errors.add("Cust ID wajib diisi");
            }

            if (nama.isBlank()) {
                errors.add("Nama wajib diisi");
            }

            // Tentukan hasil validasi
            if (!errors.isEmpty()) {
                validatedRow.put("validationStatus", "ERROR");
                validatedRow.put("actionType", "SKIP");
                validatedRow.put("errorMessage", String.join("; ", errors));
            } else {
                boolean exists = customerRepository.existsByCustId(custId);

                validatedRow.put("validationStatus", "VALID");
                validatedRow.put(
                        "actionType",
                        exists ? "UPDATE" : "INSERT"
                );
                validatedRow.put("errorMessage", "");
            }

            result.add(validatedRow);
        }

        return result;
    }

    private String getValue(Map<String, String> row, String... keys) {
        for (String targetKey : keys) {
            String cleanTarget = normalizeKey(targetKey);
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String cleanKey = normalizeKey(entry.getKey());
                if (cleanKey.equals(cleanTarget)) {
                    return entry.getValue() == null ? "" : entry.getValue().trim();
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
}
