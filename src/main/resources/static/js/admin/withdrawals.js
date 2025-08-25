/**
 * Withdrawal Management JavaScript
 * Handles form validation, AJAX requests, and UI interactions for withdrawal forms
 */

// Global variables
let isGeneratingCode = false;

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  initializeWithdrawalForm();
  initializeDeleteConfirmation();
  initializeLoadingStates();
  initializeErrorHandling();

  // Initialize page-specific functionality
  if (window.location.pathname.includes("/detail")) {
    initializeDetailPage();
  }

  if (
    window.location.pathname.includes("/index") ||
    window.location.pathname.endsWith("/withdrawals")
  ) {
    initializeIndexPage();
  }
});

/**
 * Initialize withdrawal form functionality
 */
function initializeWithdrawalForm() {
  // Auto-generate code if empty
  const codeField = document.getElementById("code");
  if (codeField && !codeField.value) {
    generateWithdrawalCode();
  }

  // Set default date to now
  const createdAtField = document.getElementById("createdAt");
  if (createdAtField && !createdAtField.value) {
    setDefaultDateTime();
  }

  // Initialize event listeners
  initializeEventListeners();
}

/**
 * Set default date time to current time
 */
function setDefaultDateTime() {
  const now = new Date();
  const localDateTime = new Date(
    now.getTime() - now.getTimezoneOffset() * 60000
  )
    .toISOString()
    .slice(0, 16);
  document.getElementById("createdAt").value = localDateTime;
}

/**
 * Initialize all event listeners
 */
function initializeEventListeners() {
  // User selection change handler
  const userSelect = document.getElementById("userId");
  if (userSelect) {
    userSelect.addEventListener("change", handleUserSelectionChange);
  }

  // Amount input handler
  const amountField = document.getElementById("amount");
  if (amountField) {
    amountField.addEventListener("input", handleAmountInput);
    amountField.addEventListener("blur", validateAmount);
    // Initialize amount converter
    updateAmountConverter();
  }

  // Payment method change handler
  const paymentMethodSelect = document.getElementById("paymentMethod");
  if (paymentMethodSelect) {
    paymentMethodSelect.addEventListener("change", handlePaymentMethodChange);
  }

  // Form submission handler
  const withdrawalForm = document.querySelector(".withdrawal-form");
  if (withdrawalForm) {
    withdrawalForm.addEventListener("submit", validateWithdrawalForm);
  }
}

/**
 * Generate withdrawal code via AJAX with retry mechanism
 */
function generateWithdrawalCode() {
  if (isGeneratingCode) return;

  isGeneratingCode = true;
  const generateBtn = document.querySelector(".generate-code-btn");
  const codeField = document.getElementById("code");

  // Update button state
  if (generateBtn) {
    showButtonLoading(generateBtn, "Đang tạo...");
  }

  // Use retry mechanism for better reliability
  retryRequest(
    () => {
      return fetch("/admin/withdrawals/generate-code", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Requested-With": "XMLHttpRequest",
          "X-CSRF-TOKEN": getCSRFToken(),
        },
      }).then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
      });
    },
    3,
    1000
  )
    .then((data) => {
      if (data.success) {
        if (codeField) {
          codeField.value = data.code;
          // Trigger change event for form validation
          codeField.dispatchEvent(new Event("change", { bubbles: true }));
        }
        showToast("Đã tạo mã withdrawal: " + data.code, "success");

        // Log for debugging
        console.log("Generated withdrawal code:", data.code);
      } else {
        throw new Error(data.message || "Không thể tạo mã withdrawal");
      }
    })
    .catch((error) => {
      console.error("Error generating code:", error);
      handleNetworkError(error, "khi tạo mã withdrawal");

      // Fallback: generate client-side code if server fails
      if (codeField && !codeField.value) {
        const fallbackCode = generateFallbackCode();
        codeField.value = fallbackCode;
        showToast("Đã tạo mã dự phòng: " + fallbackCode, "warning");
      }
    })
    .finally(() => {
      isGeneratingCode = false;
      if (generateBtn) {
        hideButtonLoading(generateBtn);
      }
    });
}

/**
 * Generate fallback code when server is unavailable
 */
function generateFallbackCode() {
  const timestamp = Date.now().toString(36);
  const random = Math.random().toString(36).substr(2, 5);
  return `WD${timestamp}${random}`.toUpperCase();
}

/**
 * Handle user selection change
 */
function handleUserSelectionChange(event) {
  const userSelect = event.target;
  const amountField = document.getElementById("amount");

  if (userSelect.value && amountField.value) {
    validateUserBalance();
  }

  // Clear any existing user-related errors
  clearFieldError(userSelect);
}

/**
 * Handle amount input changes
 */
function handleAmountInput(event) {
  const amountField = event.target;
  const amount = parseFloat(amountField.value);

  // Clear previous errors
  clearFieldError(amountField);

  // Format number with thousand separators
  if (amountField.value && !isNaN(amount)) {
    // Only format on blur to avoid cursor jumping
    if (event.type === "blur") {
      amountField.value = Math.floor(amount).toString();
    }
  }

  // Update amount converter
  updateAmountConverter();

  // Real-time validation
  if (amountField.value) {
    validateAmount();
    validateUserBalance();
  }
}

/**
 * Update amount converter display
 */
function updateAmountConverter() {
  const amountField = document.getElementById("amount");
  const vndAmountSpan = document.getElementById("vndAmount");
  
  if (amountField && vndAmountSpan) {
    const xuAmount = parseFloat(amountField.value) || 0;
    const vndAmount = xuAmount * 1000; // 1 xu = 1000 VND
    
    vndAmountSpan.textContent = new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(vndAmount);
  }
}

/**
 * Handle payment method change
 */
function handlePaymentMethodChange(event) {
  const paymentMethodSelect = event.target;
  clearFieldError(paymentMethodSelect);

  // You can add specific validation or UI changes based on payment method
  if (paymentMethodSelect.value) {
    console.log("Payment method selected:", paymentMethodSelect.value);
  }
}

/**
 * Validate amount field
 */
function validateAmount() {
  const amountField = document.getElementById("amount");
  const amount = parseFloat(amountField.value);

  if (!amountField.value) return true;

  if (isNaN(amount) || amount <= 0) {
    showFieldError(amountField, "Số xu phải là số dương");
    return false;
  }

  if (amount < 50) {
    showFieldError(amountField, "Số xu tối thiểu là 50 xu");
    return false;
  }

  if (amount > 50000) {
    showFieldError(amountField, "Số xu tối đa là 50,000 xu");
    return false;
  }

  return true;
}

/**
 * Validate user balance
 */
function validateUserBalance() {
  const userSelect = document.getElementById("userId");
  const amountField = document.getElementById("amount");
  const amount = parseFloat(amountField.value);

  if (!userSelect.value || !amountField.value || isNaN(amount)) {
    return true;
  }

  const selectedOption = userSelect.options[userSelect.selectedIndex];
  const balanceText = selectedOption.text;
  const balanceMatch = balanceText.match(/Số xu: ([\d,]+) xu/);

  if (balanceMatch) {
    const coinBalance = parseFloat(balanceMatch[1].replace(/,/g, ""));
    // For withdrawal validation, we might need to check business rules
    // For now, we'll just log the coin balance - you can add specific validation here
    console.log("User coin balance:", coinBalance, "xu");

    // Example: If you want to validate against coin balance (uncomment if needed)
    // if (amount > coinBalance * COIN_TO_VND_RATE) {
    //   showFieldError(amountField, "Số tiền rút vượt quá giá trị xu hiện có");
    //   return false;
    // }
  }

  return true;
}

/**
 * Comprehensive form validation with enhanced checks
 */
