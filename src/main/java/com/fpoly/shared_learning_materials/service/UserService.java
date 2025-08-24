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
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.UserDTO;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import org.thymeleaf.context.Context;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
    private EmailConfigService emailConfigService;

    @Autowired
    private SpringTemplateEngine templateEngine;

	/**
	 * Lấy tất cả người dùng
	 */
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public Page<User> searchUsers(String keyword, String role, String status, int page, int size, String sortBy,
			Sort.Direction direction) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

		Specification<User> spec = Specification.where((root, query, cb) -> cb.isNull(root.get("deletedAt")));

		if (keyword != null && !keyword.isBlank()) {
			String loweredKeyword = "%" + keyword.toLowerCase() + "%";
			spec = spec.and((root, query, cb) -> cb.or(
					cb.like(cb.lower(root.get("username")), loweredKeyword),
					cb.like(cb.lower(root.get("email")), loweredKeyword)));
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

	public boolean registerUser(String firstName, String lastName,String username, String email, String password) {
        // Kiểm tra email hoặc username đã tồn tại
        if (userRepository.existsByEmail(email)|| userRepository.existsByUsername(username)) {
            return false; // Email đã được sử dụng
        }

        // Tạo user mới
        User user = new User();
        user.setUsername(username); 
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(firstName + " " + lastName);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus("pending"); // Trạng thái mặc định là pending
        user.setRole("user");

        // Lưu user vào cơ sở dữ liệu
        userRepository.save(user);
		sendConfirmationEmail(user);
        return true;
    }
	 public boolean confirmUser(String username) {
        // Tìm user theo username
        return userRepository.findByUsername(username)
                .map(user -> {
                    // Kiểm tra trạng thái hiện tại
                    if ("pending".equals(user.getStatus())) {
                        // Cập nhật trạng thái thành ACTIVE
                        user.setStatus("active");
                        user.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(user); 
                        return true;
                    }
                    return false; // Trạng thái không phải PENDING
                })
                .orElse(false); // Không tìm thấy user
    }
		private void sendConfirmationEmail(User user) {
        // Tạo nội dung email từ template Thymeleaf
        Context context = new Context();
        context.setVariable("fullName", user.getFullName());
        context.setVariable("confirmationUrl", "http://localhost:8080/confirm?username=" + user.getUsername());
        String emailContent = templateEngine.process("client/confirmation-email", context);

        // Gửi email sử dụng EmailConfigService
        boolean sent = emailConfigService.sendHtmlEmail(user.getEmail(), "Xác nhận đăng ký tài khoản - EduShare", emailContent);
        if (!sent) {
            throw new RuntimeException("Không thể gửi email xác nhận cho: " + user.getEmail());
        }
    }
}
