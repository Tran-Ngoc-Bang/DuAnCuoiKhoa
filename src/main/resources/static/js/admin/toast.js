/**
 * Toast Notification System
 * Tạo thông báo toast đẹp mắt như trong hình
 */
class ToastManager {
    constructor() {
        this.container = null;
        this.toasts = new Map();
        this.init();
    }

    init() {
        // Tạo container cho toast nếu chưa có
        if (!document.querySelector('.toast-container')) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        } else {
            this.container = document.querySelector('.toast-container');
        }
    }

    /**
     * Hiển thị toast thành công
     */
    success(message, options = {}) {
        return this.show({
            type: 'success',
            message: message,
            icon: 'fas fa-check',
            ...options
        });
    }

    /**
     * Hiển thị toast lỗi
     */
    error(message, options = {}) {
        return this.show({
            type: 'error',
            message: message,
            icon: 'fas fa-times',
            ...options
        });
    }

    /**
     * Hiển thị toast cảnh báo
     */
    warning(message, options = {}) {
        return this.show({
            type: 'warning',
            message: message,
            icon: 'fas fa-exclamation-triangle',
            ...options
        });
    }

    /**
     * Hiển thị toast thông tin
     */
    info(message, options = {}) {
        return this.show({
            type: 'info',
            message: message,
            icon: 'fas fa-info-circle',
            ...options
        });
    }

    /**
     * Hiển thị toast với cấu hình tùy chỉnh
     */
    show(config) {
        const {
            type = 'info',
            message = '',
            description = '',
            icon = 'fas fa-bell',
            duration = 4000,
            closable = true,
            showProgress = true
        } = config;

        // Tạo ID duy nhất cho toast
        const toastId = 'toast_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);

        // Tạo element toast
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.setAttribute('data-toast-id', toastId);

        // Tạo nội dung toast
        toast.innerHTML = `
            <div class="toast-icon">
                <i class="${icon}"></i>
            </div>
            <div class="toast-content">
                <div class="toast-message">${message}</div>
                ${description ? `<div class="toast-description">${description}</div>` : ''}
            </div>
            ${closable ? `
                <button class="toast-close" type="button">
                    <i class="fas fa-times"></i>
                </button>
            ` : ''}
            ${showProgress && duration > 0 ? `
                <div class="toast-progress">
                    <div class="toast-progress-bar"></div>
                </div>
            ` : ''}
        `;

        // Thêm toast vào container
        this.container.appendChild(toast);

        // Lưu thông tin toast
        this.toasts.set(toastId, {
            element: toast,
            timer: null,
            progressTimer: null
        });

        // Xử lý nút đóng
        if (closable) {
            const closeBtn = toast.querySelector('.toast-close');
            closeBtn.addEventListener('click', () => {
                this.hide(toastId);
            });
        }

        // Hiển thị toast với animation
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // Xử lý progress bar
        if (showProgress && duration > 0) {
            const progressBar = toast.querySelector('.toast-progress-bar');
            if (progressBar) {
                progressBar.style.transform = 'scaleX(1)';
                progressBar.style.transitionDuration = duration + 'ms';
                
                requestAnimationFrame(() => {
                    progressBar.style.transform = 'scaleX(0)';
                });
            }
        }

        // Tự động ẩn toast sau duration
        if (duration > 0) {
            const timer = setTimeout(() => {
                this.hide(toastId);
            }, duration);

            this.toasts.get(toastId).timer = timer;
        }

        return toastId;
    }

    /**
     * Ẩn toast
     */
    hide(toastId) {
        const toastData = this.toasts.get(toastId);
        if (!toastData) return;

        const { element, timer, progressTimer } = toastData;

        // Clear timers
        if (timer) clearTimeout(timer);
        if (progressTimer) clearTimeout(progressTimer);

        // Ẩn với animation
        element.classList.remove('show');
        element.classList.add('hide');

        // Xóa element sau animation
        setTimeout(() => {
            if (element.parentNode) {
                element.parentNode.removeChild(element);
            }
            this.toasts.delete(toastId);
        }, 300);
    }

    /**
     * Ẩn tất cả toast
     */
    hideAll() {
        this.toasts.forEach((_, toastId) => {
            this.hide(toastId);
        });
    }

    /**
     * Cập nhật nội dung toast
     */
    update(toastId, config) {
        const toastData = this.toasts.get(toastId);
        if (!toastData) return;

        const { element } = toastData;
        const messageEl = element.querySelector('.toast-message');
        const descriptionEl = element.querySelector('.toast-description');

        if (config.message && messageEl) {
            messageEl.textContent = config.message;
        }

        if (config.description && descriptionEl) {
            descriptionEl.textContent = config.description;
        }
    }
}

// Tạo instance global
window.Toast = new ToastManager();

// Các hàm tiện ích global
window.showSuccessToast = (message, options) => window.Toast.success(message, options);
window.showErrorToast = (message, options) => window.Toast.error(message, options);
window.showWarningToast = (message, options) => window.Toast.warning(message, options);
window.showInfoToast = (message, options) => window.Toast.info(message, options);

// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra URL params để hiển thị toast
    const urlParams = new URLSearchParams(window.location.search);
    
    if (urlParams.has('toast')) {
        const toastType = urlParams.get('toast');
        const toastMessage = urlParams.get('message') || 'Thao tác thành công';
        
        // Hiển thị toast dựa trên type
        switch(toastType) {
            case 'success':
                window.Toast.success(toastMessage);
                break;
            case 'error':
                window.Toast.error(toastMessage);
                break;
            case 'warning':
                window.Toast.warning(toastMessage);
                break;
            case 'info':
                window.Toast.info(toastMessage);
                break;
        }
        
        // Xóa params khỏi URL để tránh hiển thị lại khi refresh
        const newUrl = window.location.pathname + window.location.search.replace(/[?&]toast=[^&]*/, '').replace(/[?&]message=[^&]*/, '').replace(/^&/, '?');
        window.history.replaceState({}, '', newUrl);
    }
});