function validateWithdrawalForm(event) {
  const form = event.target;
  let isValid = true;
  const errors = [];

  // Clear all previous errors
  clearAllErrors();

  // Required fields validation
  const requiredFields = [
    { id: "userId", name: "Người dùng" },
    { id: "amount", name: "Số xu" },
    { id: "paymentMethod", name: "Phương thức thanh toán" },
    { id: "status", name: "Trạng thái" },
    { id: "createdAt", name: "Ngày tạo" },
  ];

  requiredFields.forEach((field) => {
    const element = document.getElementById(field.id);
    if (!element || !element.value.trim()) {
      showFieldError(element, field.name + " là bắt buộc");
      errors.push(field.name + " là bắt buộc");
      isValid = false;
    }
  });

  // Amount validation
  if (!validateAmount()) {
    errors.push("Số xu không hợp lệ");
    isValid = false;
  }

  // User balance validation
  if (!validateUserBalance()) {
    errors.push("Số dư không đủ");
    isValid = false;
  }

  // Date validation
  const createdAtField = document.getElementById("createdAt");
  if (createdAtField && createdAtField.value) {
    const selectedDate = new Date(createdAtField.value);
    const now = new Date();
    if (selectedDate > now) {
      showFieldError(createdAtField, "Ngày tạo không được trong tương lai");
      errors.push("Ngày tạo không hợp lệ");
      isValid = false;
    }

    // Check if date is too far in the past (more than 1 year)
    const oneYearAgo = new Date();
    oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
    if (selectedDate < oneYearAgo) {
      showFieldError(createdAtField, "Ngày tạo không được quá 1 năm trước");
      errors.push("Ngày tạo quá cũ");
      isValid = false;
    }
  }

  // Code validation
  const codeField = document.getElementById("code");
  if (codeField) {
    if (!codeField.value.trim()) {
      showFieldError(codeField, "Mã withdrawal là bắt buộc");
      errors.push("Mã withdrawal là bắt buộc");
      isValid = false;
    } else if (!/^WD[A-Z0-9]{6,20}$/i.test(codeField.value)) {
      showFieldError(
        codeField,
        "Mã withdrawal không đúng định dạng (WD + 6-20 ký tự)"
      );
      errors.push("Mã withdrawal không đúng định dạng");
      isValid = false;
    }
  }

  // Notes validation (if present)
  const notesField = document.getElementById("notes");
  if (notesField && notesField.value && notesField.value.length > 1000) {
    showFieldError(notesField, "Ghi chú không được vượt quá 1000 ký tự");
    errors.push("Ghi chú quá dài");
    isValid = false;
  }

  // Payment method specific validation
  const paymentMethodField = document.getElementById("paymentMethod");
  if (paymentMethodField && paymentMethodField.value) {
    const validPaymentMethods = [
      "BANK_TRANSFER",
      "E_WALLET",
      "CREDIT_CARD",
      "CASH",
    ];
    if (!validPaymentMethods.includes(paymentMethodField.value)) {
      showFieldError(paymentMethodField, "Phương thức thanh toán không hợp lệ");
      errors.push("Phương thức thanh toán không hợp lệ");
      isValid = false;
    }
  }

  // Status validation
  const statusField = document.getElementById("status");
  if (statusField && statusField.value) {
    const validStatuses = [
      "PENDING",
      "PROCESSING",
      "COMPLETED",
      "FAILED",
      "CANCELLED",
    ];
    if (!validStatuses.includes(statusField.value)) {
      showFieldError(statusField, "Trạng thái không hợp lệ");
      errors.push("Trạng thái không hợp lệ");
      isValid = false;
    }
  }

  // Daily limits validation (client-side check)
  if (!validateDailyLimits()) {
    errors.push("Vượt quá hạn mức rút tiền trong ngày");
    isValid = false;
  }

  // Business hours validation (example)
  if (!validateBusinessHours()) {
    errors.push("Chỉ có thể tạo withdrawal trong giờ làm việc");
    isValid = false;
  }

  if (!isValid) {
    event.preventDefault();

    // Show summary of errors
    const errorSummary =
      errors.length > 1
        ? `Có ${errors.length} lỗi cần khắc phục`
        : "Vui lòng kiểm tra lại thông tin đã nhập";

    showToast(errorSummary, "error", 5000);

    // Scroll to first error
    const firstError = document.querySelector(".error");
    if (firstError) {
      firstError.scrollIntoView({ behavior: "smooth", block: "center" });

      // Focus on the first error field
      const firstErrorField = firstError.querySelector(
        "input, select, textarea"
      );
      if (firstErrorField) {
        setTimeout(() => firstErrorField.focus(), 300);
      }
    }

    // Log errors for debugging
    console.warn("Form validation errors:", errors);
  } else {
    // Show loading state for valid form submission
    showLoadingOverlay("Đang xử lý withdrawal...");
  }

  return isValid;
}

/**
 * Validate daily limits (client-side approximation)
 */
function validateDailyLimits() {
  const amountField = document.getElementById("amount");
  const amount = parseFloat(amountField.value);

  if (!amount || isNaN(amount)) {
    return true; // Will be caught by amount validation
  }

  // Check maximum daily withdrawal amount
  const MAX_DAILY_WITHDRAWAL = 10000000; // 10M VND
  if (amount > MAX_DAILY_WITHDRAWAL) {
    showFieldError(
      amountField,
      `Số tiền rút tối đa trong ngày là ${formatCurrency(MAX_DAILY_WITHDRAWAL)}`
    );
    return false;
  }

  return true;
}

/**
 * Validate business hours (example business rule)
 */
function validateBusinessHours() {
  const now = new Date();
  const hour = now.getHours();
  const dayOfWeek = now.getDay(); // 0 = Sunday, 6 = Saturday

  // Example: Only allow withdrawals during business hours (8 AM - 6 PM, Mon-Fri)
  // This is just an example - you might want to remove this or adjust based on business needs
  if (dayOfWeek === 0 || dayOfWeek === 6) {
    // Weekend - allow but show warning
    showToast(
      "Lưu ý: Withdrawal được tạo vào cuối tuần có thể được xử lý chậm hơn",
      "warning",
      3000
    );
  }

  if (hour < 8 || hour >= 18) {
    // Outside business hours - allow but show warning
    showToast(
      "Lưu ý: Withdrawal được tạo ngoài giờ làm việc có thể được xử lý chậm hơn",
      "warning",
      3000
    );
  }

  return true; // Always return true for now, just show warnings
}

/**
 * Show field error message
 */
function showFieldError(element, message) {
  if (!element) return;

  element.classList.add("error");

  // Remove existing error message
  clearFieldError(element, false);

  // Add new error message
  const errorSpan = document.createElement("span");
  errorSpan.className = "error-message js-error";
  errorSpan.textContent = message;
  errorSpan.setAttribute("role", "alert");
  errorSpan.setAttribute("aria-live", "polite");

  // Insert after the element or in a designated error container
  const errorContainer = element.parentNode.querySelector(".error-container");
  if (errorContainer) {
    errorContainer.appendChild(errorSpan);
  } else {
    element.parentNode.appendChild(errorSpan);
  }

  // Add visual feedback
  element.setAttribute("aria-invalid", "true");
  element.setAttribute(
    "aria-describedby",
    errorSpan.id || "error-" + Date.now()
  );
}

/**
 * Show multiple field errors from server validation
 */
function showServerFieldErrors(fieldErrors) {
  if (!fieldErrors) return;

  Object.keys(fieldErrors).forEach((fieldName) => {
    const element =
      document.getElementById(fieldName) ||
      document.querySelector(`[name="${fieldName}"]`);

    if (
      element &&
      fieldErrors[fieldName] &&
      fieldErrors[fieldName].length > 0
    ) {
      // Show the first error for each field
      showFieldError(element, fieldErrors[fieldName][0]);
    }
  });
}

/**
 * Clear field error
 */
function clearFieldError(element, removeClass = true) {
  if (!element) return;

  if (removeClass) {
    element.classList.remove("error");
    element.removeAttribute("aria-invalid");
    element.removeAttribute("aria-describedby");
  }

  // Remove JS-generated error messages only
  const existingErrors = element.parentNode.querySelectorAll(
    ".error-message.js-error"
  );
  existingErrors.forEach((error) => error.remove());

  // Also check error container
  const errorContainer = element.parentNode.querySelector(".error-container");
  if (errorContainer) {
    const containerErrors = errorContainer.querySelectorAll(
      ".error-message.js-error"
    );
    containerErrors.forEach((error) => error.remove());
  }
}

/**
 * Clear all form errors
 */
function clearAllErrors() {
  document.querySelectorAll(".error").forEach((field) => {
    field.classList.remove("error");
  });

  document.querySelectorAll(".error-message.js-error").forEach((error) => {
    error.remove();
  });
}

/**
 * Reset withdrawal form
 */
function resetWithdrawalForm() {
  const form = document.querySelector(".withdrawal-form");
  if (!form) return;

  // Reset form fields
  form.reset();

  // Clear all errors
  clearAllErrors();

  // Reset to default values
  generateWithdrawalCode();
  setDefaultDateTime();

  showToast("Đã làm mới form", "info");
}

