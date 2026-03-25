package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Source source;

    @Column(name = "reference_lot", unique = true)
    private String referenceLot;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(nullable = false, length = 50)
    private String statut;

    @Column
    private Integer taille;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"lot", "hibernateLazyInitializer", "handler"})
    private List<DonneesBrutes> donneesBrutes;

    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"lot", "hibernateLazyInitializer", "handler"})
    private List<Alerte> alertes;
}
