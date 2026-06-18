package com.vnet.vnet_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vnet.vnet_backend.entity.InternetPackage;

import java.util.Optional;
import java.util.List;

public interface InternetPackageRepository extends JpaRepository<InternetPackage, Long> {

    Optional<InternetPackage> findByNameIgnoreCase(String name);
    List<InternetPackage> findBySpeed(Double speed);

    @Query("SELECT DISTINCT p FROM Customer c JOIN c.pkg p WHERE c.agent.id = :agentId")
    List<InternetPackage> findPackagesSoldByAgentId(@Param("agentId") Long agentId);
}
