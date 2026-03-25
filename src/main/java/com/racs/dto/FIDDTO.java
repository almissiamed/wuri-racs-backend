package com.racs.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FIDDTO {
    private String fid;
    private LocalDate dateCreation;
    private String statut;
    private String metadonnees;
}
