/**
 * Transactions Management JavaScript
 * Xử lý tương tác cho trang quản lý giao dịch - MVC thuần
 */

// Initialize when document is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeTransactions();
});

/**
 * Initialize transactions page
 */
function initializeTransactions() {
    // Initialize date pickers
    initializeDatePickers();
    
    // Initialize bulk actions
    initializeBulkActions();
    
    // Initialize filters
    initializeFilters();
    
    // Initialize pagination
    initializePagination();
    
    // Initialize action buttons
    initializeActionButtons();
    
    // Initialize search functionality
    initializeSearch();
}

/**
 * Initialize date pickers
 */
function initializeDatePickers() {
    const dateInputs = document.querySelectorAll('input[type="text"][id*="Date"]');
    dateInputs.forEach(input => {
        flatpickr(input, {
            dateFormat: "Y-m-d",
            locale: "vi",
            allowInput: true
        });
    });
}

/**
 * Initialize bulk actions
 */
function initializeBulkActions() {
    const selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('input[name="transactionIds"]');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            updateBulkActionButton();
        });
    }
    
    // Individual checkboxes
    document.querySelectorAll('input[name="transactionIds"]').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateSelectAllCheckbox();
            updateBulkActionButton();
        });
    });
}

/**
 * Update select all checkbox state
 */
function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('input[name="transactionIds"]');
    const checkedBoxes = document.querySelectorAll('input[name="transactionIds"]:checked');
    
    if (selectAllCheckbox) {
        selectAllCheckbox.checked = checkboxes.length === checkedBoxes.length;
        selectAllCheckbox.indeterminate = checkedBoxes.length > 0 && checkedBoxes.length < checkboxes.length;
    }
}

/**
 * Update bulk action button state
 */
function updateBulkActionButton() {
    const bulkActionBtn = document.querySelector('.bulk-action-btn');
    const checkedBoxes = document.querySelectorAll('input[name="transactionIds"]:checked');
    
    if (bulkActionBtn) {
        bulkActionBtn.disabled = checkedBoxes.length === 0;
    }
}

/**
 * Initialize filters
 */
function initializeFilters() {
    const filterForm = document.querySelector('.management-tools form');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            applyFilters();
        });
    }
    
    // Clear filters button
    const clearFiltersBtn = document.querySelector('a[href*="/admin/transactions"]');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', function(e) {
            e.preventDefault();
            clearFilters();
        });
    }
}

/**
 * Apply filters
 */
function applyFilters() {
    const form = document.querySelector('.management-tools form');
    const formData = new FormData(form);
    
    // Build query string
    const params = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        if (value && value !== 'all') {
            params.append(key, value);
        }
    }
    
    // Redirect with filters
    window.location.href = '/admin/transactions?' + params.toString();
}

/**
 * Clear filters
 */
function clearFilters() {
    window.location.href = '/admin/transactions';
}

/**
 * Initialize pagination
 */
function initializePagination() {
    const paginationLinks = document.querySelectorAll('.pagination a');
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            if (href) {
                window.location.href = href;
            }
        });
    });
}

/**
 * Initialize action buttons
 */
function initializeActionButtons() {
    // View detail buttons
    document.querySelectorAll('.action-btn[title="Xem chi tiết"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            if (href) {
                window.location.href = href;
            }
        });
    });
    
    // Edit buttons
    document.querySelectorAll('.action-btn[title="Chỉnh sửa"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            if (href) {
                window.location.href = href;
            }
        });
    });
    
    // Delete buttons
    document.querySelectorAll('.action-btn[title="Xóa"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            if (href && confirm('Bạn có chắc chắn muốn xóa giao dịch này?')) {
                window.location.href = href;
            }
        });
    });
}

/**
 * Initialize search functionality
 */
function initializeSearch() {
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                performSearch(this.value);
            }, 500);
        });
    }
}

/**
 * Perform search
 */
function performSearch(keyword) {
    const currentUrl = new URL(window.location);
    if (keyword) {
        currentUrl.searchParams.set('keyword', keyword);
    } else {
        currentUrl.searchParams.delete('keyword');
    }
    currentUrl.searchParams.set('page', '0'); // Reset to first page
    window.location.href = currentUrl.toString();
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas fa-${getToastIcon(type)}"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Add to page
    const toastContainer = document.querySelector('.toast-container') || createToastContainer();
    toastContainer.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 5000);
}

/**
 * Get toast icon based on type
 */
function getToastIcon(type) {
    switch (type) {
        case 'success': return 'check-circle';
        case 'error': return 'exclamation-circle';
        case 'warning': return 'exclamation-triangle';
        default: return 'info-circle';
    }
}

/**
 * Create toast container if not exists
 */
function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
}

/**
 * Export transactions to CSV
 */
function exportTransactions() {
    const currentUrl = new URL(window.location);
    const params = currentUrl.searchParams;
    
    // Build export URL
    const exportUrl = `/admin/transactions/export?${params.toString()}`;
    
    // Create temporary link and click
    const link = document.createElement('a');
    link.href = exportUrl;
    link.download = `transactions_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Refresh transaction data
 */
function refreshTransactions() {
    window.location.reload();
}

/**
 * Handle form validation
 */
function validateTransactionForm(form) {
    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            showFieldError(field, 'Trường này là bắt buộc');
            isValid = false;
        } else {
            clearFieldError(field);
        }
    });
    
    // Validate amount
    const amountField = form.querySelector('input[name="amount"]');
    if (amountField && amountField.value) {
        const amount = parseFloat(amountField.value);
        if (amount < 0) {
            showFieldError(amountField, 'Số tiền phải lớn hơn 0');
            isValid = false;
        }
    }
    
    return isValid;
}

/**
 * Show field error
 */
function showFieldError(field, message) {
    clearFieldError(field);
    
    const errorSpan = document.createElement('span');
    errorSpan.className = 'error-message';
    errorSpan.textContent = message;
    
    field.parentNode.appendChild(errorSpan);
    field.classList.add('error');
}

/**
 * Clear field error
 */
function clearFieldError(field) {
    const existingError = field.parentNode.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }
    field.classList.remove('error');
}

/**
 * Reset form to original values
 */
function resetForm(form) {
    form.reset();
    
    // Clear all errors
    form.querySelectorAll('.error-message').forEach(error => error.remove());
    form.querySelectorAll('.error').forEach(field => field.classList.remove('error'));
    
    // Reset date pickers
    form.querySelectorAll('input[type="text"][id*="Date"]').forEach(input => {
        if (window.flatpickr && window.flatpickr(input)) {
            window.flatpickr(input).clear();
        }
    });
}

/**
 * Handle bulk action submission
 */
function handleBulkAction() {
    const actionSelect = document.querySelector('select[name="action"]');
    const checkedBoxes = document.querySelectorAll('input[name="transactionIds"]:checked');
    
    if (!actionSelect.value) {
        showToast('Vui lòng chọn hành động', 'warning');
        return false;
    }
    
    if (checkedBoxes.length === 0) {
        showToast('Vui lòng chọn ít nhất một giao dịch', 'warning');
        return false;
    }
    
    return confirm(`Bạn có chắc chắn muốn thực hiện hành động "${actionSelect.options[actionSelect.selectedIndex].text}" cho ${checkedBoxes.length} giao dịch?`);
}

// Export functions for global access
window.transactions = {
    showToast,
    exportTransactions,
    refreshTransactions,
    validateTransactionForm,
    resetForm,
    handleBulkAction
};
