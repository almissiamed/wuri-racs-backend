package com.racs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_modification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalModification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "donnees_socio_eco_id")
    private Integer donneesSocioEcoId;

    @Column(name = "attribut_id")
    private Integer attributId;

    @Column(length = 50, nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String avant;

    @Column(columnDefinition = "TEXT")
    private String apres;

    @Column(name = "date_action")
    private LocalDateTime dateAction;

    @Column(length = 100)
    private String processus;

    @PrePersist
    protected void onCreate() {
        dateAction = LocalDateTime.now();
    }
}
