package com.fpoly.shared_learning_materials.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.UserDTO;
import com.fpoly.shared_learning_materials.repository.UserRepository;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
/**
     * Lấy tất cả người dùng
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> searchUsers(String keyword, String role, String status, int page, int size, String sortBy, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<User> spec = Specification.where((root, query, cb) -> cb.isNull(root.get("deletedAt")));

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(
                    (root, query, cb) -> cb.like(cb.lower(root.get("fullName")), "%" + keyword.toLowerCase() + "%"));
        }

        if (role != null && !role.equals("all")) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }

        if (status != null && !status.equals("all")) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return userRepository.findAll(spec, pageable);
    }


	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean usernameExists(String username) {
		return userRepository.existsByUsername(username);
	}

	public void createUser(UserDTO dto) {
		User user = new User();
		user.setFullName(dto.getFullName());
		user.setEmail(dto.getEmail());
		user.setUsername(dto.getUsername());
		user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
		user.setRole(dto.getRole());
		user.setStatus(dto.getStatus());
		user.setAvatarUrl(dto.getAvatarUrl());
		user.setBio(dto.getBio());
		userRepository.save(user);
	}

	public Optional<UserDTO> getUserDTOById(Long id) {
		return userRepository.findById(id).map(user -> {
			UserDTO dto = new UserDTO();
			dto.setId(user.getId());
			dto.setFullName(user.getFullName());
			dto.setEmail(user.getEmail());
			dto.setUsername(user.getUsername());
			dto.setRole(user.getRole());
			dto.setStatus(user.getStatus());
			dto.setAvatarUrl(user.getAvatarUrl());
			dto.setBio(user.getBio());
			return dto;
		});
	}

	public void updateUserFromDTO(Long id, UserDTO dto) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

		user.setFullName(dto.getFullName());
		user.setEmail(dto.getEmail());
		user.setUsername(dto.getUsername());

		if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
			user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
		}

		user.setRole(dto.getRole());
		user.setStatus(dto.getStatus());
		user.setAvatarUrl(dto.getAvatarUrl());
		user.setBio(dto.getBio());

		userRepository.save(user);
	}

	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	public void updateStatus(Long id, String status) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
		user.setStatus(status);
		userRepository.save(user);
	}

	public void softDelete(Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
		user.setDeletedAt(LocalDateTime.now());
		userRepository.save(user);
	}

	public List<User> findAllActiveUsers() {
		return userRepository.findByDeletedAtIsNull();
	}
}
