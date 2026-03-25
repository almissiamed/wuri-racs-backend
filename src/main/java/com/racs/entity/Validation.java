package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "validations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Validation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donnees_brutes_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DonneesBrutes donneesBrutes;

    @Column(name = "format_valide")
    private Boolean formatValide;

    @Column(name = "champs_obligatoires_valides")
    private Boolean champsObligatoiresValides;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(columnDefinition = "TEXT")
    private String rapport;

    @OneToMany(mappedBy = "validation", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"validation", "hibernateLazyInitializer", "handler"})
    private List<ErreurValidation> erreursValidation;

    @PrePersist
    protected void onCreate() {
        dateValidation = LocalDateTime.now();
    }
}
