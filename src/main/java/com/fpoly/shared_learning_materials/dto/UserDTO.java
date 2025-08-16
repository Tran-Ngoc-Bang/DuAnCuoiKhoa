package com.fpoly.shared_learning_materials.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
	private Long id;
	
	private String fullName;

	private String email;

	private String username;

	private String password;

	private String role;

	private String status; // Trạng thái (active, inactive, pending)

	private MultipartFile file;

	private String avatarUrl;

	private String bio;
}