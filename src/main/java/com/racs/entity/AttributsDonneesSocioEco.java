package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "attributs_donnees_socio_eco", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"donnees_socio_eco_id", "nom"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributsDonneesSocioEco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donnees_socio_eco_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
}
