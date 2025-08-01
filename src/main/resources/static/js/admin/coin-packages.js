/**
 * JavaScript cho trang quản lý gói xu
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // Kiểm tra mã gói xu có tồn tại không
    const codeInput = document.getElementById('code');
    if (codeInput) {
        codeInput.addEventListener('blur', function() {
            checkCodeExists(this.value);
        });
    }

    // Tạo mã tự động
    const nameInput = document.getElementById('name');
    const generateCodeBtn = document.getElementById('generateCode');
    if (nameInput && generateCodeBtn) {
        generateCodeBtn.addEventListener('click', function() {
            generateCode(nameInput.value);
        });
    }

    // Bulk action
    const bulkActionForm = document.getElementById('bulkActionForm');
    if (bulkActionForm) {
        bulkActionForm.addEventListener('submit', function(e) {
            const selectedPackages = document.querySelectorAll('input[name="packageIds"]:checked');
            if (selectedPackages.length === 0) {
                e.preventDefault();
                alert('Vui lòng chọn ít nhất một gói xu để thực hiện hành động.');
                return false;
            }
        });
    }

    // Xác nhận xóa
    const deleteButtons = document.querySelectorAll('.delete-package');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Bạn có chắc chắn muốn xóa gói xu này?')) {
                e.preventDefault();
                return false;
            }
        });
    });
});

/**
 * Kiểm tra mã gói xu có tồn tại không
 */
function checkCodeExists(code) {
    if (!code || code.trim() === '') {
        return;
    }

    const excludeId = document.getElementById('packageId')?.value;
    const url = excludeId ? 
        `/admin/coin-packages/check-code?code=${encodeURIComponent(code)}&excludeId=${excludeId}` :
        `/admin/coin-packages/check-code?code=${encodeURIComponent(code)}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            const codeInput = document.getElementById('code');
            const codeFeedback = document.getElementById('codeFeedback');
            
            if (data.exists) {
                codeInput.classList.add('is-invalid');
                codeInput.classList.remove('is-valid');
                if (codeFeedback) {
                    codeFeedback.textContent = 'Mã gói xu đã tồn tại';
                    codeFeedback.className = 'invalid-feedback';
                }
            } else {
                codeInput.classList.add('is-valid');
                codeInput.classList.remove('is-invalid');
                if (codeFeedback) {
                    codeFeedback.textContent = 'Mã gói xu có thể sử dụng';
                    codeFeedback.className = 'valid-feedback';
                }
            }
        })
        .catch(error => {
            console.error('Lỗi khi kiểm tra mã:', error);
        });
}

/**
 * Tạo mã gói xu tự động
 */
function generateCode(name) {
    if (!name || name.trim() === '') {
        alert('Vui lòng nhập tên gói xu trước khi tạo mã.');
        return;
    }

    fetch('/admin/coin-packages/generate-code', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `name=${encodeURIComponent(name)}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.code) {
            const codeInput = document.getElementById('code');
            if (codeInput) {
                codeInput.value = data.code;
                codeInput.classList.add('is-valid');
                codeInput.classList.remove('is-invalid');
                
                const codeFeedback = document.getElementById('codeFeedback');
                if (codeFeedback) {
                    codeFeedback.textContent = 'Mã đã được tạo tự động';
                    codeFeedback.className = 'valid-feedback';
                }
            }
        } else if (data.error) {
            alert('Lỗi khi tạo mã: ' + data.error);
        }
    })
    .catch(error => {
        console.error('Lỗi khi tạo mã:', error);
        alert('Lỗi khi tạo mã gói xu');
    });
}

/**
 * Chọn tất cả/bỏ chọn tất cả
 */
function toggleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const packageCheckboxes = document.querySelectorAll('input[name="packageIds"]');
    
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            packageCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            updateBulkActionButtons();
        });
    }

    // Cập nhật trạng thái "chọn tất cả" khi chọn từng item
    packageCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const checkedCount = document.querySelectorAll('input[name="packageIds"]:checked').length;
            const totalCount = packageCheckboxes.length;
            
            if (selectAllCheckbox) {
                selectAllCheckbox.checked = checkedCount === totalCount;
                selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount < totalCount;
            }
            updateBulkActionButtons();
        });
    });
}

/**
 * Cập nhật trạng thái các nút bulk action
 */
function updateBulkActionButtons() {
    const selectedCount = document.querySelectorAll('input[name="packageIds"]:checked').length;
    const bulkActionButtons = document.querySelectorAll('.bulk-action-btn');
    
    bulkActionButtons.forEach(button => {
        if (selectedCount === 0) {
            button.disabled = true;
        } else {
            button.disabled = false;
        }
    });
}

/**
 * Hiển thị thông báo
 */
function showNotification(message, type = 'success') {
    // Tạo toast notification
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;
    
    const toastContainer = document.getElementById('toastContainer');
    if (toastContainer) {
        toastContainer.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
    }
}

// Khởi tạo các chức năng
document.addEventListener('DOMContentLoaded', function() {
    toggleSelectAll();
    updateBulkActionButtons();
}); 