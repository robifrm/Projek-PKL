package com.vnet.vnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ba_aktivasi", indexes = {
    @Index(name = "idx_ba_registration", columnList = "registration_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaAktivasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "registration_id", nullable = false)
    private CustomerRegistration registration;

    private String namaTeknisi;

    @Column(columnDefinition = "TEXT")
    private String catatanTeknisi;

    // 1. Modem / ONT / ONU
    private Integer qtyOnt;
    private String snOnt;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoOnt;

    // 2. Wi-Fi Router
    private Integer qtyRouter;
    private String snRouter;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoRouter;

    // 3. Kabel Fiber Optic Drop Core (Meter)
    private Integer qtyKabel;
    private String snKabel;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoKabel;

    // 4. Roset Optik
    private Integer qtyRoset;
    private String snRoset;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoRoset;

    // 5. Fast Connector / Aksesoris
    private Integer qtyAksesoris;
    private String snAksesoris;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoAksesoris;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tandaTanganCustomer;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tandaTanganTeknisi;

    private String bandwidth;
    private Integer panjangKabel;
    private String koordinatRumah;
    private String kodeOdp;
    private Integer portOdp;
    private String portOdpTerpakai;
    private String pop;
    private String olt;
    private String rosset;
    private Integer pigtail;
    private Integer patchcore;
    private Integer splicing;
    private String redamanOutputKabel;
    private Integer jumlahBracket;
    private Integer jumlahSClamp;
    private String statusAktivasi;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoRumah;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoOdpDepan;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoRedamanOdp;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoDalemOdp;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoRedamanKabel;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoDepanOnt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoBelakangOnt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoSpeedTest;
}
