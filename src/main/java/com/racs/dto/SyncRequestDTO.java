package com.racs.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRequestDTO {
    private List<String> fids;
    private Integer sourceId;
    private Integer batchSize;
}
