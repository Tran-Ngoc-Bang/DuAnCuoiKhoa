/**
 * Coin Packages Index Page JavaScript
 * Quản lý danh sách gói xu với MVC thuần
 */

document.addEventListener("DOMContentLoaded", function () {
  // Khởi tạo các biến
  let selectedPackages = new Set();

  // Elements
  const searchInput = document.getElementById("searchInput");
  const statusFilter = document.getElementById("statusFilter");
  const packagesTableBody = document.getElementById("packagesTableBody");
  const selectAllCheckbox = document.getElementById("selectAll");
  const bulkActionSelect = document.getElementById("bulkActionSelect");
  const applyBulkActionBtn = document.getElementById("applyBulkAction");
  const deletePackageModal = document.getElementById("deletePackageModal");
  const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
  const bulkActionForm = document.getElementById("bulkActionForm");

  /**
   * Khởi tạo trang
   */
  function initializePage() {
    console.log("Initializing coin packages page...");
    try {
      setupEventListeners();
      setupSearchAndFilter();
      setupBulkActions();
      setupDeleteModal();
      console.log("Coin packages page initialized successfully");
    } catch (error) {
      console.error("Error initializing coin packages page:", error);
    }
  }

  /**
   * Thiết lập event listeners
   */
  function setupEventListeners() {
    // Select all checkbox
    if (selectAllCheckbox) {
      selectAllCheckbox.addEventListener("change", handleSelectAll);
    }

    // Individual row checkboxes
    if (packagesTableBody) {
      packagesTableBody.addEventListener("change", function (e) {
        if (e.target.classList.contains("row-checkbox")) {
          handleRowSelection(e.target);
        }
      });

      // Delete package buttons - removed modal handling for MVC pattern
    }
  }

  /**
   * Thiết lập tìm kiếm và lọc
   */
  function setupSearchAndFilter() {
    // Search input - sử dụng form submission
    if (searchInput) {
      let searchTimeout;
      searchInput.addEventListener("input", function () {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
          performSearch();
        }, 500);
      });
    }

    // Status filter - sử dụng form submission
    if (statusFilter) {
      statusFilter.addEventListener("change", function () {
        performFilter();
      });
    }
  }

  /**
   * Thực hiện tìm kiếm
   */
  function performSearch() {
    const keyword = searchInput.value.trim();
    const currentUrl = new URL(window.location);

    if (keyword) {
      currentUrl.searchParams.set("keyword", keyword);
    } else {
      currentUrl.searchParams.delete("keyword");
    }

    // Reset page về 0 khi tìm kiếm
    currentUrl.searchParams.set("page", "0");

    window.location.href = currentUrl.toString();
  }

  /**
   * Thực hiện lọc
   */
  function performFilter() {
    const status = statusFilter.value;
    const currentUrl = new URL(window.location);

    if (status && status !== "all") {
      currentUrl.searchParams.set("status", status);
    } else {
      currentUrl.searchParams.delete("status");
    }

    // Reset page về 0 khi lọc
    currentUrl.searchParams.set("page", "0");

    window.location.href = currentUrl.toString();
  }

  /**
   * Thiết lập bulk actions
   */
  function setupBulkActions() {
    if (applyBulkActionBtn) {
      applyBulkActionBtn.addEventListener("click", function () {
        performBulkAction();
      });
    }
  }

  /**
   * Xử lý select all checkbox
   */
  function handleSelectAll() {
    const checkboxes = document.querySelectorAll(".row-checkbox");
    checkboxes.forEach((checkbox) => {
      checkbox.checked = selectAllCheckbox.checked;
      if (selectAllCheckbox.checked) {
        selectedPackages.add(checkbox.value);
      } else {
        selectedPackages.delete(checkbox.value);
      }
    });
    updateBulkActionButton();
  }

  /**
   * Xử lý selection của từng row
   */
  function handleRowSelection(checkbox) {
    if (checkbox.checked) {
      selectedPackages.add(checkbox.value);
    } else {
      selectedPackages.delete(checkbox.value);
    }
    updateSelectAllCheckbox();
    updateBulkActionButton();
  }

  /**
   * Cập nhật trạng thái select all checkbox
   */
  function updateSelectAllCheckbox() {
    const checkboxes = document.querySelectorAll(".row-checkbox");
    const checkedCheckboxes = document.querySelectorAll(
      ".row-checkbox:checked"
    );

    if (checkboxes.length === 0) {
      selectAllCheckbox.checked = false;
      selectAllCheckbox.indeterminate = false;
    } else if (checkedCheckboxes.length === 0) {
      selectAllCheckbox.checked = false;
      selectAllCheckbox.indeterminate = false;
    } else if (checkedCheckboxes.length === checkboxes.length) {
      selectAllCheckbox.checked = true;
      selectAllCheckbox.indeterminate = false;
    } else {
      selectAllCheckbox.checked = false;
      selectAllCheckbox.indeterminate = true;
    }
  }

  /**
   * Cập nhật trạng thái bulk action button
   */
  function updateBulkActionButton() {
    if (applyBulkActionBtn) {
      applyBulkActionBtn.disabled =
        selectedPackages.size === 0 || !bulkActionSelect.value;
    }
  }

  /**
   * Thực hiện bulk action
   */
  function performBulkAction() {
    const action = bulkActionSelect.value;
    if (!action || selectedPackages.size === 0) {
      showToast("Vui lòng chọn hành động và ít nhất một gói xu", "warning");
      return;
    }

    // Tạo form để submit
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "/admin/coin-packages/bulk-action";

    // Thêm package IDs
    selectedPackages.forEach((packageId) => {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = "packageIds";
      input.value = packageId;
      form.appendChild(input);
    });

    // Thêm action
    const actionInput = document.createElement("input");
    actionInput.type = "hidden";
    actionInput.name = "action";
    actionInput.value = action;
    form.appendChild(actionInput);

    // Submit form
    document.body.appendChild(form);
    form.submit();
  }

  /**
   * Thiết lập delete modal
   */
  function setupDeleteModal() {
    if (deletePackageModal) {
      // Close modal khi click overlay
      deletePackageModal
        .querySelector(".modal-overlay")
        .addEventListener("click", hideDeleteModal);

      // Close modal khi click close button
      deletePackageModal
        .querySelector(".modal-close")
        .addEventListener("click", hideDeleteModal);

      // Cancel button
      deletePackageModal
        .querySelector(".cancel-btn")
        .addEventListener("click", hideDeleteModal);

      // Confirm delete button
      if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", confirmDeletePackage);
      }
    }
  }

  /**
   * Hiển thị delete modal
   */
  function showDeleteModal(packageId, packageName) {
    if (deletePackageModal) {
      document.getElementById("deletePackageName").textContent = packageName;
      document.getElementById("deletePackageId").textContent = packageId;
      deletePackageModal.classList.add("active");
    }
  }

  /**
   * Ẩn delete modal
   */
  function hideDeleteModal() {
    if (deletePackageModal) {
      deletePackageModal.classList.remove("active");
    }
  }

  /**
   * Xác nhận xóa gói xu
   */
  function confirmDeletePackage() {
    const packageId = document.getElementById("deletePackageId").textContent;
    const packageName =
      document.getElementById("deletePackageName").textContent;

    // Tạo form để submit
    const form = document.createElement("form");
    form.method = "POST";
    form.action = `/admin/coin-packages/${packageId}/delete`;

    // Thêm reason (có thể để trống)
    const reasonInput = document.createElement("input");
    reasonInput.type = "hidden";
    reasonInput.name = "reason";
    reasonInput.value = "Xóa bởi admin";
    form.appendChild(reasonInput);

    // Submit form
    document.body.appendChild(form);
    form.submit();
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
