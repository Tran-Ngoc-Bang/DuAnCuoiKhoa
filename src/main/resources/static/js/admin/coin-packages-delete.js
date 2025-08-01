/**
 * Coin Packages Delete Page JavaScript
 * Xóa gói xu với MVC thuần
 */

document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const deletePackageForm = document.getElementById('deletePackageForm');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const reasonInput = document.getElementById('reason');
    const packageId = document.getElementById('packageId')?.value;
    const packageName = document.getElementById('packageName')?.textContent;

    /**
     * Khởi tạo trang
     */
    function initializePage() {
        console.log('Initializing coin package delete page...');
        try {
            setupFormValidation();
            setupDeleteConfirmation();
            setupPackageStats();
            console.log('Coin package delete page initialized successfully');
        } catch (error) {
            console.error('Error initializing coin package delete page:', error);
        }
    }

    /**
     * Thiết lập form validation
     */
    function setupFormValidation() {
        if (deletePackageForm) {
            deletePackageForm.addEventListener('submit', function(e) {
                if (!validateForm()) {
                    e.preventDefault();
                    return false;
                }
            });
        }
    }

    /**
     * Validate form
     */
    function validateForm() {
        let isValid = true;

        // Validate reason (optional but recommended)
        if (reasonInput && reasonInput.value.trim().length < 5) {
            showFieldError(reasonInput, 'Lý do xóa nên có ít nhất 5 ký tự');
            isValid = false;
        } else if (reasonInput) {
            clearFieldError(reasonInput);
        }

        return isValid;
    }

    /**
     * Thiết lập xác nhận xóa
     */
    function setupDeleteConfirmation() {
        if (confirmDeleteBtn) {
            confirmDeleteBtn.addEventListener('click', function(e) {
                e.preventDefault();
                showDeleteConfirmation();
            });
        }

        if (cancelBtn) {
            cancelBtn.addEventListener('click', function(e) {
                e.preventDefault();
                cancelDelete();
            });
        }
    }

    /**
     * Hiển thị xác nhận xóa
     */
    function showDeleteConfirmation() {
        const message = `Bạn có chắc chắn muốn xóa gói xu "${packageName}" không?\n\nHành động này không thể hoàn tác.`;
        
        if (confirm(message)) {
            submitDeleteForm();
        }
    }

    /**
     * Hủy bỏ xóa
     */
    function cancelDelete() {
        if (confirm('Bạn có chắc chắn muốn hủy bỏ việc xóa gói xu?')) {
            window.location.href = '/admin/coin-packages';
        }
    }

    /**
     * Submit form xóa
     */
    function submitDeleteForm() {
        if (deletePackageForm) {
            // Thêm loading state
            confirmDeleteBtn.disabled = true;
            confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xóa...';
            
            // Submit form
            deletePackageForm.submit();
        }
    }

    /**
     * Thiết lập hiển thị thống kê gói xu
     */
    function setupPackageStats() {
        // Thống kê đã được render từ server, chỉ cần format hiển thị
        const statsElements = document.querySelectorAll('.stat-value');
        statsElements.forEach(element => {
            const value = element.textContent;
            if (value && !isNaN(value)) {
                if (element.classList.contains('currency')) {
                    element.textContent = formatCurrency(parseFloat(value));
                } else if (element.classList.contains('percentage')) {
                    element.textContent = parseFloat(value).toFixed(2) + '%';
                } else {
                    element.textContent = formatNumber(parseFloat(value));
                }
            }
        });
    }

    /**
     * Format tiền tệ
     */
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    /**
     * Format số
     */
    function formatNumber(number) {
        return new Intl.NumberFormat('vi-VN').format(number);
    }

    /**
     * Hiển thị lỗi cho field
     */
    function showFieldError(field, message) {
        clearFieldError(field);
        
        const errorElement = document.createElement('span');
        errorElement.className = 'error-message';
        errorElement.textContent = message;
        
        field.parentNode.appendChild(errorElement);
        field.classList.add('error');
    }

    /**
     * Xóa lỗi cho field
     */
    function clearFieldError(field) {
        const errorElement = field.parentNode.querySelector('.error-message');
        if (errorElement) {
            errorElement.remove();
        }
        field.classList.remove('error');
    }

    /**
     * Hiển thị toast message
     */
    function showToast(message, type = 'info') {
        // Tạo toast element
        const toast = document.createElement('div');
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
        const toastContainer = document.getElementById('toastContainer') || document.body;
        toastContainer.appendChild(toast);

        // Auto remove sau 5 giây
        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 5000);

        // Close button
        toast.querySelector('.toast-close').addEventListener('click', () => {
            toast.remove();
        });
    }

    /**
     * Lấy icon cho toast
     */
    function getToastIcon(type) {
        switch (type) {
            case 'success': return 'check-circle';
            case 'error': return 'exclamation-circle';
            case 'warning': return 'exclamation-triangle';
            default: return 'info-circle';
        }
    }

    // Khởi tạo trang
    initializePage();
}); 