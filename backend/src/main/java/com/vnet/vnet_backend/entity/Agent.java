package com.vnet.vnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    private String noTelpon;

    private String area;

    private Double komisi;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;
}