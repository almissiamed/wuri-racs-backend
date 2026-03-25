package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String type;

    @Column(length = 20, nullable = false)
    private String niveau;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String contexte;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Column
    private Boolean traitee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Validation validation;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @Column(name = "fid", length = 100)
    private String fid;

    @Column(length = 100)
    private String attribut;

    @Column(name = "valeur_avant", precision = 19, scale = 2)
    private BigDecimal valeurAvant;

    @Column(name = "valeur_apres", precision = 19, scale = 2)
    private BigDecimal valeurApres;

    @Column(precision = 19, scale = 2)
    private BigDecimal variation;

    @PrePersist
    protected void onCreate() {
        dateGeneration = LocalDateTime.now();
        if (traitee == null) {
            traitee = false;
        }
    }
}
