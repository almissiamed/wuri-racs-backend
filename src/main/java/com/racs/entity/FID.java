package com.racs.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "fids")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FID {
    @Id
    @Column(name = "fid", length = 100)
    private String fid;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @Column(nullable = false, length = 50)
    private String statut;

    @Column(columnDefinition = "TEXT")
    private String metadonnees;

    @OneToMany(mappedBy = "fid", fetch = FetchType.LAZY)
    private List<DonneesSocioEco> donneesSocioEcoList;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }
    }
}
