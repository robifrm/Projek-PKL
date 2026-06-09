package com.vnet.vnet_backend.entity;

import com.vnet.vnet_backend.enums.ActionType;
import com.vnet.vnet_backend.enums.ImportLogStatus;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "import_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cust_id")
    private String custId;

    @Enumerated(EnumType.STRING)
    private ImportLogStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    private String message;

    @ManyToOne
    @JoinColumn(name = "upload_session_id")
    private UploadSession uploadSession;
}
