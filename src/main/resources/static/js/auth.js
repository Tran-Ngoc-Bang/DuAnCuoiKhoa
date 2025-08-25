// Authentication functionality for EduShare

document.addEventListener("DOMContentLoaded", function () {
  // Initialize login form if it exists
  if (document.getElementById("loginForm")) {
    initLoginForm();
  }

  // Initialize register form if it exists
  if (document.getElementById("registerForm")) {
    initRegisterForm();
  }

  // Initialize password toggles
  initPasswordToggle();

  // Initialize password strength meter
  if (document.getElementById("registerPassword")) {
    initPasswordStrength();
  }

  // Initialize forgot password modal functionality
  const forgotPasswordLink = document.querySelector(".forgot-password");
  if (forgotPasswordLink) {
    forgotPasswordLink.addEventListener("click", function (e) {
      e.preventDefault();
      const modal = document.getElementById("forgotPasswordModal");
      if (modal) {
        modal.classList.add("active");
      }
    });
  }

  // Close modal when clicking on overlay or close button
  const modalOverlay = document.querySelector(".modal-overlay");
  const modalClose = document.querySelector(".modal-close");

  if (modalOverlay) {
    modalOverlay.addEventListener("click", function () {
      document.getElementById("forgotPasswordModal").classList.remove("active");
    });
  }

  if (modalClose) {
    modalClose.addEventListener("click", function () {
      document.getElementById("forgotPasswordModal").classList.remove("active");
    });
  }

  // Handle forgot password form submission
  const forgotPasswordForm = document.getElementById("forgotPasswordForm");
  if (forgotPasswordForm) {
    const submitBtn = document.querySelector(".modal-footer .submit-btn");
    const cancelBtn = document.querySelector(".modal-footer .cancel-btn");

    submitBtn.addEventListener("click", function () {
      const email = document.getElementById("resetEmail").value.trim();

      if (!email) {
        alert("Vui lòng nhập địa chỉ email");
        return;
      }

      if (!isValidEmail(email)) {
        alert("Vui lòng nhập địa chỉ email hợp lệ");
        return;
      }

      // Show loading state
      const originalText = submitBtn.textContent;
      submitBtn.innerHTML =
        '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';
      submitBtn.disabled = true;

      // Simulate sending reset email
      setTimeout(function () {
        // Hiển thị thông báo thành công trong modal thay vì alert
        const formContainer = document.getElementById(
          "forgotPasswordFormContainer"
        );
        const successMessage = document.getElementById("forgotPasswordSuccess");
        const modalFooter = document.querySelector(".modal-footer");

        if (formContainer && successMessage) {
          // Ẩn form và footer
          formContainer.style.display = "none";
          modalFooter.style.display = "none";

          // Hiển thị thông báo thành công
          successMessage.style.display = "block";

          // Tự động đóng modal sau 5 giây
          setTimeout(function () {
            // Đóng modal
            document
              .getElementById("forgotPasswordModal")
              .classList.remove("active");

            // Đặt lại trạng thái sau khi đóng modal
            setTimeout(function () {
              formContainer.style.display = "block";
              successMessage.style.display = "none";
              modalFooter.style.display = "flex";
              forgotPasswordForm.reset();
              submitBtn.textContent = originalText;
              submitBtn.disabled = false;
            }, 300);
          }, 5000);
        } else {
          // Nếu không tìm thấy phần tử thông báo, sử dụng alert như cũ
          alert(
            "Chúng tôi đã gửi hướng dẫn đặt lại mật khẩu đến email " + email
          );

          // Reset form và đóng modal
          forgotPasswordForm.reset();
          document
            .getElementById("forgotPasswordModal")
            .classList.remove("active");

          // Đặt lại trạng thái nút
          submitBtn.textContent = originalText;
          submitBtn.disabled = false;
        }
      }, 1500);
    });

    cancelBtn.addEventListener("click", function () {
      forgotPasswordForm.reset();
      document.getElementById("forgotPasswordModal").classList.remove("active");
    });
  }
});

// Initialize login form functionality
function initLoginForm() {
  const loginForm = document.getElementById("loginForm");

  loginForm.addEventListener("submit", function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    const rememberMe = document.getElementById("rememberMe")
      ? document.getElementById("rememberMe").checked
      : false;

    // Validate inputs
    if (!username) {
      showAuthError("Vui lòng nhập tên đăng nhập");
      return;
    }

    if (!password) {
      showAuthError("Vui lòng nhập mật khẩu");
      return;
    }

    // Show loading state
    const submitBtn = loginForm.querySelector(".login-submit-btn");
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.innerHTML =
      '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
    submitBtn.disabled = true;

    // Submit the form normally for Spring Security to handle
    loginForm.submit();
  });
}

