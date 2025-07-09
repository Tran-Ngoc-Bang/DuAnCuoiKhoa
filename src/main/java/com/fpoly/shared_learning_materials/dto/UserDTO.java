package com.fpoly.shared_learning_materials.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * DTO cho thao tác tạo/cập nhật người dùng.
 * Dùng để binding dữ liệu form và validate phía backend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    /**
     * Tên đăng nhập, không được để trống, tối đa 255 ký tự.
     */
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 255, message = "Tên đăng nhập tối đa 255 ký tự")
    private String username;

    /**
     * Email hợp lệ, không được để trống, tối đa 255 ký tự.
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    /**
     * Mật khẩu, không được để trống khi tạo mới, tối thiểu 6 ký tự.
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu từ 6-255 ký tự")
    private String password;

    /**
     * Họ tên đầy đủ, không bắt buộc.
     */
    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    /**
     * Số điện thoại, không bắt buộc.
     */
    @Size(max = 50, message = "Số điện thoại tối đa 50 ký tự")
    private String phoneNumber;

    /**
     * Địa chỉ, không bắt buộc.
     */
    private String address;

    /**
     * Ngày sinh, không bắt buộc.
     */
    private LocalDate dateOfBirth;

    /**
     * Giới tính, không bắt buộc.
     */
    private String gender;

    /**
     * Trạng thái tài khoản, mặc định là "active".
     */
    private String status = "active";

    /**
     * Vai trò người dùng (user/admin), không bắt buộc, mặc định là "user".
     */
    private String role = "user";
}