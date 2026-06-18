package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.Customer;
import com.vnet.vnet_backend.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByCustId(String custId);

    Optional<Customer> findByCustId(String custId);

    long countByStatus(CustomerStatus status);

    List<Customer> findTop5ByOrderByTanggalRegistrasiDesc();

    long countByAgentId(Long agentId);

    boolean existsByPkgId(Long pkgId);

    /**
     * Fetch all customers with their relations (pkg, address, agent) in ONE query.
     * This eliminates the N+1 problem where each lazy relation triggered separate SQL.
     */
    @EntityGraph(attributePaths = {"pkg", "address", "agent"})
    @Query("SELECT c FROM Customer c")
    List<Customer> findAllWithRelations();

    @EntityGraph(attributePaths = {"pkg", "address", "agent"})
    @Query("SELECT c FROM Customer c WHERE LOWER(c.nama) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.custId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);
}
