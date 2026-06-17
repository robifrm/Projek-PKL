package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
}
