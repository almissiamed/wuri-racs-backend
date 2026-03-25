package com.racs.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonneesSocioEcoDTO {
    private Integer id;
    private String fid;
    private Integer sourceId;
    private String sourceNom;
    private LocalDate dateValeur;
    private LocalDateTime dateMiseAJour;
    private Integer version;
    private Map<String, Object> attributs;
}