// Initialize register form functionality
function initRegisterForm() {
  const registerForm = document.getElementById("registerForm");

  registerForm.addEventListener("submit", function (e) {
    const firstName = document.getElementById("registerFirstName")
      ? document.getElementById("registerFirstName").value.trim()
      : "";
    const lastName = document.getElementById("registerLastName")
      ? document.getElementById("registerLastName").value.trim()
      : "";
    const email = document.getElementById("registerEmail").value.trim();
    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById(
      "registerConfirmPassword"
    ).value;
    const termsAgreement = document.getElementById("termsAgreement").checked;

    // Validate inputs - prevent form submission if validation fails
    if (!firstName || !lastName) {
      e.preventDefault();
      showAuthError("Vui lòng nhập đầy đủ họ và tên");
      return;
    }

    if (!email) {
      e.preventDefault();
      showAuthError("Vui lòng nhập địa chỉ email");
      return;
    }

    if (!isValidEmail(email)) {
      e.preventDefault();
      showAuthError("Vui lòng nhập địa chỉ email hợp lệ");
      return;
    }

    if (!password) {
      e.preventDefault();
      showAuthError("Vui lòng tạo mật khẩu");
      return;
    }

    if (password.length < 8) {
      e.preventDefault();
      showAuthError("Mật khẩu phải có ít nhất 8 ký tự");
      return;
    }
    
    // Validate password strength - require all criteria
    if (!isPasswordStrong(password)) {
      e.preventDefault();
      showAuthError("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
      return;
    }

    if (password !== confirmPassword) {
      e.preventDefault();
      showAuthError("Mật khẩu xác nhận không khớp");
      return;
    }

    if (!termsAgreement) {
      e.preventDefault();
      showAuthError("Bạn phải đồng ý với điều khoản sử dụng");
      return;
    }

    // Show loading state
    const submitBtn = registerForm.querySelector(".register-submit-btn");
    const originalBtnText = submitBtn.innerHTML;
    submitBtn.innerHTML =
      '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
    submitBtn.disabled = true;

    // Let form submit to backend - backend will handle redirect
  });
}

// Toggle password visibility
function initPasswordToggle() {
  const toggleButtons = document.querySelectorAll(".password-toggle");

  toggleButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const input = this.parentElement.querySelector("input");
      const icon = this.querySelector("i");

      // Toggle input type
      if (input.type === "password") {
        input.type = "text";
        icon.classList.remove("fa-eye");
        icon.classList.add("fa-eye-slash");
      } else {
        input.type = "password";
        icon.classList.remove("fa-eye-slash");
        icon.classList.add("fa-eye");
      }
    });
  });
}

// Check if password meets all strength requirements
function isPasswordStrong(password) {
  if (!password || password.length < 8) return false;
  
  const hasLowercase = /[a-z]/.test(password);
  const hasUppercase = /[A-Z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecial = /[^A-Za-z0-9]/.test(password);
  
  // Require ALL criteria for strong password
  return hasLowercase && hasUppercase && hasNumber && hasSpecial;
}

// Password strength indicator
function initPasswordStrength() {
  const passwordInput = document.getElementById("registerPassword");
  const strengthBar = document.querySelector(".strength-bar");
  const strengthText = document.querySelector(".strength-text span");

  passwordInput.addEventListener("input", function () {
    const password = this.value;
    let strength = 0;
    let strengthLabel = 'Yếu';
    let strengthColor = '#ff4757';

    // Remove all classes from strength bar
    strengthBar.className = "strength-bar";

    if (password.length === 0) {
      strengthBar.style.width = "0%";
      strengthBar.style.backgroundColor = strengthColor;
      strengthText.textContent = strengthLabel;
      return;
    }

    // Check password criteria
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;

    // Determine strength level
    if (strength >= 5) {
      strengthLabel = 'Rất mạnh';
      strengthColor = '#2ed573';
    } else if (strength >= 4) {
      strengthLabel = 'Mạnh';
      strengthColor = '#5f27cd';
    } else if (strength >= 3) {
      strengthLabel = 'Trung bình';
      strengthColor = '#ffa502';
    } else if (strength >= 2) {
      strengthLabel = 'Yếu';
      strengthColor = '#ff6348';
    } else {
      strengthLabel = 'Rất yếu';
      strengthColor = '#ff4757';
    }

    // Update UI
    strengthBar.style.width = (strength * 20) + '%';
    strengthBar.style.backgroundColor = strengthColor;
    strengthText.textContent = strengthLabel;
  });
}

// Utility function to validate email format
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// Show error message
function showAuthError(message) {
  const errorElement = document.querySelector(".auth-error");
  const errorMessageElement = errorElement.querySelector(".error-message");

  // Update error message
  errorMessageElement.textContent = message;

  // Show error message
  errorElement.style.display = "flex";

  // Hide error after 5 seconds
  setTimeout(function () {
    errorElement.style.display = "none";
  }, 5000);
}

// Show success message
function showAuthSuccess(message) {
  const successElement = document.querySelector(".auth-success");
  const successMessageElement =
    successElement.querySelector(".success-message");

  // Update success message
  successMessageElement.textContent = message;

  // Show success message
  successElement.style.display = "flex";

  // Hide success after 5 seconds
  setTimeout(function () {
    successElement.style.display = "none";
  }, 5000);
}
