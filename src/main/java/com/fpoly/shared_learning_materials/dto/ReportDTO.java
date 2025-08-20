package com.fpoly.shared_learning_materials.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportDTO {
    private Long id;
    private String status;
    private String note;
    private String reply;
}
