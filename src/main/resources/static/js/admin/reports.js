// Reports Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Các chức năng khác của trang reports sẽ được thêm vào đây

    // Bulk delete functionality
    const bulkDeleteBtn = document.getElementById('bulkDeleteBtn');
    if (bulkDeleteBtn) {
        bulkDeleteBtn.addEventListener('click', function() {
            const selectedReports = getSelectedReports();
            if (selectedReports.length === 0) {
                showToast('Vui lòng chọn ít nhất một báo cáo để xóa!', 'warning');
                return;
            }
            showBulkDeleteConfirmModal(selectedReports);
        });
    }

    // Select all checkbox
    const selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.report-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            updateBulkActionButtons();
        });
    }

    // Individual checkboxes
    const reportCheckboxes = document.querySelectorAll('.report-checkbox');
    reportCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateBulkActionButtons();
            updateSelectAllCheckbox();
        });
    });
});



// Show bulk delete confirmation modal
function showBulkDeleteConfirmModal(reportIds) {
    const modal = createModal({
        title: 'Xác nhận xóa nhiều báo cáo',
        content: `
            <div class="delete-confirm-content">
                <div class="warning-icon">
                    <i class="fas fa-exclamation-triangle"></i>
                </div>
                <p>Bạn có chắc chắn muốn xóa <strong>${reportIds.length}</strong> báo cáo đã chọn không?</p>
                <p class="warning-text">Hành động này không thể hoàn tác!</p>
                <div class="form-group">
                    <label for="bulkDeleteReason">Lý do xóa (tùy chọn):</label>
                    <textarea id="bulkDeleteReason" class="form-input" rows="3" placeholder="Nhập lý do xóa các báo cáo..."></textarea>
                </div>
            </div>
        `,
        buttons: [
            {
                text: 'Hủy',
                class: 'btn-secondary',
                onclick: 'closeModal()'
            },
            {
                text: `Xóa ${reportIds.length} báo cáo`,
                class: 'btn-danger',
                onclick: `confirmBulkDeleteReports([${reportIds.join(',')}])`
            }
        ]
    });
    
    document.body.appendChild(modal);
    modal.classList.add('show');
}






// Confirm bulk deletion
function confirmBulkDeleteReports(reportIds) {
    const deleteReason = document.getElementById('bulkDeleteReason').value;
    const button = document.querySelector(`[onclick="confirmBulkDeleteReports([${reportIds.join(',')}])"]`);
    
    // Show loading state
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xóa...';
    button.disabled = true;
    
    let completedRequests = 0;
    let successCount = 0;
    let errorCount = 0;
    
    reportIds.forEach(reportId => {
        const formData = new FormData();
        if (deleteReason.trim()) {
            formData.append('deleteReason', deleteReason.trim());
        }
        
        fetch(`/admin/reports/api/${reportId}`, {
            method: 'DELETE',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                successCount++;
                removeReportRow(reportId);
            } else {
                errorCount++;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            errorCount++;
        })
        .finally(() => {
            completedRequests++;
            if (completedRequests === reportIds.length) {
                // All requests completed
                closeModal();
                if (successCount > 0) {
                    showToast(`Đã xóa thành công ${successCount} báo cáo!`, 'success');
                }
                if (errorCount > 0) {
                    showToast(`Có ${errorCount} báo cáo không thể xóa!`, 'error');
                }
                // Reset checkboxes
                resetCheckboxes();
            }
        });
    });
}

// Remove report row from table
function removeReportRow(reportId) {
    const row = document.querySelector(`tr:has(.report-checkbox[data-id="${reportId}"])`);
    if (row) {
        row.style.transition = 'opacity 0.3s ease';
        row.style.opacity = '0';
        setTimeout(() => {
            row.remove();
            updateSelectAllCheckbox();
            updateBulkActionButtons();
        }, 300);
    }
}

// Get selected report IDs
function getSelectedReports() {
    const selectedCheckboxes = document.querySelectorAll('.report-checkbox:checked');
    return Array.from(selectedCheckboxes).map(checkbox => checkbox.getAttribute('data-id'));
}

// Update bulk action buttons visibility
function updateBulkActionButtons() {
    const selectedReports = getSelectedReports();
    const bulkActions = document.querySelector('.bulk-actions');
    
    if (bulkActions) {
        if (selectedReports.length > 0) {
            bulkActions.style.display = 'flex';
            const countSpan = bulkActions.querySelector('.selected-count');
            if (countSpan) {
                countSpan.textContent = selectedReports.length;
            }
        } else {
            bulkActions.style.display = 'none';
        }
    }
}

// Update select all checkbox state
function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const reportCheckboxes = document.querySelectorAll('.report-checkbox');
    const checkedCheckboxes = document.querySelectorAll('.report-checkbox:checked');
    
    if (selectAllCheckbox) {
        if (checkedCheckboxes.length === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else if (checkedCheckboxes.length === reportCheckboxes.length) {
            selectAllCheckbox.checked = true;
            selectAllCheckbox.indeterminate = false;
        } else {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = true;
        }
    }
}

// Reset all checkboxes
function resetCheckboxes() {
    const checkboxes = document.querySelectorAll('.report-checkbox, #selectAll');
    checkboxes.forEach(checkbox => {
        checkbox.checked = false;
        checkbox.indeterminate = false;
    });
    updateBulkActionButtons();
}

// Create modal
function createModal({ title, content, buttons }) {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-overlay" onclick="closeModal()"></div>
        <div class="modal-container">
            <div class="modal-header">
                <h3>${title}</h3>
                <button class="modal-close" onclick="closeModal()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body">
                ${content}
            </div>
            <div class="modal-footer">
                ${buttons.map(btn => `
                    <button class="btn ${btn.class}" onclick="${btn.onclick}">
                        ${btn.text}
                    </button>
                `).join('')}
            </div>
        </div>
    `;
    return modal;
}

// Close modal
function closeModal() {
    const modal = document.querySelector('.modal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.remove();
        }, 300);
    }
}

// Toast notification
function showToast(message, type = 'info') {
    // Remove existing toasts
    const existingToasts = document.querySelectorAll('.toast');
    existingToasts.forEach(toast => toast.remove());
    
    // Create toast
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : type === 'warning' ? 'fa-exclamation-triangle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Add to page
    document.body.appendChild(toast);
    
    // Auto remove
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 5000);
}