package com.vnet.vnet_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vnet.vnet_backend.entity.InternetPackage;

import java.util.Optional;

public interface InternetPackageRepository extends JpaRepository<InternetPackage, Long> {

    Optional<InternetPackage> findByNameIgnoreCase(String name);
    java.util.List<InternetPackage> findBySpeed(Double speed);
}
