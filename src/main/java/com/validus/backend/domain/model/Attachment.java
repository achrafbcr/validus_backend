package com.validus.backend.domain.model;

import com.validus.backend.domain.enums.AttachmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AttachmentType type;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime", nullable = false, length = 100)
    private String mime;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "sha256", length = 64)
    private String sha256;

    @Column(name = "storage_ref", nullable = false, length = 512)
    private String storageRef;
}
