// Settings page JavaScript functionality

document.addEventListener('DOMContentLoaded', function() {
    // Tab switching functionality
    const tabButtons = document.querySelectorAll('.settings-nav-item');
    const tabContents = document.querySelectorAll('.settings-section');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-section');
            
            // Remove active class from all buttons and contents
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            // Add active class to clicked button and corresponding content
            this.classList.add('active');
            const targetContent = document.getElementById(targetTab);
            if (targetContent) {
                targetContent.classList.add('active');
            }
        });
    });
    
    // Form submission feedback
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                submitBtn.disabled = true;
                const originalText = submitBtn.innerHTML;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
                
                // Re-enable after 3 seconds to prevent permanent disable
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }, 3000);
            }
        });
    });
});

// Test email connection
function testEmailConnection() {
    const smtpServer = document.querySelector('input[name="smtpServer"]').value;
    const smtpPort = document.querySelector('input[name="smtpPort"]').value;
    const emailSender = document.querySelector('input[name="emailSender"]').value;
    const smtpPassword = document.querySelector('input[name="smtpPassword"]').value;
    
    if (!smtpServer || !smtpPort || !emailSender || !smtpPassword) {
        alert('Vui lòng điền đầy đủ thông tin email trước khi test!');
        return;
    }
    
    // Show loading
    const testBtn = event.target;
    const originalText = testBtn.innerHTML;
    testBtn.disabled = true;
    testBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang test...';
    
    // Create form and submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/admin/settings/email/test';
    form.style.display = 'none';
    
    const fields = {
        'smtpServer': smtpServer,
        'smtpPort': smtpPort,
        'emailSender': emailSender,
        'smtpPassword': smtpPassword
    };
    
    Object.keys(fields).forEach(key => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = key;
        input.value = fields[key];
        form.appendChild(input);
    });
    
    document.body.appendChild(form);
    form.submit();
}

// Auto-save settings (optional)
function autoSaveSettings() {
    const form = document.getElementById('settingsForm');
    if (form) {
        const formData = new FormData(form);
        
        fetch('/admin/settings/save', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (response.ok) {
                showToast('Cài đặt đã được lưu tự động', 'success');
            }
        })
        .catch(error => {
            console.error('Auto-save failed:', error);
        });
    }
}

// Show toast notification
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) return;
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    toastContainer.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 5000);
}
