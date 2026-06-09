package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.ImportLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {

    List<ImportLog> findByUploadSessionId(Long uploadSessionId);
}