package com.fpoly.shared_learning_materials.controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.UserDTO;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.service.UserService;
import com.fpoly.shared_learning_materials.util.ImageUtils;
import com.fpoly.shared_learning_materials.util.UserExcelExporter;
import com.fpoly.shared_learning_materials.service.FileService;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/users")
public class UserController extends BaseAdminController {
	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	public UserController(NotificationService notificationService, UserRepository userRepository) {
		super(notificationService, userRepository);
	}

	@GetMapping
	public String showUsersPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String dir,
			Model model) {

		String sortBy = (sort != null) ? sort : "createdAt";
		Sort.Direction direction = ("desc".equalsIgnoreCase(dir)) ? Sort.Direction.DESC : Sort.Direction.ASC;

		Page<User> usersPage = userService.searchUsers(keyword, role, status, page, size, sortBy, direction);

		model.addAttribute("usersPage", usersPage);
		model.addAttribute("currentPage", "users");
		model.addAttribute("totalPages", usersPage.getTotalPages());

		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedRole", role);
		model.addAttribute("selectedStatus", status);

		model.addAttribute("sort", sortBy);
		model.addAttribute("dir", direction.toString().toLowerCase());
		model.addAttribute("current", page);

		return "admin/users/index";
	}

	@GetMapping("/{id}/detail")
	public String detail(@PathVariable Long id, Model model,
			RedirectAttributes redirectAttrs) {
		Optional<UserDTO> optDto = userService.getUserDTOById(id);
		if (!optDto.isPresent()) {
			redirectAttrs.addFlashAttribute("errorMessage", "Người dùng không tồn tại");
			return "redirect:/admin/users";
		}

		model.addAttribute("userDTO", optDto.get());
		model.addAttribute("currentPage", "users");
		return "admin/users/detail";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("userDTO", new UserDTO());
		model.addAttribute("readonly", false);
		model.addAttribute("currentPage", "users");
		return "admin/users/create";
	}

	@PostMapping("/create")
	public String createUser(@ModelAttribute("userDTO") @Valid UserDTO userDTO,
			BindingResult result,
			Model model,
			RedirectAttributes redirectAttrs) {

		// Kiểm tra mật khẩu có dấu cách không
		if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()
				&& userDTO.getPassword().trim().isEmpty()) {
			model.addAttribute("passwordError", "Mật khẩu không được chỉ chứa dấu cách");
			model.addAttribute("currentPage", "users");
			return "admin/users/create";
		}

		if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty() && userDTO.getPassword().contains(" ")) {
			model.addAttribute("passwordError", "Mật khẩu không được chứa dấu cách");
			model.addAttribute("currentPage", "users");
			return "admin/users/create";
		}

		if (result.hasErrors()) {
			model.addAttribute("currentPage", "users");
			return "admin/users/create";
		}

		if (userService.emailExists(userDTO.getEmail())) {
			model.addAttribute("emailError", "Email này đã được sử dụng");
			model.addAttribute("currentPage", "users");
			return "admin/users/create";
		}

		if (userService.usernameExists(userDTO.getUsername())) {
			model.addAttribute("usernameError", "Tên đăng nhập này đã tồn tại");
			model.addAttribute("currentPage", "users");
			return "admin/users/create";
		}

		try {
			MultipartFile avatarFile = userDTO.getFile();
			if (avatarFile != null && !avatarFile.isEmpty()) {
				com.fpoly.shared_learning_materials.domain.File saved = fileService.saveFile(avatarFile, null);
				userDTO.setAvatarUrl("/documents/file/" + saved.getFileName());
			}
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Tải ảnh đại diện thất bại: " + e.getMessage());
			model.addAttribute("currentPage", "users");
			return "admin/users/create";
		}

		userService.createUser(userDTO);

		redirectAttrs.addFlashAttribute("successMessage", "Tạo người dùng thành công");
		return "redirect:/admin/users";
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable Long id,
			@RequestParam(name = "readonly", required = false, defaultValue = "false") boolean readonly, Model model,
			RedirectAttributes redirectAttrs) {
		Optional<UserDTO> optDto = userService.getUserDTOById(id);
		if (!optDto.isPresent()) {
			redirectAttrs.addFlashAttribute("errorMessage", "Người dùng không tồn tại");
			return "redirect:/admin/users";
		}

		model.addAttribute("userDTO", optDto.get());
		model.addAttribute("readonly", readonly);
		model.addAttribute("currentPage", "users");
		return "admin/users/edit";
	}

	@PostMapping("/{id}/edit")
	public String updateUser(@PathVariable Long id,
			@ModelAttribute("userDTO") @Valid UserDTO userDTO,
			BindingResult result,
			Model model,
			RedirectAttributes redirectAttrs) {

		Optional<User> optExisting = userService.findById(id);
		if (!optExisting.isPresent()) {
			redirectAttrs.addFlashAttribute("errorMessage", "Người dùng không tồn tại");
			return "redirect:/admin/users";
		}

		User existing = optExisting.get();

		// Kiểm tra mật khẩu có dấu cách không
		if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()
				&& userDTO.getPassword().trim().isEmpty()) {
			redirectAttrs.addFlashAttribute("errorMessage", "Mật khẩu không được chỉ chứa dấu cách");
			return "redirect:/admin/users/" + id + "/edit";
		}

		if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty() && userDTO.getPassword().contains(" ")) {
			redirectAttrs.addFlashAttribute("errorMessage", "Mật khẩu không được chứa dấu cách");
			return "redirect:/admin/users/" + id + "/edit";
		}

		if (result.hasErrors()) {
			userDTO.setId(id);
			model.addAttribute("readonly", false);
			model.addAttribute("currentPage", "users");
			return "admin/users/edit";
		}

		if (!existing.getEmail().equals(userDTO.getEmail()) && userService.emailExists(userDTO.getEmail())) {
			model.addAttribute("emailError", "Email này đã được sử dụng");
			userDTO.setId(id);
			model.addAttribute("readonly", false);
			model.addAttribute("currentPage", "users");
			return "admin/users/edit";
		}

		if (!existing.getUsername().equals(userDTO.getUsername())
				&& userService.usernameExists(userDTO.getUsername())) {
			model.addAttribute("usernameError", "Tên đăng nhập này đã tồn tại");
			userDTO.setId(id);
			model.addAttribute("readonly", false);
			model.addAttribute("currentPage", "users");
			return "admin/users/edit";
		}

		// Xử lý upload avatar nếu có file mới
		try {
			MultipartFile avatarFile = userDTO.getFile();
			if (avatarFile != null && !avatarFile.isEmpty()) {
				com.fpoly.shared_learning_materials.domain.File saved = fileService.saveFile(avatarFile, existing);
				// Sử dụng endpoint động để phục vụ avatar
				userDTO.setAvatarUrl("/documents/file/" + saved.getFileName());
			} else {
				// Nếu không chọn file mới và DTO không có avatarUrl, giữ nguyên avatar cũ
				if (userDTO.getAvatarUrl() == null || userDTO.getAvatarUrl().trim().isEmpty()) {
					userDTO.setAvatarUrl(existing.getAvatarUrl());
				}
			}
		} catch (Exception e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Tải ảnh đại diện thất bại: " + e.getMessage());
			return "redirect:/admin/users/" + id + "/edit";
		}

		userService.updateUserFromDTO(id, userDTO);

		redirectAttrs.addFlashAttribute("successMessage", "Cập nhật người dùng thành công");
		return "redirect:/admin/users";
	}

	@GetMapping("/{id}/lock")
	public String lockUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
		userService.updateStatus(id, "inactive");
		redirectAttrs.addFlashAttribute("successMessage", "Đã khóa tài khoản người dùng.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/users");
	}

	@GetMapping("/{id}/unlock")
	public String unlockUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
		userService.updateStatus(id, "active");
		redirectAttrs.addFlashAttribute("successMessage", "Đã mở khóa tài khoản người dùng.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/users");
	}

	@GetMapping("/{id}/approve")
	public String confirmUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
		userService.updateStatus(id, "active");
		redirectAttrs.addFlashAttribute("successMessage", "Đã xác nhận tài khoản.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/users");
	}

	@GetMapping("/{id}/reject")
	public String rejectUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
		userService.updateStatus(id, "inactive");
		redirectAttrs.addFlashAttribute("successMessage", "Đã từ chối tài khoản.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/users");
	}

	@GetMapping("/{id}/delete")
	public String softDeleteUser(@PathVariable Long id, HttpServletRequest request, Model model,
			RedirectAttributes redirectAttrs) {
		Optional<UserDTO> optDto = userService.getUserDTOById(id);
		if (!optDto.isPresent()) {
			redirectAttrs.addFlashAttribute("errorMessage", "Người dùng không tồn tại");
			return "redirect:/admin/users";
		}

		model.addAttribute("userDTO", optDto.get());
		model.addAttribute("currentPage", "users");
		return "admin/users/delete";
	}

	@GetMapping("/{id}/fullDelete")
	public String fullDelete(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
		userService.softDelete(id);
		redirectAttrs.addFlashAttribute("successMessage", "Đã xóa người dùng.");
		return "redirect:/admin/users";
	}

	@GetMapping("/export")
	public void exportUsers(HttpServletResponse response) throws IOException {
		UserExcelExporter userExcelExporter = new UserExcelExporter();
		response.setContentType("application/octet-stream");
		String headerValue = "attachment; filename=users.xlsx";
		response.setHeader("Content-Disposition", headerValue);

		List<User> users = userService.findAllActiveUsers();
		userExcelExporter.export(users, response);
	}

}
