package com.fpoly.shared_learning_materials.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        // Generate hashes for common passwords
        System.out.println("Password: password");
        System.out.println("Hash: " + encoder.encode("password"));
        System.out.println();

        System.out.println("Password: admin");
        System.out.println("Hash: " + encoder.encode("admin"));
        System.out.println();

        System.out.println("Password: user123");
        System.out.println("Hash: " + encoder.encode("user123"));
    }
}