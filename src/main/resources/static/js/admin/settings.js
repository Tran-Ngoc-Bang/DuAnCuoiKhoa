// Settings Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Tab switching functionality
    const navItems = document.querySelectorAll('.settings-nav-item');
    const sections = document.querySelectorAll('.settings-section');

    navItems.forEach(item => {
        item.addEventListener('click', function() {
            const targetSection = this.getAttribute('data-section');
            
            // Remove active class from all nav items and sections
            navItems.forEach(nav => nav.classList.remove('active'));
            sections.forEach(section => section.classList.remove('active'));
            
            // Add active class to clicked nav item and corresponding section
            this.classList.add('active');
            document.getElementById(targetSection).classList.add('active');
        });
    });

    // Toggle switches functionality
    const toggleInputs = document.querySelectorAll('.toggle-input');
    toggleInputs.forEach(input => {
        input.addEventListener('change', function() {
            // Auto-save toggle settings
            const key = this.name;
            const value = this.checked;
            
            if (key) {
                updateSetting(key, value);
            }
        });
    });

    // Form submission with AJAX
    const forms = document.querySelectorAll('form[action*="/admin/settings/"]');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const actionUrl = this.getAttribute('action');
            
            // Show loading state
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';
            submitBtn.disabled = true;
            
            fetch(actionUrl, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    showToast('Cài đặt đã được lưu thành công!', 'success');
                } else {
                    showToast('Có lỗi xảy ra khi lưu cài đặt!', 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Có lỗi xảy ra khi lưu cài đặt!', 'error');
            })
            .finally(() => {
                // Restore button state
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            });
        });
    });

    // Auto-save for individual inputs
    const autoSaveInputs = document.querySelectorAll('.form-input[data-auto-save]');
    autoSaveInputs.forEach(input => {
        let timeout;
        input.addEventListener('input', function() {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                const key = this.name;
                const value = this.value;
                
                if (key) {
                    updateSetting(key, value);
                }
            }, 1000); // Auto-save after 1 second of no typing
        });
    });
});

// Function to update individual setting via AJAX
function updateSetting(key, value) {
    const formData = new FormData();
    formData.append('key', key);
    formData.append('value', value);
    
    fetch('/admin/settings/api/update', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('Setting updated:', key, '=', value);
        } else {
            console.error('Error updating setting:', data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}

// Function to reset settings
function resetSettings(category = null) {
    if (!confirm('Bạn có chắc chắn muốn reset cài đặt về mặc định? Hành động này không thể hoàn tác.')) {
        return;
    }
    
    const formData = new FormData();
    if (category) {
        formData.append('category', category);
    }
    
    fetch('/admin/settings/api/reset', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('Cài đặt đã được reset thành công!', 'success');
            setTimeout(() => {
                location.reload();
            }, 1500);
        } else {
            showToast('Có lỗi xảy ra: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi reset cài đặt!', 'error');
    });
}

// Function to export settings
function exportSettings() {
    fetch('/admin/settings/api/export')
    .then(response => response.json())
    .then(data => {
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'edushare-settings-' + new Date().toISOString().split('T')[0] + '.json';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        showToast('Cài đặt đã được export thành công!', 'success');
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi export cài đặt!', 'error');
    });
}

// Function to import settings
function importSettings() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    
    input.onchange = function(e) {
        const file = e.target.files[0];
        if (!file) return;
        
        const reader = new FileReader();
        reader.onload = function(e) {
            try {
                const settings = JSON.parse(e.target.result);
                
                fetch('/admin/settings/api/import', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(settings)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showToast(`Import thành công ${data.imported} cài đặt!`, 'success');
                        setTimeout(() => {
                            location.reload();
                        }, 1500);
                    } else {
                        showToast('Có lỗi xảy ra: ' + data.message, 'error');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast('Có lỗi xảy ra khi import cài đặt!', 'error');
                });
            } catch (error) {
                showToast('File JSON không hợp lệ!', 'error');
            }
        };
        reader.readAsText(file);
    };
    
    input.click();
}

// Function to save all settings
function saveAllSettings() {
    const forms = document.querySelectorAll('form[action*="/admin/settings/"]');
    let completedForms = 0;
    let totalForms = forms.length;
    
    if (totalForms === 0) {
        showToast('Không có cài đặt nào để lưu!', 'warning');
        return;
    }
    
    showToast('Đang lưu tất cả cài đặt...', 'info');
    
    forms.forEach(form => {
        const formData = new FormData(form);
        const actionUrl = form.getAttribute('action');
        
        fetch(actionUrl, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            completedForms++;
            if (completedForms === totalForms) {
                showToast('Đã lưu tất cả cài đặt thành công!', 'success');
            }
        })
        .catch(error => {
            console.error('Error saving form:', error);
            showToast('Có lỗi xảy ra khi lưu một số cài đặt!', 'error');
        });
    });
}

// Toast notification function
function showToast(message, type = 'info') {
    // Get or create toast container
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    // Create toast element
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
    
    // Add to container
    container.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 5000);
}