package com.racs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(name = "api_url")
    private String apiUrl;

    @Column(name = "type_objet")
    private String typeObjet;

    @Column(nullable = false)
    private Boolean actif;

    @Column(name = "champs_obligatoires", columnDefinition = "TEXT")
    private String champsObligatoiresJson;

    @Column(name = "types_champs", columnDefinition = "TEXT")
    private String typesChampsJson;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "source", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Lot> lots;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
