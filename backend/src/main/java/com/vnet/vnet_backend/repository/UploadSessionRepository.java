package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {
}