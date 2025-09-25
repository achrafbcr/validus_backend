package com.validus.backend.domain.model;

import com.validus.backend.domain.enums.AuditAction;
import com.validus.backend.infrastructure.persistence.converter.JsonMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 60)
    private AuditAction action;

    @Column(name = "by_user", nullable = false, length = 100)
    private String byUser;

    @CreationTimestamp
    @Column(name = "at", nullable = false, updatable = false)
    private Instant at;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "meta", columnDefinition = "nvarchar(max)")
    private Map<String, Object> meta;
}