/**
 * Format currency with thousand separators
 */
function formatCurrency(amount) {
  return new Intl.NumberFormat("vi-VN").format(amount);
}

/**
 * Get CSRF token from meta tag or form
 */
function getCSRFToken() {
  // Try to get from meta tag first
  const metaToken = document.querySelector('meta[name="_csrf"]');
  if (metaToken) {
    return metaToken.getAttribute("content");
  }

  // Try to get from form input
  const inputToken = document.querySelector('input[name="_csrf"]');
  if (inputToken) {
    return inputToken.value;
  }

  return "";
}

/**
 * Show toast notification
 */
function showToast(message, type = "info", duration = 3000) {
  // Remove existing toasts
  document.querySelectorAll(".toast").forEach((toast) => {
    toast.remove();
  });

  // Create toast element
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${getToastIcon(type)}"></i>
            <span>${message}</span>
        </div>
    `;

  // Add to page
  document.body.appendChild(toast);

  // Show toast with animation
  requestAnimationFrame(() => {
    toast.classList.add("show");
  });

  // Remove toast after duration
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 300);
  }, duration);
}

/**
 * Get appropriate icon for toast type
 */
function getToastIcon(type) {
  switch (type) {
    case "success":
      return "fa-check-circle";
    case "error":
      return "fa-exclamation-circle";
    case "warning":
      return "fa-exclamation-triangle";
    case "info":
    default:
      return "fa-info-circle";
  }
}

/**
 * Debounce function for performance optimization
 */
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * Add note to withdrawal via AJAX with enhanced error handling
 */
function addNoteToWithdrawal(withdrawalId, noteContent) {
  return retryRequest(
    () => {
      return fetch(`/admin/withdrawals/${withdrawalId}/add-note`, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          "X-Requested-With": "XMLHttpRequest",
          "X-CSRF-TOKEN": getCSRFToken(),
        },
        body: `note=${encodeURIComponent(noteContent)}`,
      }).then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json ? response.json() : response;
      });
    },
    2,
    1000
  );
}

/**
 * Handle add note form submission
 */
function handleAddNoteSubmission(event) {
  event.preventDefault();

  const form = event.target;
  const formData = new FormData(form);
  const noteContent = formData.get("note");
  const withdrawalId = form.action.match(/\/(\d+)\/add-note/)[1];

  if (!noteContent.trim()) {
    showToast("Vui lòng nhập nội dung ghi chú", "error");
    return;
  }

  // Show loading state
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.innerHTML;
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang thêm...';

  // Submit via AJAX
  addNoteToWithdrawal(withdrawalId, noteContent)
    .then(() => {
      showToast("Đã thêm ghi chú thành công", "success");
      // Clear form
      form.querySelector("textarea").value = "";
      // Reload page to show new note
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    })
    .catch((error) => {
      console.error("Error adding note:", error);
      showToast("Lỗi khi thêm ghi chú", "error");
    })
    .finally(() => {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    });
}

/**
 * Clear note form
 */
function clearNoteForm() {
  const noteTextarea = document.getElementById("noteContent");
  if (noteTextarea) {
    noteTextarea.value = "";
    noteTextarea.focus();
  }
}

/**
 * Initialize detail page functionality
 */
function initializeDetailPage() {
  // Initialize AJAX form for adding notes
  const addNoteForm = document.getElementById("addNoteForm");
  if (addNoteForm) {
    addNoteForm.addEventListener("submit", handleAddNoteSubmission);
  }

  // Initialize quick status change forms
  const statusForms = document.querySelectorAll(".quick-actions form");
  statusForms.forEach((form) => {
    form.addEventListener("submit", function (event) {
      const status = form.querySelector('input[name="status"]').value;
      const statusText = status === "COMPLETED" ? "hoàn thành" : "thất bại";

      if (
        !confirm(
          `Bạn có chắc chắn muốn đánh dấu withdrawal này là ${statusText}?`
        )
      ) {
        event.preventDefault();
      }
    });
  });
}

/**
 * Print withdrawal details
 */
function printWithdrawal() {
  // Hide elements that shouldn't be printed
  const elementsToHide = document.querySelectorAll(
    ".action-buttons, .add-note-form, .quick-actions"
  );
  elementsToHide.forEach((el) => (el.style.display = "none"));

  // Print
  window.print();

  // Restore hidden elements
  elementsToHide.forEach((el) => (el.style.display = ""));
}

/**
 * Export withdrawal data to CSV
 */
function exportWithdrawal(withdrawalData) {
  if (!withdrawalData) {
    showToast("Không có dữ liệu để xuất", "error");
    return;
  }

  // Create CSV content
  const csvContent =
    "data:text/csv;charset=utf-8," +
    "Mã withdrawal,Số tiền,Trạng thái,Người dùng,Email,Ngày tạo,Cập nhật lần cuối\n" +
    `"${withdrawalData.code}","${withdrawalData.amount}","${
      withdrawalData.status
    }","${withdrawalData.user}","${withdrawalData.email}","${
      withdrawalData.createdAt
    }","${withdrawalData.updatedAt || "N/A"}"`;

  // Create and trigger download
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute(
    "download",
    `withdrawal_${withdrawalData.code}_${
      new Date().toISOString().split("T")[0]
    }.csv`
  );
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  showToast("Đã xuất file thành công", "success");
}

// Export functions for global access
window.generateWithdrawalCode = generateWithdrawalCode;
window.generateFallbackCode = generateFallbackCode;
window.resetWithdrawalForm = resetWithdrawalForm;
window.validateWithdrawalForm = validateWithdrawalForm;
window.showToast = showToast;
window.addNoteToWithdrawal = addNoteToWithdrawal;
window.clearNoteForm = clearNoteForm;
window.initializeDetailPage = initializeDetailPage;
window.printWithdrawal = printWithdrawal;
window.exportWithdrawal = exportWithdrawal;
/**
 * Edit Form Specific Functions
 */

/**
 * Initialize edit form specific features
 */
function initializeWithdrawalEditForm() {
  // Add change tracking
  const form = document.querySelector(".withdrawal-form");
  if (!form) return;

  const originalData = new FormData(form);

  // Track changes
  form.addEventListener("change", function () {
    const currentData = new FormData(form);
    let hasChanges = false;

    for (let [key, value] of currentData.entries()) {
      if (originalData.get(key) !== value) {
        hasChanges = true;
        break;
      }
    }

    // Show unsaved changes indicator
    if (hasChanges) {
      document.body.classList.add("has-unsaved-changes");
    } else {
      document.body.classList.remove("has-unsaved-changes");
    }
  });

  // Warn before leaving with unsaved changes
  window.addEventListener("beforeunload", function (e) {
    if (document.body.classList.contains("has-unsaved-changes")) {
      e.preventDefault();
      e.returnValue =
        "Bạn có thay đổi chưa được lưu. Bạn có chắc chắn muốn rời khỏi trang?";
    }
  });
}

/**
 * Validate edit form with business rules
 */
function validateWithdrawalEditForm(event) {
  const isCompleted =
    event.target.querySelector('input[name="status"]')?.value === "COMPLETED";

  // Basic validation
  if (!validateWithdrawalForm(event)) {
    return false;
  }

  // Additional validation for completed withdrawals
  if (isCompleted) {
    const confirmation = confirm(
      "Bạn có chắc chắn muốn cập nhật withdrawal đã hoàn thành? " +
        "Điều này có thể ảnh hưởng đến báo cáo tài chính."
    );
    if (!confirmation) {
      event.preventDefault();
      return false;
    }
  }

  return true;
}

/**
 * Reset form to original values
 */
function resetWithdrawalEditForm() {
  if (confirm("Bạn có chắc chắn muốn khôi phục form về trạng thái ban đầu?")) {
    location.reload();
  }
}

/**
 * Show warning for completed withdrawals
 */
function showCompletedWithdrawalWarning() {
  // Add visual indicators for restricted fields
  const restrictedFields = document.querySelectorAll(".disabled-field");
  restrictedFields.forEach((field) => {
    field.style.backgroundColor = "#f8f9fa";
    field.style.borderColor = "#dee2e6";
  });

  // Show tooltip on hover for disabled fields
  restrictedFields.forEach((field) => {
    field.title = "Trường này không thể chỉnh sửa cho withdrawal đã hoàn thành";
  });
}

/**
 * Validate status change for completed withdrawals
 */
