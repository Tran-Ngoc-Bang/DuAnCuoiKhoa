package com.fpoly.shared_learning_materials.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fpoly.shared_learning_materials.domain.File;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.FileRepository;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    public File saveFile(MultipartFile file, User uploadedBy) throws IOException {
        File newFile = new File();
        newFile.setFileName(file.getOriginalFilename());
        newFile.setFilePath("uploads/" + file.getOriginalFilename()); // Đường dẫn tương đối
        newFile.setFileType(file.getContentType());
        newFile.setFileSize(file.getSize());
        newFile.setMimeType(file.getContentType());
        newFile.setUploadedBy(uploadedBy);
        newFile.setStatus("active");
        newFile.setCreatedAt(LocalDateTime.now());
        newFile.setUpdatedAt(LocalDateTime.now());

        // Lưu file vào thư mục
        String uploadDir = "src/main/resources/static/uploads/";
        Path filePath = Paths.get(uploadDir + file.getOriginalFilename());
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        return fileRepository.save(newFile);
    }

    public void deleteFile(String filePath) throws IOException {
        try {
            // Xóa file vật lý từ hệ thống file
            Path path = Paths.get("src/main/resources/static/" + filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("Successfully deleted physical file: " + filePath);
            } else {
                System.out.println("Physical file not found: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error deleting physical file: " + filePath + " - " + e.getMessage());
            throw e;
        }
    }
}