package com.racs.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteDTO {
    private Integer id;
    private String type;
    private String niveau;
    private String message;
    private String contexte;
    private LocalDateTime dateGeneration;
    private Boolean traitee;
    private Integer lotId;
    private Integer validationId;
    private LocalDateTime dateTraitement;
}
