package com.racs.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceDTO {
    private Integer id;
    private String nom;
    private String apiUrl;
    private String typeObjet;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private List<String> champsObligatoires;
    private Map<String, String> typesChamps;
}
