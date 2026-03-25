package com.racs.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonneesBrutesDTO {
    private Integer id;
    private Integer lotId;
    private String fid;
    private String payload;
    private LocalDateTime dateReception;
    private String hash;
    private String statut;
    private ValidationDTO validation;
}
