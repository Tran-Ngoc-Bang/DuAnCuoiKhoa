package com.fpoly.shared_learning_materials.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
	private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private String checksum;
    private Long uploadedById;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
