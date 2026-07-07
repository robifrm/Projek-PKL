package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.CustomerRegistration;
import com.vnet.vnet_backend.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRegistrationRepository extends JpaRepository<CustomerRegistration, Long> {

    @EntityGraph(attributePaths = {"pkg"})
    @Query("SELECT r FROM CustomerRegistration r WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:search IS NULL OR LOWER(r.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.nomorSelulerUtama) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.waktu DESC")
    List<CustomerRegistration> searchRegistrations(
            @Param("status") RegistrationStatus status,
            @Param("search") String search
    );

    @EntityGraph(attributePaths = {"pkg"})
    List<CustomerRegistration> findAllByOrderByWaktuDesc();

    boolean existsByCustId(String custId);

    java.util.Optional<CustomerRegistration> findByCustId(String custId);
}