function validateStatusChange(currentStatus, newStatus) {
  if (currentStatus === "COMPLETED") {
    const allowedTransitions = ["CANCELLED"];
    if (!allowedTransitions.includes(newStatus)) {
      showToast(
        'Withdrawal đã hoàn thành chỉ có thể chuyển sang trạng thái "Hủy"',
        "warning"
      );
      return false;
    }

    if (newStatus === "CANCELLED") {
      return confirm(
        "Bạn có chắc chắn muốn hủy withdrawal đã hoàn thành? " +
          "Điều này sẽ ảnh hưởng đến báo cáo tài chính và có thể cần xử lý hoàn tiền."
      );
    }
  }

  return true;
}

/**
 * Handle status field change in edit form
 */
function handleEditStatusChange(event) {
  const statusSelect = event.target;
  const currentStatus =
    statusSelect.dataset.originalValue || statusSelect.defaultValue;
  const newStatus = statusSelect.value;

  if (!validateStatusChange(currentStatus, newStatus)) {
    statusSelect.value = currentStatus;
    return false;
  }

  // Show confirmation for critical status changes
  const criticalChanges = {
    COMPLETED: "hoàn thành",
    FAILED: "thất bại",
    CANCELLED: "hủy",
  };

  if (criticalChanges[newStatus] && currentStatus !== newStatus) {
    const confirmation = confirm(
      `Bạn có chắc chắn muốn đánh dấu withdrawal này là "${criticalChanges[newStatus]}"?`
    );

    if (!confirmation) {
      statusSelect.value = currentStatus;
      return false;
    }
  }

  return true;
}

/**
 * Initialize edit form event listeners
 */
function initializeEditFormListeners() {
  // Status change handler
  const statusSelect = document.getElementById("status");
  if (statusSelect) {
    // Store original value
    statusSelect.dataset.originalValue = statusSelect.value;
    statusSelect.addEventListener("change", handleEditStatusChange);
  }

  // Amount change handler for completed withdrawals
  const amountField = document.getElementById("amount");
  if (amountField && amountField.disabled) {
    amountField.addEventListener("focus", function () {
      showToast(
        "Không thể thay đổi số tiền cho withdrawal đã hoàn thành",
        "warning"
      );
      amountField.blur();
    });
  }

  // User change handler for completed withdrawals
  const userSelect = document.getElementById("userId");
  if (userSelect && userSelect.disabled) {
    userSelect.addEventListener("focus", function () {
      showToast(
        "Không thể thay đổi người dùng cho withdrawal đã hoàn thành",
        "warning"
      );
      userSelect.blur();
    });
  }
}

/**
 * Auto-save draft changes (optional feature)
 */
function autoSaveDraft() {
  const form = document.querySelector(".withdrawal-form");
  if (!form) return;

  const formData = new FormData(form);
  const draftData = {};

  for (let [key, value] of formData.entries()) {
    draftData[key] = value;
  }

  // Save to localStorage
  const withdrawalId = form.action.match(/\/(\d+)\/edit/)?.[1];
  if (withdrawalId) {
    localStorage.setItem(
      `withdrawal_draft_${withdrawalId}`,
      JSON.stringify(draftData)
    );
  }
}

/**
 * Load draft changes
 */
function loadDraft() {
  const form = document.querySelector(".withdrawal-form");
  if (!form) return;

  const withdrawalId = form.action.match(/\/(\d+)\/edit/)?.[1];
  if (!withdrawalId) return;

  const draftData = localStorage.getItem(`withdrawal_draft_${withdrawalId}`);
  if (!draftData) return;

  try {
    const data = JSON.parse(draftData);

    // Ask user if they want to restore draft
    if (confirm("Có bản nháp chưa lưu. Bạn có muốn khôi phục không?")) {
      Object.keys(data).forEach((key) => {
        const field = form.querySelector(`[name="${key}"]`);
        if (field && !field.disabled && !field.readOnly) {
          field.value = data[key];
        }
      });

      showToast("Đã khôi phục bản nháp", "info");
    }

    // Clear draft after loading
    localStorage.removeItem(`withdrawal_draft_${withdrawalId}`);
  } catch (error) {
    console.error("Error loading draft:", error);
  }
}

/**
 * Clear draft data
 */
function clearDraft() {
  const form = document.querySelector(".withdrawal-form");
  if (!form) return;

  const withdrawalId = form.action.match(/\/(\d+)\/edit/)?.[1];
  if (withdrawalId) {
    localStorage.removeItem(`withdrawal_draft_${withdrawalId}`);
  }
}

/**
 * Show field change history (if available)
 */
function showFieldHistory(fieldName, history) {
  if (!history || history.length === 0) {
    showToast("Không có lịch sử thay đổi cho trường này", "info");
    return;
  }

  let historyHtml = '<div class="field-history">';
  historyHtml += `<h4>Lịch sử thay đổi: ${fieldName}</h4>`;

  history.forEach((change) => {
    historyHtml += `
      <div class="history-entry">
        <div class="history-time">${change.timestamp}</div>
        <div class="history-change">
          <span class="old-value">${change.oldValue}</span>
          <i class="fas fa-arrow-right"></i>
          <span class="new-value">${change.newValue}</span>
        </div>
        <div class="history-user">bởi ${change.user}</div>
      </div>
    `;
  });

  historyHtml += "</div>";

  // Show in modal or toast
  showToast(historyHtml, "info", 10000);
}

/**
 * Export edit form data for backup
 */
