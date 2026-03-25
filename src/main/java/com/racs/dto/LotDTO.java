package com.racs.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotDTO {
    private Integer id;
    private Integer sourceId;
    private String sourceNom;
    private String referenceLot;
    private LocalDateTime dateEnvoi;
    private String statut;
    private Integer taille;
    private LocalDateTime dateFin;
}
