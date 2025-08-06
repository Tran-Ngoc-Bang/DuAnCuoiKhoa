package com.fpoly.shared_learning_materials.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {
	private static final Path IMAGE_DIR = Paths.get("c:\\var\\images");

	public static Optional<String> upload(MultipartFile multipartFile) {
		Optional<String> imageName = Optional.empty();
		try {
			if (multipartFile.getSize() != 0 && multipartFile.getContentType().startsWith("image")) {
				if (!Files.exists(IMAGE_DIR)) {
					Files.createDirectories(IMAGE_DIR);
				}
				Path targetLocation = Files.createTempFile(IMAGE_DIR, "img-", ".jpg");
				try (InputStream fileContent = multipartFile.getInputStream()) {
					Files.copy(fileContent, targetLocation, StandardCopyOption.REPLACE_EXISTING);
				}
				imageName = Optional.of(targetLocation.getFileName().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageName;
	}

	public static void delete(String imageName) {
		Path imagePath = IMAGE_DIR.resolve(imageName).normalize();
		try {
			boolean result = Files.deleteIfExists(imagePath);
			if (result) {
				System.out.println("File is deleted: " + imageName);
			} else {
				System.out.println("Sorry, unable to delete the file: " + imageName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
