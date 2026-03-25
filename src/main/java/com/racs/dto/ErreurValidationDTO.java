package com.racs.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErreurValidationDTO {
    private Integer id;
    private Integer validationId;
    private String champ;
    private String codeErreur;
    private String message;
    private String niveau;
}
