// Notification utilities
class NotificationManager {
    constructor() {
        this.init();
    }

    init() {
        // Auto-hide alerts after 5 seconds
        this.autoHideAlerts();
        
        // Setup dismissible alerts
        this.setupDismissibleAlerts();
    }

    autoHideAlerts() {
        const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
        alerts.forEach(alert => {
            setTimeout(() => {
                this.hideAlert(alert);
            }, 5000);
        });
    }

    setupDismissibleAlerts() {
        const closeButtons = document.querySelectorAll('.alert .btn-close');
        closeButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                const alert = e.target.closest('.alert');
                this.hideAlert(alert);
            });
        });
    }

    hideAlert(alert) {
        if (!alert) return;
        
        alert.classList.add('fade-out');
        setTimeout(() => {
            alert.remove();
        }, 500);
    }

    showAlert(message, type = 'info', duration = 5000) {
        const alertContainer = document.querySelector('.admin-content');
        if (!alertContainer) return;

        const iconMap = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-triangle',
            warning: 'fas fa-exclamation-circle',
            info: 'fas fa-info-circle'
        };

        const alert = document.createElement('div');
        alert.className = `alert alert-${type} alert-dismissible`;
        alert.innerHTML = `
            <i class="${iconMap[type] || iconMap.info}"></i>
            <span>${message}</span>
            <button type="button" class="btn-close" aria-label="Close">
                <i class="fas fa-times"></i>
            </button>
        `;

        // Insert at the beginning of admin-content
        const firstChild = alertContainer.firstElementChild;
        alertContainer.insertBefore(alert, firstChild);

        // Setup close button
        const closeBtn = alert.querySelector('.btn-close');
        closeBtn.addEventListener('click', () => {
            this.hideAlert(alert);
        });

        // Auto-hide if duration is specified
        if (duration > 0) {
            setTimeout(() => {
                this.hideAlert(alert);
            }, duration);
        }

        return alert;
    }

    showToast(message, type = 'info', duration = 5000) {
        let toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container';
            document.body.appendChild(toastContainer);
        }

        const titleMap = {
            success: 'Thành công',
            error: 'Lỗi',
            warning: 'Cảnh báo',
            info: 'Thông tin'
        };

        const iconMap = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-triangle',
            warning: 'fas fa-exclamation-circle',
            info: 'fas fa-info-circle'
        };

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-header">
                <div style="display: flex; align-items: center; gap: 8px;">
                    <i class="${iconMap[type] || iconMap.info}"></i>
                    <span>${titleMap[type] || titleMap.info}</span>
                </div>
                <button type="button" class="toast-close" aria-label="Close">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="toast-body">${message}</div>
            ${duration > 0 ? `
                <div class="toast-progress">
                    <div class="toast-progress-bar" style="animation-duration: ${duration}ms;"></div>
                </div>
            ` : ''}
        `;

        toastContainer.appendChild(toast);

        // Setup close button
        const closeBtn = toast.querySelector('.toast-close');
        closeBtn.addEventListener('click', () => {
            this.hideToast(toast);
        });

        // Auto-hide if duration is specified
        if (duration > 0) {
            setTimeout(() => {
                this.hideToast(toast);
            }, duration);
        }

        return toast;
    }

    hideToast(toast) {
        if (!toast) return;
        
        toast.style.animation = 'slideInRight 0.3s ease-out reverse';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }

    // Static methods for easy access
    static success(message, duration = 5000) {
        return window.notificationManager.showAlert(message, 'success', duration);
    }

    static error(message, duration = 5000) {
        return window.notificationManager.showAlert(message, 'error', duration);
    }

    static warning(message, duration = 5000) {
        return window.notificationManager.showAlert(message, 'warning', duration);
    }

    static info(message, duration = 5000) {
        return window.notificationManager.showAlert(message, 'info', duration);
    }

    static toast(message, type = 'info', duration = 5000) {
        return window.notificationManager.showToast(message, type, duration);
    }
}

// Initialize notification manager when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.notificationManager = new NotificationManager();
});

// Export for use in other scripts
window.NotificationManager = NotificationManager;