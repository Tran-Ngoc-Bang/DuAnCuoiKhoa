//package com.fpoly.shared_learning_materials.util;
//
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class FileUtils {
//    
//    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
//    
//    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
//        "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "zip", "rar"
//    );
//    
//    public static String getFileExtension(String fileName) {
//        if (fileName == null || !fileName.contains(".")) {
//            return "";
//        }
//        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
//    }
//    
//    public static boolean isValidFileType(String fileName) {
//        String extension = getFileExtension(fileName);
//        return ALLOWED_EXTENSIONS.contains(extension);
//    }
//    
//    public static boolean isValidFileSize(MultipartFile file) {
//        return file.getSize() <= MAX_FILE_SIZE;
//    }
//    
//    public static void validateFile(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("File cannot be empty");
//        }
//        
//        if (!isValidFileSize(file)) {
//            throw new IllegalArgumentException("File size exceeds maximum allowed size (100MB)");
//        }
//        
//        if (!isValidFileType(file.getOriginalFilename())) {
//            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
//        }
//    }
//}