function exportEditFormData() {
  const form = document.querySelector(".withdrawal-form");
  if (!form) return;

  const formData = new FormData(form);
  const exportData = {};

  for (let [key, value] of formData.entries()) {
    exportData[key] = value;
  }

  // Add metadata
  exportData._metadata = {
    exportedAt: new Date().toISOString(),
    url: window.location.href,
    userAgent: navigator.userAgent,
  };

  // Create and download JSON file
  const dataStr = JSON.stringify(exportData, null, 2);
  const dataBlob = new Blob([dataStr], { type: "application/json" });

  const link = document.createElement("a");
  link.href = URL.createObjectURL(dataBlob);
  link.download = `withdrawal_edit_backup_${Date.now()}.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  showToast("Đã xuất dữ liệu form thành công", "success");
}

// Export edit-specific functions for global access
window.initializeWithdrawalEditForm = initializeWithdrawalEditForm;
window.validateWithdrawalEditForm = validateWithdrawalEditForm;
window.resetWithdrawalEditForm = resetWithdrawalEditForm;
window.showCompletedWithdrawalWarning = showCompletedWithdrawalWarning;
window.validateStatusChange = validateStatusChange;
window.handleEditStatusChange = handleEditStatusChange;
window.initializeEditFormListeners = initializeEditFormListeners;
window.autoSaveDraft = autoSaveDraft;
window.loadDraft = loadDraft;
window.clearDraft = clearDraft;
window.showFieldHistory = showFieldHistory;
window.exportEditFormData = exportEditFormData;

/**
 * Delete Confirmation and Actions
 */

/**
 * Initialize delete confirmation dialogs
 */
function initializeDeleteConfirmation() {
  // Handle delete buttons
  const deleteButtons = document.querySelectorAll(
    '.btn-delete, .delete-btn, [data-action="delete"]'
  );
  deleteButtons.forEach((button) => {
    button.addEventListener("click", handleDeleteConfirmation);
  });

  // Handle delete forms
  const deleteForms = document.querySelectorAll(
    '.delete-form, form[action*="/delete"]'
  );
  deleteForms.forEach((form) => {
    form.addEventListener("submit", handleDeleteFormSubmission);
  });

  // Handle bulk delete actions
  const bulkDeleteBtn = document.getElementById("bulkDeleteBtn");
  if (bulkDeleteBtn) {
    bulkDeleteBtn.addEventListener("click", handleBulkDelete);
  }
}

/**
 * Handle delete confirmation dialog
 */
function handleDeleteConfirmation(event) {
  event.preventDefault();

  const button = event.target.closest("button, a");
  const withdrawalCode = button.dataset.withdrawalCode || "N/A";
  const withdrawalAmount = button.dataset.withdrawalAmount || "N/A";
  const withdrawalStatus = button.dataset.withdrawalStatus || "N/A";

  // Create custom confirmation dialog
  const confirmDialog = createDeleteConfirmationDialog({
    code: withdrawalCode,
    amount: withdrawalAmount,
    status: withdrawalStatus,
    isCompleted: withdrawalStatus === "COMPLETED",
  });

  document.body.appendChild(confirmDialog);

  // Handle confirmation
  const confirmBtn = confirmDialog.querySelector(".confirm-delete");
  const cancelBtn = confirmDialog.querySelector(".cancel-delete");

  confirmBtn.addEventListener("click", () => {
    const reason = confirmDialog.querySelector("#deleteReason").value.trim();
    if (!reason) {
      showToast("Vui lòng nhập lý do xóa", "error");
      return;
    }

    // Proceed with deletion
    proceedWithDeletion(button, reason);
    document.body.removeChild(confirmDialog);
  });

  cancelBtn.addEventListener("click", () => {
    document.body.removeChild(confirmDialog);
  });
}

/**
 * Create delete confirmation dialog
 */
function createDeleteConfirmationDialog(withdrawal) {
  const dialog = document.createElement("div");
  dialog.className = "delete-confirmation-modal";

  const warningClass = withdrawal.isCompleted
    ? "warning-severe"
    : "warning-normal";
  const warningText = withdrawal.isCompleted
    ? "CẢNH BÁO: Withdrawal này đã hoàn thành. Việc xóa có thể ảnh hưởng đến báo cáo tài chính!"
    : "Bạn có chắc chắn muốn xóa withdrawal này?";

  dialog.innerHTML = `
    <div class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-exclamation-triangle"></i> Xác nhận xóa</h3>
        </div>
        <div class="modal-body">
          <div class="withdrawal-info">
            <p><strong>Mã withdrawal:</strong> ${withdrawal.code}</p>
            <p><strong>Số tiền:</strong> ${formatCurrency(
              withdrawal.amount
            )} VND</p>
            <p><strong>Trạng thái:</strong> <span class="status-${withdrawal.status.toLowerCase()}">${
    withdrawal.status
  }</span></p>
          </div>
          <div class="warning-message ${warningClass}">
            <i class="fas fa-exclamation-triangle"></i>
            ${warningText}
          </div>
          <div class="form-group">
            <label for="deleteReason">Lý do xóa <span class="required">*</span></label>
            <textarea id="deleteReason" class="form-control" rows="3" 
                      placeholder="Nhập lý do xóa withdrawal này..." required></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary cancel-delete">
            <i class="fas fa-times"></i> Hủy
          </button>
          <button type="button" class="btn btn-danger confirm-delete">
            <i class="fas fa-trash"></i> Xác nhận xóa
          </button>
        </div>
      </div>
    </div>
  `;

  return dialog;
}

/**
 * Proceed with deletion
 */
function proceedWithDeletion(button, reason) {
  const deleteUrl = button.href || button.dataset.deleteUrl;
  if (!deleteUrl) {
    showToast("Không tìm thấy URL xóa", "error");
    return;
  }

  // Show loading state
  showLoadingOverlay("Đang xóa withdrawal...");

  // Create form and submit
  const form = document.createElement("form");
  form.method = "POST";
  form.action = deleteUrl;

  // Add CSRF token
  const csrfInput = document.createElement("input");
  csrfInput.type = "hidden";
  csrfInput.name = "_csrf";
  csrfInput.value = getCSRFToken();
  form.appendChild(csrfInput);

  // Add delete reason
  const reasonInput = document.createElement("input");
  reasonInput.type = "hidden";
  reasonInput.name = "deleteReason";
  reasonInput.value = reason;
  form.appendChild(reasonInput);

  document.body.appendChild(form);
  form.submit();
}

/**
 * Handle delete form submission
 */
function handleDeleteFormSubmission(event) {
  const form = event.target;
  const reasonField = form.querySelector('[name="deleteReason"]');

  if (!reasonField || !reasonField.value.trim()) {
    event.preventDefault();
    showToast("Vui lòng nhập lý do xóa", "error");
    reasonField?.focus();
    return false;
  }

  // Show loading state
  showLoadingOverlay("Đang xóa withdrawal...");
  return true;
}

/**
 * Handle bulk delete operations
 */
function handleBulkDelete(event) {
  event.preventDefault();

  const selectedCheckboxes = document.querySelectorAll(
    ".withdrawal-checkbox:checked"
  );
  if (selectedCheckboxes.length === 0) {
    showToast("Vui lòng chọn ít nhất một withdrawal để xóa", "warning");
    return;
  }

  const selectedIds = Array.from(selectedCheckboxes).map((cb) => cb.value);
  const confirmMessage = `Bạn có chắc chắn muốn xóa ${selectedIds.length} withdrawal đã chọn?`;

  if (!confirm(confirmMessage)) {
    return;
  }

  const reason = prompt("Nhập lý do xóa:");
  if (!reason || !reason.trim()) {
    showToast("Vui lòng nhập lý do xóa", "error");
    return;
  }

  // Show loading state
  showLoadingOverlay(`Đang xóa ${selectedIds.length} withdrawal...`);

  // Submit bulk delete
  const form = document.createElement("form");
  form.method = "POST";
  form.action = "/admin/withdrawals/bulk-delete";

  // Add CSRF token
  const csrfInput = document.createElement("input");
  csrfInput.type = "hidden";
  csrfInput.name = "_csrf";
  csrfInput.value = getCSRFToken();
  form.appendChild(csrfInput);

  // Add selected IDs
  selectedIds.forEach((id) => {
    const idInput = document.createElement("input");
    idInput.type = "hidden";
    idInput.name = "ids";
    idInput.value = id;
    form.appendChild(idInput);
  });

  // Add reason
  const reasonInput = document.createElement("input");
  reasonInput.type = "hidden";
  reasonInput.name = "deleteReason";
  reasonInput.value = reason.trim();
  form.appendChild(reasonInput);

  document.body.appendChild(form);
  form.submit();
}

/**
 * Loading States and UI Feedback
 */

/**
 * Initialize loading states
 */
function initializeLoadingStates() {
  // Add loading states to all forms
  const forms = document.querySelectorAll("form");
  forms.forEach((form) => {
    form.addEventListener("submit", function (event) {
      if (form.classList.contains("no-loading")) return;

      const submitBtn = form.querySelector(
        'button[type="submit"], input[type="submit"]'
      );
      if (submitBtn) {
        showButtonLoading(submitBtn);
      }
    });
  });

  // Add loading states to AJAX buttons
  const ajaxButtons = document.querySelectorAll('[data-ajax="true"]');
  ajaxButtons.forEach((button) => {
    button.addEventListener("click", function () {
      showButtonLoading(button);
    });
  });
}

/**
 * Show button loading state
 */
function showButtonLoading(button, loadingText = "Đang xử lý...") {
  if (button.dataset.originalText) return; // Already in loading state

  button.dataset.originalText = button.innerHTML;
  button.disabled = true;
  button.classList.add("loading");

  const icon = '<i class="fas fa-spinner fa-spin"></i>';
  button.innerHTML = `${icon} ${loadingText}`;
}

/**
 * Hide button loading state
 */
function hideButtonLoading(button) {
  if (!button.dataset.originalText) return;

  button.innerHTML = button.dataset.originalText;
  button.disabled = false;
  button.classList.remove("loading");
  delete button.dataset.originalText;
}

/**
 * Show loading overlay
 */
function showLoadingOverlay(message = "Đang xử lý...") {
  // Remove existing overlay
  hideLoadingOverlay();

  const overlay = document.createElement("div");
  overlay.id = "loadingOverlay";
  overlay.className = "loading-overlay";
  overlay.innerHTML = `
    <div class="loading-content">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
      </div>
      <div class="loading-message">${message}</div>
    </div>
  `;

  document.body.appendChild(overlay);

  // Auto-hide after 30 seconds as fallback
  setTimeout(() => {
    hideLoadingOverlay();
  }, 30000);
}

/**
 * Hide loading overlay
 */
function hideLoadingOverlay() {
  const overlay = document.getElementById("loadingOverlay");
  if (overlay) {
    overlay.remove();
  }
}

/**
 * Enhanced Error Handling
 */

/**
 * Initialize error handling
 */
function initializeErrorHandling() {
  // Global error handler for unhandled promise rejections
  window.addEventListener("unhandledrejection", function (event) {
    console.error("Unhandled promise rejection:", event.reason);
    showToast("Đã xảy ra lỗi không mong muốn", "error");
    hideLoadingOverlay();
  });

  // Global error handler for JavaScript errors
  window.addEventListener("error", function (event) {
    console.error("JavaScript error:", event.error);
    if (event.error && event.error.message) {
      showToast("Lỗi JavaScript: " + event.error.message, "error");
    }
    hideLoadingOverlay();
  });

  // Handle AJAX errors
  document.addEventListener("ajaxError", function (event) {
    console.error("AJAX error:", event.detail);
    showToast("Lỗi kết nối mạng", "error");
    hideLoadingOverlay();
  });

  // Handle form validation errors
  const forms = document.querySelectorAll("form");
  forms.forEach((form) => {
    form.addEventListener(
      "invalid",
      function (event) {
        event.preventDefault();
        const firstInvalidField = form.querySelector(":invalid");
        if (firstInvalidField) {
          firstInvalidField.scrollIntoView({
            behavior: "smooth",
            block: "center",
          });
          firstInvalidField.focus();
          showToast("Vui lòng kiểm tra lại thông tin đã nhập", "error");
        }
      },
      true
    );
  });
}

/**
 * Handle network errors
 */
function handleNetworkError(error, context = "") {
  console.error(`Network error ${context}:`, error);

  let message = "Lỗi kết nối mạng";

  if (error.name === "TypeError" && error.message.includes("fetch")) {
    message = "Không thể kết nối đến server";
  } else if (error.status) {
    switch (error.status) {
      case 400:
        message = "Dữ liệu không hợp lệ";
        break;
      case 401:
        message = "Phiên đăng nhập đã hết hạn";
        break;
      case 403:
        message = "Không có quyền thực hiện thao tác này";
        break;
      case 404:
        message = "Không tìm thấy dữ liệu";
        break;
      case 500:
        message = "Lỗi server nội bộ";
        break;
      default:
        message = `Lỗi HTTP ${error.status}`;
    }
  }

  showToast(message, "error");
  hideLoadingOverlay();
}

/**
 * Retry mechanism for failed requests
 */
function retryRequest(requestFn, maxRetries = 3, delay = 1000) {
  return new Promise((resolve, reject) => {
    let retries = 0;

    function attempt() {
      requestFn()
        .then(resolve)
        .catch((error) => {
          retries++;
          if (retries < maxRetries) {
            showToast(`Thử lại lần ${retries}/${maxRetries}...`, "info", 2000);
            setTimeout(attempt, delay * retries);
          } else {
            reject(error);
          }
        });
    }

    attempt();
  });
}

/**
 * Index Page Specific Functions
 */

/**
 * Initialize index page functionality
 */
function initializeIndexPage() {
  initializeSearch();
  initializeFilters();
  initializeBulkActions();
  initializePagination();
  initializeTableSorting();
  initializeRefreshButton();
}

/**
 * Initialize search functionality
 */
function initializeSearch() {
  const searchInput = document.getElementById("searchInput");
  const searchForm = document.getElementById("searchForm");

  if (searchInput) {
    // Debounced search
    const debouncedSearch = debounce(function () {
      if (searchForm) {
        searchForm.submit();
      }
    }, 500);

    searchInput.addEventListener("input", debouncedSearch);

    // Clear search
    const clearSearchBtn = document.getElementById("clearSearch");
    if (clearSearchBtn) {
      clearSearchBtn.addEventListener("click", function () {
        searchInput.value = "";
        if (searchForm) {
          searchForm.submit();
        }
      });
    }
  }
}

/**
 * Initialize filter functionality
 */
function initializeFilters() {
  const filterForm = document.getElementById("filterForm");
  const filterInputs = document.querySelectorAll(".filter-input");

  filterInputs.forEach((input) => {
    input.addEventListener("change", function () {
      if (filterForm) {
        showLoadingOverlay("Đang lọc dữ liệu...");
        filterForm.submit();
      }
    });
  });

  // Clear filters
  const clearFiltersBtn = document.getElementById("clearFilters");
  if (clearFiltersBtn) {
    clearFiltersBtn.addEventListener("click", function () {
      filterInputs.forEach((input) => {
        if (input.type === "checkbox" || input.type === "radio") {
          input.checked = false;
        } else {
          input.value = "";
        }
      });

      if (filterForm) {
        showLoadingOverlay("Đang xóa bộ lọc...");
        filterForm.submit();
      }
    });
  }
}

/**
 * Initialize bulk actions
 */
function initializeBulkActions() {
  const selectAllCheckbox = document.getElementById("selectAll");
  const itemCheckboxes = document.querySelectorAll(".withdrawal-checkbox");
  const bulkActionButtons = document.querySelectorAll(".bulk-action-btn");

  // Select all functionality
  if (selectAllCheckbox) {
    selectAllCheckbox.addEventListener("change", function () {
      itemCheckboxes.forEach((checkbox) => {
        checkbox.checked = selectAllCheckbox.checked;
      });
      updateBulkActionButtons();
    });
  }

  // Individual checkbox change
  itemCheckboxes.forEach((checkbox) => {
    checkbox.addEventListener("change", function () {
      updateSelectAllState();
      updateBulkActionButtons();
    });
  });

  // Bulk action buttons
  bulkActionButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      const selectedCount = document.querySelectorAll(
        ".withdrawal-checkbox:checked"
      ).length;
      if (selectedCount === 0) {
        event.preventDefault();
        showToast("Vui lòng chọn ít nhất một withdrawal", "warning");
      }
    });
  });
}

/**
 * Update select all checkbox state
 */
function updateSelectAllState() {
  const selectAllCheckbox = document.getElementById("selectAll");
  const itemCheckboxes = document.querySelectorAll(".withdrawal-checkbox");

  if (!selectAllCheckbox || itemCheckboxes.length === 0) return;

  const checkedCount = document.querySelectorAll(
    ".withdrawal-checkbox:checked"
  ).length;

  if (checkedCount === 0) {
    selectAllCheckbox.checked = false;
    selectAllCheckbox.indeterminate = false;
  } else if (checkedCount === itemCheckboxes.length) {
    selectAllCheckbox.checked = true;
    selectAllCheckbox.indeterminate = false;
  } else {
    selectAllCheckbox.checked = false;
    selectAllCheckbox.indeterminate = true;
  }
}

/**
 * Update bulk action buttons state
 */
function updateBulkActionButtons() {
  const selectedCount = document.querySelectorAll(
    ".withdrawal-checkbox:checked"
  ).length;
  const bulkActionButtons = document.querySelectorAll(".bulk-action-btn");
  const selectedCountDisplay = document.getElementById("selectedCount");

  bulkActionButtons.forEach((button) => {
    button.disabled = selectedCount === 0;
  });

  if (selectedCountDisplay) {
    selectedCountDisplay.textContent = selectedCount;
  }
}

/**
 * Initialize pagination
 */
function initializePagination() {
  const paginationLinks = document.querySelectorAll(".pagination a");

  paginationLinks.forEach((link) => {
    link.addEventListener("click", function (event) {
      // Don't show loading for disabled links
      if (link.classList.contains("disabled")) {
        event.preventDefault();
        return;
      }

      showLoadingOverlay("Đang tải trang...");
    });
  });
}

/**
 * Initialize table sorting
 */
function initializeTableSorting() {
  const sortableHeaders = document.querySelectorAll(".sortable");

  sortableHeaders.forEach((header) => {
    header.addEventListener("click", function () {
      showLoadingOverlay("Đang sắp xếp...");
    });
  });
}

/**
 * Initialize refresh button
 */
function initializeRefreshButton() {
  const refreshBtn = document.getElementById("refreshBtn");

  if (refreshBtn) {
    refreshBtn.addEventListener("click", function () {
      showLoadingOverlay("Đang làm mới dữ liệu...");
      window.location.reload();
    });
  }
}

// Export new functions for global access
window.initializeDeleteConfirmation = initializeDeleteConfirmation;
window.handleDeleteConfirmation = handleDeleteConfirmation;
window.createDeleteConfirmationDialog = createDeleteConfirmationDialog;
window.proceedWithDeletion = proceedWithDeletion;
window.handleDeleteFormSubmission = handleDeleteFormSubmission;
window.handleBulkDelete = handleBulkDelete;
window.initializeLoadingStates = initializeLoadingStates;
window.showButtonLoading = showButtonLoading;
window.hideButtonLoading = hideButtonLoading;
window.showLoadingOverlay = showLoadingOverlay;
window.hideLoadingOverlay = hideLoadingOverlay;
window.initializeErrorHandling = initializeErrorHandling;
window.handleNetworkError = handleNetworkError;
window.retryRequest = retryRequest;
window.initializeIndexPage = initializeIndexPage;
window.initializeSearch = initializeSearch;
window.initializeFilters = initializeFilters;
window.initializeBulkActions = initializeBulkActions;
window.updateSelectAllState = updateSelectAllState;
window.updateBulkActionButtons = updateBulkActionButtons;
window.initializePagination = initializePagination;
window.initializeTableSorting = initializeTableSorting;
window.initializeRefreshButton = initializeRefreshButton;

// Auto-initialize edit form features when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  // Check if we're on an edit page
  if (window.location.pathname.includes("/edit")) {
    initializeEditFormListeners();
    loadDraft();

    // Auto-save every 30 seconds
    setInterval(autoSaveDraft, 30000);

    // Clear draft on successful form submission
    const form = document.querySelector(".withdrawal-form");
    if (form) {
      form.addEventListener("submit", function (event) {
        if (validateWithdrawalEditForm(event)) {
          clearDraft();
        }
      });
    }
  }
});

/**
 * Enhanced Error Handling and Loading States
 */

/**
 * Show loading overlay
 */
function showLoadingOverlay(message = "Đang xử lý...") {
  // Remove existing overlay
  hideLoadingOverlay();

  const overlay = document.createElement("div");
  overlay.className = "loading-overlay";
  overlay.innerHTML = `
    <div class="loading-content">
      <div class="loading-spinner"></div>
      <div class="loading-message">${message}</div>
    </div>
  `;

  document.body.appendChild(overlay);
  document.body.classList.add("loading");

  // Auto-hide after 30 seconds to prevent permanent loading state
  setTimeout(() => {
    hideLoadingOverlay();
    showToast(
      "Thao tác đang mất nhiều thời gian. Vui lòng kiểm tra lại.",
      "warning"
    );
  }, 30000);
}

/**
 * Hide loading overlay
 */
function hideLoadingOverlay() {
  const overlay = document.querySelector(".loading-overlay");
  if (overlay) {
    overlay.remove();
  }
  document.body.classList.remove("loading");
}

/**
 * Show button loading state
 */
function showButtonLoading(button, loadingText = "Đang xử lý...") {
  if (!button) return;

  button.disabled = true;
  button.dataset.originalText = button.innerHTML;
  button.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${loadingText}`;
  button.classList.add("loading");
}

