package com.vnet.vnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_sessions",
    indexes = {
        @Index(name = "idx_user_sessions_token", columnList = "token"),
        @Index(name = "idx_user_sessions_user_id", columnList = "user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(
        name = "fk_user_sessions_user_id",
        foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
    ))
    private User user;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(length = 255)
    private String device;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
