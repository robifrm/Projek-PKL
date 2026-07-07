package com.vnet.vnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_registrations", indexes = {
    @Index(name = "idx_reg_status", columnList = "status"),
    @Index(name = "idx_reg_waktu", columnList = "waktu")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String custId;

    private LocalDateTime tanggalJadwal;

    @Column(nullable = false)
    private LocalDateTime waktu;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String jenisIdentitas;

    @Column(nullable = false)
    private String nomorIdentitas;

    private String nomorSelulerUtama;

    private String nomorCadangan;

    @Column(columnDefinition = "TEXT")
    private String alamatDetail;

    private String rt;

    private String rw;

    private String kelurahan;

    private String kecamatan;

    private String kota;

    private String provinsi;

    private String kodePos;

    @Column(columnDefinition = "LONGTEXT")
    private String identityPhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private InternetPackage pkg;

    private Double harga;

    @Column(columnDefinition = "TEXT")
    private String noteRequest;

    @Column(columnDefinition = "TEXT")
    private String noteTeknisi;

    private String koordinat;

    private String afiliator;
}