/**
 * Hide button loading state
 */
function hideButtonLoading(button) {
  if (!button) return;

  button.disabled = false;
  button.innerHTML = button.dataset.originalText || button.innerHTML;
  button.classList.remove("loading");
  delete button.dataset.originalText;
}

/**
 * Enhanced network error handling
 */
function handleNetworkError(error, context = "") {
  console.error("Network error" + (context ? " " + context : ""), error);

  let message = "Đã xảy ra lỗi kết nối";
  let type = "error";

  if (error.name === "TypeError" && error.message.includes("fetch")) {
    message = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.";
  } else if (error.message.includes("timeout")) {
    message = "Kết nối bị timeout. Vui lòng thử lại.";
  } else if (error.message.includes("HTTP 500")) {
    message = "Lỗi máy chủ nội bộ. Vui lòng thử lại sau.";
  } else if (error.message.includes("HTTP 404")) {
    message = "Không tìm thấy tài nguyên yêu cầu.";
  } else if (error.message.includes("HTTP 403")) {
    message = "Bạn không có quyền thực hiện thao tác này.";
  } else if (error.message.includes("HTTP 401")) {
    message = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    type = "warning";
    // Redirect to login after showing message
    setTimeout(() => {
      window.location.href = "/login";
    }, 3000);
  }

  showToast(message, type, 5000);
}

