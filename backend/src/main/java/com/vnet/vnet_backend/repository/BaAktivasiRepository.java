package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.BaAktivasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaAktivasiRepository extends JpaRepository<BaAktivasi, Long> {
    Optional<BaAktivasi> findByRegistrationId(Long registrationId);
}
