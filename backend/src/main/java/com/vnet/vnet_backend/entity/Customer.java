package com.vnet.vnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_status", columnList = "status"),
    @Index(name = "idx_customer_reg_date", columnList = "tanggalRegistrasi"),
    @Index(name = "idx_customer_agent", columnList = "agent_id"),
    @Index(name = "idx_customer_pkg", columnList = "package_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String custId;

    @Column(nullable = false)
    private String nama;

    private String noTelpon;

    private String email;

    private LocalDate tanggalRegistrasi;

    private LocalDate tanggalAktivasi;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    private Double price;

    private Double profit;

    private Double biayaPasang;

    private Boolean isolir;

    // RELASI

    @ManyToOne
    @JoinColumn(name = "package_id")
    private InternetPackage pkg;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne(optional = true)
    @JoinColumn(name = "agent_id")
    private Agent agent;
}