/**
 * Retry mechanism for network requests
 */
function retryRequest(requestFn, maxRetries = 3, delay = 1000) {
  return new Promise((resolve, reject) => {
    let retries = 0;

    function attempt() {
      requestFn()
        .then(resolve)
        .catch((error) => {
          retries++;
          if (retries < maxRetries) {
            console.warn(
              `Request failed, retrying (${retries}/${maxRetries})...`
            );
            setTimeout(attempt, delay * retries); // Exponential backoff
          } else {
            reject(error);
          }
        });
    }

    attempt();
  });
}

/**
 * Initialize error handling for forms
 */
function initializeErrorHandling() {
  // Handle server-side validation errors
  const fieldErrors = window.fieldErrors;
  if (fieldErrors) {
    showServerFieldErrors(fieldErrors);
  }

  // Handle global error messages
  const errorMessage = document.querySelector(".alert-danger");
  if (errorMessage) {
    const message = errorMessage.textContent.trim();
    if (message) {
      showToast(message, "error", 5000);
    }
  }

  // Handle success messages
  const successMessage = document.querySelector(".alert-success");
  if (successMessage) {
    const message = successMessage.textContent.trim();
    if (message) {
      showToast(message, "success", 3000);
    }
  }

  // Handle warning messages
  const warningMessage = document.querySelector(".alert-warning");
  if (warningMessage) {
    const message = warningMessage.textContent.trim();
    if (message) {
      showToast(message, "warning", 4000);
    }
  }
}

/**
 * Initialize loading states
 */
function initializeLoadingStates() {
  // Add loading state to all form submissions
  document.querySelectorAll("form").forEach((form) => {
    form.addEventListener("submit", function (event) {
      // Only show loading if form validation passes
      if (!form.checkValidity || form.checkValidity()) {
        const submitButton = form.querySelector('button[type="submit"]');
        if (submitButton) {
          showButtonLoading(submitButton);
        }
      }
    });
  });

  // Add loading state to AJAX buttons
  document.querySelectorAll("[data-ajax]").forEach((button) => {
    button.addEventListener("click", function () {
      showButtonLoading(button);
    });
  });
}

/**
 * Initialize delete confirmation
 */
function initializeDeleteConfirmation() {
  // Enhanced delete confirmation with details
  document.querySelectorAll(".delete-form").forEach((form) => {
    form.addEventListener("submit", function (event) {
      const withdrawalCode = form.dataset.withdrawalCode || "này";
      const isCompleted = form.dataset.isCompleted === "true";

      let message = `Bạn có chắc chắn muốn xóa withdrawal ${withdrawalCode}?`;

      if (isCompleted) {
        message +=
          "\n\nCảnh báo: Withdrawal này đã hoàn thành. Việc xóa có thể ảnh hưởng đến báo cáo tài chính.";
      }

      message += "\n\nThao tác này không thể hoàn tác.";

      if (!confirm(message)) {
        event.preventDefault();
      }
    });
  });

  // Bulk delete confirmation
  document.querySelectorAll(".bulk-delete-btn").forEach((button) => {
    button.addEventListener("click", function (event) {
      const selectedCount = document.querySelectorAll(
        'input[name="withdrawalIds"]:checked'
      ).length;

      if (selectedCount === 0) {
        event.preventDefault();
        showToast("Vui lòng chọn ít nhất một withdrawal để xóa", "warning");
        return;
      }

      const message = `Bạn có chắc chắn muốn xóa ${selectedCount} withdrawal đã chọn?\n\nThao tác này không thể hoàn tác.`;

      if (!confirm(message)) {
        event.preventDefault();
      }
    });
  });
}

/**
 * Initialize index page functionality
 */
function initializeIndexPage() {
  // Initialize bulk actions
  initializeBulkActions();

  // Initialize search form
  initializeSearchForm();

  // Initialize filters
  initializeFilters();

  // Initialize statistics refresh
  initializeStatisticsRefresh();
}

/**
 * Initialize bulk actions
 */
