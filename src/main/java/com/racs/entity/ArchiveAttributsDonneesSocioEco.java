package com.racs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "archive_attributs_donnees_socio_eco", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"donnees_socio_eco_id", "nom", "version"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveAttributsDonneesSocioEco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "attribut_origine_id")
    private Integer attributOrigineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donnees_socio_eco_id", nullable = false)
    private DonneesSocioEco donneesSocioEco;

    @Column(length = 100)
    private String nom;

    @Column(name = "valeur_text")
    private String valeurText;

    @Column(name = "valeur_numerique", precision = 19, scale = 2)
    private BigDecimal valeurNumerique;

    @Column(name = "valeur_date")
    private LocalDate valeurDate;

    @Column(name = "valeur_boolean")
    private Boolean valeurBoolean;

    @Column
    private Integer version;

    @Column(name = "date_archivage")
    private LocalDateTime dateArchivage;

    @Column(name = "raison_archivage", length = 255)
    private String raisonArchivage;

    @PrePersist
    protected void onCreate() {
        dateArchivage = LocalDateTime.now();
    }
}
