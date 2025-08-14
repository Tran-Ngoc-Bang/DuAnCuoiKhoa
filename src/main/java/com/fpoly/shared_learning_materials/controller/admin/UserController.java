package com.fpoly.shared_learning_materials.controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.UserDTO;
import com.fpoly.shared_learning_materials.service.UserService;
import com.fpoly.shared_learning_materials.utils.ImageUtils;
import com.fpoly.shared_learning_materials.utils.UserExcelExporter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/users")
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping
	public String showUsersPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			@RequestParam(required = false) String keyword, @RequestParam(required = false) String role,
			@RequestParam(required = false) String status, Model model) {
		Page<User> usersPage = userService.searchUsers(keyword, role, status, page, size);

		model.addAttribute("usersPage", usersPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", usersPage.getTotalPages());

		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedRole", role);
		model.addAttribute("selectedStatus", status);

		return "admin/users/show";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("userDTO", new UserDTO());
		model.addAttribute("readonly", false);
		return "admin/users/form";
	}

	@PostMapping("/create")
	public String createUser(@ModelAttribute UserDTO userDTO, Model model, RedirectAttributes redirectAttrs) {
		boolean hasError = false;

		if (userService.emailExists(userDTO.getEmail())) {
			model.addAttribute("emailError", "Email này đã được sử dụng");
			hasError = true;
		}

		if (userService.usernameExists(userDTO.getUsername())) {
			model.addAttribute("usernameError", "Tên đăng nhập này đã tồn tại");
			hasError = true;
		}

		if (hasError) {
			model.addAttribute("userDTO", userDTO);
			return "admin/users/form";
		}

		Optional<String> uploaded = ImageUtils.upload(userDTO.getFile());
		uploaded.ifPresent(userDTO::setAvatarUrl);

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
		return "admin/users/form";
	}

	@PostMapping("/{id}/edit")
	public String updateUser(@PathVariable Long id, @ModelAttribute UserDTO userDTO, Model model,
			RedirectAttributes redirectAttrs) {
		Optional<User> optExisting = userService.findById(id);
		if (!optExisting.isPresent()) {
			redirectAttrs.addFlashAttribute("errorMessage", "Người dùng không tồn tại");
			return "redirect:/admin/users";
		}
		User existing = optExisting.get();

		boolean hasError = false;

		if (!existing.getEmail().equals(userDTO.getEmail()) && userService.emailExists(userDTO.getEmail())) {
			model.addAttribute("emailError", "Email này đã được sử dụng");
			hasError = true;
		}

		if (!existing.getUsername().equals(userDTO.getUsername())
				&& userService.usernameExists(userDTO.getUsername())) {
			model.addAttribute("usernameError", "Tên đăng nhập này đã tồn tại");
			hasError = true;
		}

		if (hasError) {
			userDTO.setId(id);
			model.addAttribute("userDTO", userDTO);
			return "admin/users/form";
		}

		Optional<String> uploaded = ImageUtils.upload(userDTO.getFile());
		uploaded.ifPresent(userDTO::setAvatarUrl);

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
	public String softDeleteUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttrs) {
		userService.softDelete(id);
		redirectAttrs.addFlashAttribute("successMessage", "Đã xóa người dùng.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/users");
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