function initializeBulkActions() {
  const selectAllCheckbox = document.getElementById("selectAll");
  const itemCheckboxes = document.querySelectorAll(
    'input[name="withdrawalIds"]'
  );
  const bulkActionSelect = document.getElementById("bulkAction");
  const bulkActionButton = document.getElementById("bulkActionButton");

  // Select all functionality
  if (selectAllCheckbox) {
    selectAllCheckbox.addEventListener("change", function () {
      itemCheckboxes.forEach((checkbox) => {
        checkbox.checked = selectAllCheckbox.checked;
      });
      updateBulkActionButton();
    });
  }

  // Individual checkbox change
  itemCheckboxes.forEach((checkbox) => {
    checkbox.addEventListener("change", function () {
      updateSelectAllState();
      updateBulkActionButton();
    });
  });

  // Update select all state based on individual checkboxes
  function updateSelectAllState() {
    if (selectAllCheckbox) {
      const checkedCount = document.querySelectorAll(
        'input[name="withdrawalIds"]:checked'
      ).length;
      const totalCount = itemCheckboxes.length;

      selectAllCheckbox.checked = checkedCount === totalCount;
      selectAllCheckbox.indeterminate =
        checkedCount > 0 && checkedCount < totalCount;
    }
  }

  // Update bulk action button state
  function updateBulkActionButton() {
    const checkedCount = document.querySelectorAll(
      'input[name="withdrawalIds"]:checked'
    ).length;

    if (bulkActionButton) {
      bulkActionButton.disabled = checkedCount === 0;
      bulkActionButton.textContent =
        checkedCount > 0 ? `Thực hiện (${checkedCount})` : "Thực hiện";
    }
  }

  // Bulk action confirmation
  if (bulkActionButton) {
    bulkActionButton.addEventListener("click", function (event) {
      const selectedCount = document.querySelectorAll(
        'input[name="withdrawalIds"]:checked'
      ).length;
      const action = bulkActionSelect ? bulkActionSelect.value : "";

      if (selectedCount === 0) {
        event.preventDefault();
        showToast("Vui lòng chọn ít nhất một withdrawal", "warning");
        return;
      }

      if (!action) {
        event.preventDefault();
        showToast("Vui lòng chọn hành động", "warning");
        return;
      }

      let actionText = "";
      switch (action) {
        case "delete":
          actionText = "xóa";
          break;
        case "status_completed":
          actionText = "đánh dấu hoàn thành";
          break;
        case "status_failed":
          actionText = "đánh dấu thất bại";
          break;
        case "status_cancelled":
          actionText = "đánh dấu hủy";
          break;
        case "status_pending":
          actionText = "đánh dấu chờ xử lý";
          break;
        default:
          actionText = "thực hiện hành động";
      }

      const message = `Bạn có chắc chắn muốn ${actionText} ${selectedCount} withdrawal đã chọn?`;

      if (!confirm(message)) {
        event.preventDefault();
      }
    });
  }
}

/**
 * Initialize search form
 */
function initializeSearchForm() {
  const searchForm = document.getElementById("searchForm");
  const searchInput = document.getElementById("keyword");

  if (searchForm && searchInput) {
    // Debounced search
    const debouncedSearch = debounce(() => {
      if (searchInput.value.length >= 3 || searchInput.value.length === 0) {
        searchForm.submit();
      }
    }, 500);

    searchInput.addEventListener("input", debouncedSearch);
  }
}

/**
 * Initialize filters
 */
function initializeFilters() {
  const filterForm = document.getElementById("filterForm");
  const statusFilter = document.getElementById("status");
  const startDateFilter = document.getElementById("startDate");
  const endDateFilter = document.getElementById("endDate");

  // Auto-submit on filter change
  [statusFilter, startDateFilter, endDateFilter].forEach((filter) => {
    if (filter) {
      filter.addEventListener("change", () => {
        if (filterForm) {
          filterForm.submit();
        }
      });
    }
  });

  // Date range validation
  if (startDateFilter && endDateFilter) {
    function validateDateRange() {
      const startDate = new Date(startDateFilter.value);
      const endDate = new Date(endDateFilter.value);

      if (startDate && endDate && startDate > endDate) {
        showToast("Ngày bắt đầu không thể sau ngày kết thúc", "warning");
        endDateFilter.value = startDateFilter.value;
      }
    }

    startDateFilter.addEventListener("change", validateDateRange);
    endDateFilter.addEventListener("change", validateDateRange);
  }
}

/**
 * Initialize statistics refresh
 */
function initializeStatisticsRefresh() {
  const refreshButton = document.getElementById("refreshStats");

  if (refreshButton) {
    refreshButton.addEventListener("click", function () {
      showButtonLoading(refreshButton, "Đang tải...");

      // Reload the page to refresh statistics
      setTimeout(() => {
        window.location.reload();
      }, 500);
    });
  }
}

// Export additional functions for global access
window.showLoadingOverlay = showLoadingOverlay;
window.hideLoadingOverlay = hideLoadingOverlay;
window.showButtonLoading = showButtonLoading;
window.hideButtonLoading = hideButtonLoading;
window.handleNetworkError = handleNetworkError;
window.retryRequest = retryRequest;
window.showServerFieldErrors = showServerFieldErrors;
window.initializeErrorHandling = initializeErrorHandling;
window.initializeLoadingStates = initializeLoadingStates;
window.initializeDeleteConfirmation = initializeDeleteConfirmation;
window.initializeIndexPage = initializeIndexPage;

/**
 * Hide button loading state
 */
function hideButtonLoading(button) {
  if (!button) return;

  button.disabled = false;
  button.innerHTML = button.dataset.originalText || button.innerHTML;
  button.classList.remove("loading");
  delete button.dataset.originalText;
}

/**
 * Enhanced network error handling
 */
function handleNetworkError(error, context = "") {
  console.error("Network error" + (context ? " " + context : ""), error);

  let message = "Đã xảy ra lỗi kết nối";
  let type = "error";

  if (error.name === "TypeError" && error.message.includes("fetch")) {
    message = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.";
  } else if (error.message.includes("timeout")) {
    message = "Kết nối bị timeout. Vui lòng thử lại.";
  } else if (error.message.includes("HTTP 500")) {
    message = "Lỗi máy chủ nội bộ. Vui lòng thử lại sau.";
  } else if (error.message.includes("HTTP 404")) {
    message = "Không tìm thấy tài nguyên yêu cầu.";
  } else if (error.message.includes("HTTP 403")) {
    message = "Bạn không có quyền thực hiện thao tác này.";
  } else if (error.message.includes("HTTP 401")) {
    message = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    type = "warning";
    // Redirect to login after showing message
    setTimeout(() => {
      window.location.href = "/login";
    }, 3000);
  }

  showToast(message, type, 5000);
}

/**
 * Retry mechanism for network requests
 */
function retryRequest(requestFn, maxRetries = 3, delay = 1000) {
  return new Promise((resolve, reject) => {
    let retries = 0;

    function attempt() {
      requestFn()
        .then(resolve)
        .catch((error) => {
          retries++;
          if (retries < maxRetries) {
            console.warn(
              `Request failed, retrying (${retries}/${maxRetries})...`
            );
            setTimeout(attempt, delay * retries); // Exponential backoff
          } else {
            reject(error);
          }
        });
    }

    attempt();
  });
}

/**
 * Initialize error handling for forms
 */
function initializeErrorHandling() {
  // Handle server-side validation errors
  const fieldErrors = window.fieldErrors;
  if (fieldErrors) {
    showServerFieldErrors(fieldErrors);
  }

  // Handle global error messages
  const errorMessage = document.querySelector(".alert-danger");
  if (errorMessage) {
    const message = errorMessage.textContent.trim();
    if (message) {
      showToast(message, "error", 5000);
    }
  }

  // Handle success messages
  const successMessage = document.querySelector(".alert-success");
  if (successMessage) {
    const message = successMessage.textContent.trim();
    if (message) {
      showToast(message, "success", 3000);
    }
  }

  // Handle warning messages
  const warningMessage = document.querySelector(".alert-warning");
  if (warningMessage) {
    const message = warningMessage.textContent.trim();
    if (message) {
      showToast(message, "warning", 4000);
    }
  }
}

/**
 * Initialize loading states
 */
function initializeLoadingStates() {
  // Add loading state to all form submissions
  document.querySelectorAll("form").forEach((form) => {
    form.addEventListener("submit", function (event) {
      // Only show loading if form validation passes
      if (!form.checkValidity || form.checkValidity()) {
        const submitButton = form.querySelector('button[type="submit"]');
        if (submitButton) {
          showButtonLoading(submitButton);
        }
      }
    });
  });

  // Add loading state to AJAX buttons
  document.querySelectorAll("[data-ajax]").forEach((button) => {
    button.addEventListener("click", function () {
      showButtonLoading(button);
    });
  });
}

// Export all functions for global access
window.showLoadingOverlay = showLoadingOverlay;
window.hideLoadingOverlay = hideLoadingOverlay;
window.showButtonLoading = showButtonLoading;
window.hideButtonLoading = hideButtonLoading;
window.handleNetworkError = handleNetworkError;
window.retryRequest = retryRequest;
window.showServerFieldErrors = showServerFieldErrors;
window.initializeErrorHandling = initializeErrorHandling;
window.initializeLoadingStates = initializeLoadingStates;
window.initializeDeleteConfirmation = initializeDeleteConfirmation;
window.initializeIndexPage = initializeIndexPage;
