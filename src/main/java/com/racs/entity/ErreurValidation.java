package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "erreurs_validation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErreurValidation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Validation validation;

    @Column(length = 100)
    private String champ;

    @Column(name = "code_erreur", length = 50)
    private String codeErreur;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 20)
    private String niveau;
}
