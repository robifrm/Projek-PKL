package com.vnet.vnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.vnet.vnet_backend.enums.ImportStatus;


@Entity
@Table(name = "upload_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "total_data")
    private Integer totalData;

    @Column(name = "total_valid")
    private Integer totalValid;

    @Column(name = "total_error")
    private Integer totalError;

    @Enumerated(EnumType.STRING)
    private ImportStatus status;
}