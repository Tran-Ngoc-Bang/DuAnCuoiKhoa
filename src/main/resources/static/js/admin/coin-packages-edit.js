/**
 * Coin Packages Edit Page JavaScript
 * Chỉnh sửa gói xu với MVC thuần
 */

document.addEventListener("DOMContentLoaded", function () {
  // Elements
  const editPackageForm = document.getElementById("editPackageForm");
  const codeInput = document.getElementById("code");
  const nameInput = document.getElementById("name");
  const originalPriceInput = document.getElementById("originalPrice");
  const salePriceInput = document.getElementById("salePrice");
  const discountPercentInput = document.getElementById("discountPercent");
  const generateCodeBtn = document.getElementById("generateCodeBtn");

  /**
   * Khởi tạo trang
   */
  function initializePage() {
    console.log("Initializing coin package edit page...");
    try {
      setupFormValidation();
      setupPriceCalculation();
      setupCodeGeneration();
      console.log("Coin package edit page initialized successfully");
    } catch (error) {
      console.error("Error initializing coin package edit page:", error);
    }
  }

  /**
   * Thiết lập form validation
   */
  function setupFormValidation() {
    if (editPackageForm) {
      editPackageForm.addEventListener("submit", function (e) {
        if (!validateForm()) {
          e.preventDefault();
          return false;
        }
      });
    }

    // Real-time validation
    if (codeInput) {
      codeInput.addEventListener("blur", validateCode);
    }

    if (nameInput) {
      nameInput.addEventListener("blur", validateName);
    }

    if (originalPriceInput && salePriceInput) {
      originalPriceInput.addEventListener("input", validatePrices);
      salePriceInput.addEventListener("input", validatePrices);
    }
  }

  /**
   * Validate toàn bộ form
   */
  function validateForm() {
    let isValid = true;

    // Validate required fields
    const requiredFields = [
      "code",
      "name",
      "coinAmount",
      "originalPrice",
      "salePrice",
      "status",
    ];
    requiredFields.forEach((fieldName) => {
      const field = document.getElementById(fieldName);
      if (field && !field.value.trim()) {
        showFieldError(field, "Trường này là bắt buộc");
        isValid = false;
      } else if (field) {
        clearFieldError(field);
      }
    });

    // Validate prices
    if (!validatePrices()) {
      isValid = false;
    }

    // Validate code format
    if (!validateCode()) {
      isValid = false;
    }

    // Validate name
    if (!validateName()) {
      isValid = false;
    }

    return isValid;
  }

  /**
   * Validate mã gói xu
   */
  function validateCode() {
    if (!codeInput) return true;

    const code = codeInput.value.trim();

    if (!code) {
      showFieldError(codeInput, "Mã gói xu là bắt buộc");
      return false;
    }

    if (code.length < 3) {
      showFieldError(codeInput, "Mã gói xu phải có ít nhất 3 ký tự");
      return false;
    }

    if (!/^[A-Z0-9-_]+$/.test(code)) {
      showFieldError(
        codeInput,
        "Mã gói xu chỉ được chứa chữ hoa, số, dấu gạch ngang và gạch dưới"
      );
      return false;
    }

    clearFieldError(codeInput);
    return true;
  }

  /**
   * Validate tên gói xu
   */
  function validateName() {
    if (!nameInput) return true;

    const name = nameInput.value.trim();

    if (!name) {
      showFieldError(nameInput, "Tên gói xu là bắt buộc");
      return false;
    }

    if (name.length < 3) {
      showFieldError(nameInput, "Tên gói xu phải có ít nhất 3 ký tự");
      return false;
    }

    clearFieldError(nameInput);
    return true;
  }

  /**
   * Validate giá cả
   */
  function validatePrices() {
    if (!originalPriceInput || !salePriceInput) return true;

    const originalPrice = parseFloat(originalPriceInput.value) || 0;
    const salePrice = parseFloat(salePriceInput.value) || 0;

    if (originalPrice <= 0) {
      showFieldError(originalPriceInput, "Giá gốc phải lớn hơn 0");
      return false;
    }

    if (salePrice <= 0) {
      showFieldError(salePriceInput, "Giá bán phải lớn hơn 0");
      return false;
    }

    if (salePrice > originalPrice) {
      showFieldError(salePriceInput, "Giá bán không được lớn hơn giá gốc");
      return false;
    }

    clearFieldError(originalPriceInput);
    clearFieldError(salePriceInput);
    return true;
  }

  /**
   * Thiết lập tính toán giá
   */
  function setupPriceCalculation() {
    if (originalPriceInput && salePriceInput) {
      originalPriceInput.addEventListener("input", calculateDiscount);
      salePriceInput.addEventListener("input", calculateDiscount);
    }
  }

  /**
   * Tính toán phần trăm khuyến mãi
   */
  function calculateDiscount() {
    if (!originalPriceInput || !salePriceInput || !discountPercentInput) return;

    const originalPrice = parseFloat(originalPriceInput.value) || 0;
    const salePrice = parseFloat(salePriceInput.value) || 0;

    if (originalPrice > 0 && salePrice > 0 && salePrice <= originalPrice) {
      const discount = ((originalPrice - salePrice) / originalPrice) * 100;
      discountPercentInput.value = discount.toFixed(2);
    }
  }

  /**
   * Thiết lập tạo mã tự động
   */
  function setupCodeGeneration() {
    if (generateCodeBtn && nameInput) {
      generateCodeBtn.addEventListener("click", function () {
        generateCodeFromName();
      });
    }
  }

  /**
   * Tạo mã từ tên gói xu
   */
  function generateCodeFromName() {
    if (!nameInput || !codeInput) return;

    const name = nameInput.value.trim();
    if (!name) {
      showToast("Vui lòng nhập tên gói xu trước", "warning");
      return;
    }

    // Tạo mã từ tên
    let code = name
      .toUpperCase()
      .replace(/[^A-Z0-9\s]/g, "") // Loại bỏ ký tự đặc biệt
      .replace(/\s+/g, "-") // Thay khoảng trắng bằng dấu gạch ngang
      .replace(/-+/g, "-") // Loại bỏ dấu gạch ngang liên tiếp
      .replace(/^-|-$/g, ""); // Loại bỏ dấu gạch ngang ở đầu và cuối

    // Thêm prefix COIN- nếu chưa có
    if (!code.startsWith("COIN-")) {
      code = "COIN-" + code;
    }

    // Thêm timestamp để đảm bảo unique
    const timestamp = Date.now().toString().slice(-4);
    code = code + "-" + timestamp;

    codeInput.value = code;

    // Trigger input event để kích hoạt validation
    const event = new Event("input", { bubbles: true });
    codeInput.dispatchEvent(event);

    showToast("Đã tạo mã gói xu tự động", "success");
  }

  /**
   * Hiển thị lỗi cho field
   */
  function showFieldError(field, message) {
    clearFieldError(field);

    const errorElement = document.createElement("span");
    errorElement.className = "error-message";
    errorElement.textContent = message;

    field.parentNode.appendChild(errorElement);
    field.classList.add("error");
  }

  /**
   * Xóa lỗi cho field
   */
  function clearFieldError(field) {
    const errorElement = field.parentNode.querySelector(".error-message");
    if (errorElement) {
      errorElement.remove();
    }
    field.classList.remove("error");
  }

  /**
   * Hiển thị toast message
   */
  function showToast(message, type = "info") {
    // Tạo toast element
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
            <div class="toast-content">
                <i class="fas fa-${getToastIcon(type)}"></i>
                <span>${message}</span>
            </div>
            <button class="toast-close">
                <i class="fas fa-times"></i>
            </button>
        `;

    // Thêm vào container
    const toastContainer =
      document.getElementById("toastContainer") || document.body;
    toastContainer.appendChild(toast);

    // Auto remove sau 5 giây
    setTimeout(() => {
      if (toast.parentNode) {
        toast.remove();
      }
    }, 5000);

    // Close button
    toast.querySelector(".toast-close").addEventListener("click", () => {
      toast.remove();
    });
  }

  /**
   * Lấy icon cho toast
   */
  function getToastIcon(type) {
    switch (type) {
      case "success":
        return "check-circle";
      case "error":
        return "exclamation-circle";
      case "warning":
        return "exclamation-triangle";
      default:
        return "info-circle";
    }
  }

  // Khởi tạo trang
  initializePage();
});
