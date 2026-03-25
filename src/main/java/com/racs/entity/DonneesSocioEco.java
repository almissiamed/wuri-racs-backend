package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "donnees_socio_eco", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"fid", "source_id", "date_valeur"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonneesSocioEco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fid", length = 100)
    private String fid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    @JsonIgnoreProperties({"lots", "hibernateLazyInitializer", "handler"})
    private Source source;

    @Column(name = "date_valeur")
    private LocalDate dateValeur;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    @Column
    private Integer version;

    @OneToMany(mappedBy = "donneesSocioEco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"donneesSocioEco", "hibernateLazyInitializer", "handler"})
    private List<AttributsDonneesSocioEco> attributs;

    @PrePersist
    protected void onCreate() {
        dateMiseAJour = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
    }
}
