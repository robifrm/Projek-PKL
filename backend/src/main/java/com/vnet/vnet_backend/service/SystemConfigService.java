package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.SystemConfig;
import com.vnet.vnet_backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public static final String KEY_DB_ENDPOINT = "dbEndpoint";
    public static final String KEY_SYNC_INTERVAL = "syncInterval";
    public static final String KEY_MAX_IMPORT_BATCH = "maxImportBatch";
    public static final String KEY_AUTO_BACKUP = "autoBackup";
    public static final String KEY_MAINTENANCE_MODE = "maintenanceMode";

    @Transactional(readOnly = true)
    public Map<String, Object> getAllConfigs() {
        Map<String, Object> configs = new LinkedHashMap<>();
        configs.put(KEY_DB_ENDPOINT, get(KEY_DB_ENDPOINT, "mysql://vnet-db.internal:3306/vnet_prod"));
        configs.put(KEY_SYNC_INTERVAL, getInt(KEY_SYNC_INTERVAL, 15));
        configs.put(KEY_MAX_IMPORT_BATCH, getInt(KEY_MAX_IMPORT_BATCH, 1200));
        configs.put(KEY_AUTO_BACKUP, getBool(KEY_AUTO_BACKUP, true));
        configs.put(KEY_MAINTENANCE_MODE, getBool(KEY_MAINTENANCE_MODE, false));
        return configs;
    }

    @Transactional
    public void saveAllConfigs(Map<String, Object> configs) {
        if (configs.containsKey(KEY_DB_ENDPOINT)) {
            set(KEY_DB_ENDPOINT, String.valueOf(configs.get(KEY_DB_ENDPOINT)));
        }
        if (configs.containsKey(KEY_SYNC_INTERVAL)) {
            set(KEY_SYNC_INTERVAL, String.valueOf(configs.get(KEY_SYNC_INTERVAL)));
        }
        if (configs.containsKey(KEY_MAX_IMPORT_BATCH)) {
            set(KEY_MAX_IMPORT_BATCH, String.valueOf(configs.get(KEY_MAX_IMPORT_BATCH)));
        }
        if (configs.containsKey(KEY_AUTO_BACKUP)) {
            set(KEY_AUTO_BACKUP, String.valueOf(configs.get(KEY_AUTO_BACKUP)));
        }
        if (configs.containsKey(KEY_MAINTENANCE_MODE)) {
            set(KEY_MAINTENANCE_MODE, String.valueOf(configs.get(KEY_MAINTENANCE_MODE)));
        }
    }

    public String get(String key, String defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getConfigValue)
                .orElseGet(() -> {
                    // Save default value to database on demand
                    SystemConfig def = new SystemConfig(key, defaultValue);
                    systemConfigRepository.save(def);
                    return defaultValue;
                });
    }

    public Integer getInt(String key, Integer defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Boolean getBool(String key, Boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(val);
    }

    @Transactional
    public void set(String key, String value) {
        SystemConfig config = systemConfigRepository.findById(key)
                .orElse(new SystemConfig(key, value));
        config.setConfigValue(value);
        systemConfigRepository.save(config);
    }
}
