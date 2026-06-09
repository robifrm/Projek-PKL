package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.Agent;
import com.vnet.vnet_backend.entity.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    List<Agent> findByStatus(AgentStatus status);

    Optional<Agent> findByNamaIgnoreCase(String nama);
}
