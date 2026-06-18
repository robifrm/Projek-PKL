package com.vnet.vnet_backend.repository;

import com.vnet.vnet_backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);
    List<UserSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByToken(String token);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);
}
