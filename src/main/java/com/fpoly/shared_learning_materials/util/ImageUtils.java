package com.fpoly.shared_learning_materials.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {

    private static final Path IMAGE_DIR = Paths.get("src/main/resources/static/assets/images");

    /**
     * Upload ảnh và trả về tên file (đường dẫn tương đối để lưu trong DB hoặc trả về giao diện).
     */
    public static Optional<String> upload(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return Optional.empty();
        }

        try {
            String contentType = multipartFile.getContentType();
            if (contentType == null || !contentType.startsWith("image")) {
                return Optional.empty();
            }

            if (!Files.exists(IMAGE_DIR)) {
                Files.createDirectories(IMAGE_DIR);
            }

            String originalFilename = multipartFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = "img-" + UUID.randomUUID() + extension;

            Path targetLocation = IMAGE_DIR.resolve(uniqueFilename);

            try (InputStream fileContent = multipartFile.getInputStream()) {
                Files.copy(fileContent, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = "/assets/images/" + uniqueFilename;
            return Optional.of(relativePath);

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Xóa ảnh theo tên file (tên đầy đủ hoặc tương đối)
     */
    public static void delete(String imageName) {
        if (imageName == null || imageName.isBlank()) return;

        String fileName = Paths.get(imageName).getFileName().toString();
        Path imagePath = IMAGE_DIR.resolve(fileName).normalize();

        try {
            boolean result = Files.deleteIfExists(imagePath);
            if (result) {
                System.out.println("Ảnh đã được xóa: " + fileName);
            } else {
                System.out.println("Không tìm thấy ảnh để xóa: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
