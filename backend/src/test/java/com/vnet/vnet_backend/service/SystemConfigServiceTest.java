package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.SystemConfig;
import com.vnet.vnet_backend.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemConfigService Unit Tests")
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigService systemConfigService;

    @BeforeEach
    void setUp() {
        // Reset mock behavior if needed
    }

    @Test
    @DisplayName("getAllConfigs() harus mengembalikan konfigurasi default jika DB kosong")
    void getAllConfigs_shouldReturnDefaults_whenDbEmpty() {
        when(systemConfigRepository.findById(anyString())).thenReturn(Optional.empty());

        Map<String, Object> configs = systemConfigService.getAllConfigs();

        assertThat(configs).containsEntry("dbEndpoint", "mysql://vnet-db.internal:3306/vnet_prod");
        assertThat(configs).containsEntry("syncInterval", 15);
        assertThat(configs).containsEntry("maxImportBatch", 1200);
        assertThat(configs).containsEntry("autoBackup", true);
        assertThat(configs).containsEntry("maintenanceMode", false);

        // Harus menyimpan default values ke DB jika belum ada
        verify(systemConfigRepository, atLeastOnce()).save(any(SystemConfig.class));
    }

    @Test
    @DisplayName("get() harus mengembalikan nilai dari DB jika ada")
    void get_shouldReturnValueFromDb_whenExists() {
        SystemConfig config = new SystemConfig("someKey", "customValue");
        when(systemConfigRepository.findById("someKey")).thenReturn(Optional.of(config));

        String value = systemConfigService.get("someKey", "defaultValue");

        assertThat(value).isEqualTo("customValue");
        verify(systemConfigRepository, never()).save(any(SystemConfig.class));
    }

    @Test
    @DisplayName("getInt() harus mem-parse Integer dengan benar")
    void getInt_shouldParseAndReturnInteger() {
        SystemConfig config = new SystemConfig("intKey", "99");
        when(systemConfigRepository.findById("intKey")).thenReturn(Optional.of(config));

        Integer value = systemConfigService.getInt("intKey", 5);

        assertThat(value).isEqualTo(99);
    }

    @Test
    @DisplayName("getInt() harus mengembalikan default jika format string bukan angka")
    void getInt_shouldReturnDefault_whenInvalidNumberFormat() {
        SystemConfig config = new SystemConfig("invalidIntKey", "not-a-number");
        when(systemConfigRepository.findById("invalidIntKey")).thenReturn(Optional.of(config));

        Integer value = systemConfigService.getInt("invalidIntKey", 5);

        assertThat(value).isEqualTo(5);
    }

    @Test
    @DisplayName("getBool() harus mem-parse boolean dengan benar")
    void getBool_shouldParseAndReturnBoolean() {
        SystemConfig configTrue = new SystemConfig("boolTrueKey", "true");
        SystemConfig configFalse = new SystemConfig("boolFalseKey", "false");

        when(systemConfigRepository.findById("boolTrueKey")).thenReturn(Optional.of(configTrue));
        when(systemConfigRepository.findById("boolFalseKey")).thenReturn(Optional.of(configFalse));

        assertThat(systemConfigService.getBool("boolTrueKey", false)).isTrue();
        assertThat(systemConfigService.getBool("boolFalseKey", true)).isFalse();
    }

    @Test
    @DisplayName("saveAllConfigs() harus memperbarui nilai yang sesuai di DB")
    void saveAllConfigs_shouldUpdateConfigValues() {
        when(systemConfigRepository.findById("dbEndpoint"))
                .thenReturn(Optional.of(new SystemConfig("dbEndpoint", "oldValue")));
        when(systemConfigRepository.findById("syncInterval"))
                .thenReturn(Optional.of(new SystemConfig("syncInterval", "15")));
        when(systemConfigRepository.findById("maxImportBatch"))
                .thenReturn(Optional.of(new SystemConfig("maxImportBatch", "1000")));
        when(systemConfigRepository.findById("autoBackup"))
                .thenReturn(Optional.of(new SystemConfig("autoBackup", "true")));
        when(systemConfigRepository.findById("maintenanceMode"))
                .thenReturn(Optional.of(new SystemConfig("maintenanceMode", "false")));

        Map<String, Object> payload = new HashMap<>();
        payload.put("dbEndpoint", "newMysqlValue");
        payload.put("syncInterval", 30);
        payload.put("maxImportBatch", 500);
        payload.put("autoBackup", false);
        payload.put("maintenanceMode", true);

        systemConfigService.saveAllConfigs(payload);

        verify(systemConfigRepository).save(argThat(config -> 
            config.getConfigKey().equals("dbEndpoint") && config.getConfigValue().equals("newMysqlValue")
        ));
        verify(systemConfigRepository).save(argThat(config -> 
            config.getConfigKey().equals("syncInterval") && config.getConfigValue().equals("30")
        ));
        verify(systemConfigRepository).save(argThat(config -> 
            config.getConfigKey().equals("maxImportBatch") && config.getConfigValue().equals("500")
        ));
        verify(systemConfigRepository).save(argThat(config -> 
            config.getConfigKey().equals("autoBackup") && config.getConfigValue().equals("false")
        ));
        verify(systemConfigRepository).save(argThat(config -> 
            config.getConfigKey().equals("maintenanceMode") && config.getConfigValue().equals("true")
        ));
    }
}
