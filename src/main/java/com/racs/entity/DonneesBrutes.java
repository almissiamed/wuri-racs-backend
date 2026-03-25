package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donnees_brutes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonneesBrutes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lot lot;

    @Column(name = "fid", length = 100)
    private String fid;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "date_reception")
    private LocalDateTime dateReception;

    @Column(length = 64)
    private String hash;

    @Column(nullable = false, length = 50)
    private String statut;

    @OneToOne(mappedBy = "donneesBrutes", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"donneesBrutes", "hibernateLazyInitializer", "handler"})
    private Validation validation;

    @PrePersist
    protected void onCreate() {
        dateReception = LocalDateTime.now();
    }
}
