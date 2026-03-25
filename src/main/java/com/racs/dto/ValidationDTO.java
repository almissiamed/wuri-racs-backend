package com.racs.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationDTO {
    private Integer id;
    private Integer donneesBrutesId;
    private Boolean formatValide;
    private Boolean champsObligatoiresValides;
    private LocalDateTime dateValidation;
    private String rapport;
    private List<ErreurValidationDTO> erreurs;
}
