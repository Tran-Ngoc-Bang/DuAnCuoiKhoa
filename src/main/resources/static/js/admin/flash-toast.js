/**
 * Global Flash Toast System
 * Hệ thống toast notification chung cho toàn bộ admin
 * Tự động detect flash attributes và hiển thị toast tương ứng
 */

// Prevent duplicate execution - singleton pattern
(function() {
    'use strict';
    
    // Check if already loaded
    if (window.FlashToastSystem) {
        console.log('Flash Toast System already loaded, skipping...');
        return;
    }
    
    // Mark as loaded
    window.FlashToastSystem = true;
    
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Flash Toast System initialized');
        
        // Kiểm tra và hiển thị toast từ flash attributes
        initFlashToastSystem();
    });
})();

/**
 * Khởi tạo hệ thống flash toast
 */
function initFlashToastSystem() {
    // Debug: Kiểm tra xem Toast có sẵn không
    console.log('Toast available:', typeof window.Toast !== 'undefined');
    
    // Đợi một chút để đảm bảo Toast đã load
    setTimeout(() => {
        checkAndShowFlashToast();
    }, 100);
}

/**
 * Kiểm tra flash attributes và hiển thị toast tương ứng
 */
function checkAndShowFlashToast() {
    // Prevent duplicate toast display
    if (window.FlashToastProcessed) {
        return;
    }
    
    window.FlashToastProcessed = true;
    
    // Kiểm tra success flash attribute
    const successMessage = getFlashAttribute('success');
    if (successMessage) {
        console.log('Showing success toast:', successMessage);
        showToast('success', successMessage);
    }
    
    // Kiểm tra error flash attribute
    const errorMessage = getFlashAttribute('error');
    if (errorMessage) {
        console.log('Showing error toast:', errorMessage);
        showToast('error', errorMessage);
    }
    
    // Kiểm tra warning flash attribute
    const warningMessage = getFlashAttribute('warning');
    if (warningMessage) {
        console.log('Showing warning toast:', warningMessage);
        showToast('warning', warningMessage);
    }
    
    // Kiểm tra info flash attribute
    const infoMessage = getFlashAttribute('info');
    if (infoMessage) {
        console.log('Showing info toast:', infoMessage);
        showToast('info', infoMessage);
    }
}

/**
 * Hiển thị toast với type và message
 */
function showToast(type, message) {
    if (typeof window.Toast !== 'undefined') {
        switch(type) {
            case 'success':
                window.Toast.success(message, {
                    icon: 'fas fa-check',
                    duration: 3000
                });
                break;
            case 'error':
                window.Toast.error(message, {
                    icon: 'fas fa-exclamation-triangle',
                    duration: 4000
                });
                break;
            case 'warning':
                window.Toast.warning(message, {
                    icon: 'fas fa-exclamation-circle',
                    duration: 4000
                });
                break;
            case 'info':
                window.Toast.info(message, {
                    icon: 'fas fa-info-circle',
                    duration: 3000
                });
                break;
        }
    } else {
        // Fallback nếu Toast chưa load
        console.warn('Toast not available, using alert fallback');
        alert(`${type.toUpperCase()}: ${message}`);
    }
}

/**
 * Lấy flash attribute từ meta tag, hidden input hoặc data attribute
 */
function getFlashAttribute(type) {
    // Thử tìm từ meta tag (ưu tiên cao nhất)
    const metaTag = document.querySelector(`meta[name="flash-${type}"]`);
    if (metaTag && metaTag.getAttribute('content')) {
        return metaTag.getAttribute('content');
    }
    
    // Thử tìm từ hidden input
    const hiddenInput = document.querySelector(`input[name="flash-${type}"]`);
    if (hiddenInput && hiddenInput.value) {
        return hiddenInput.value;
    }
    
    // Thử tìm từ data attribute trên body
    const bodyData = document.body.getAttribute(`data-flash-${type}`);
    if (bodyData) {
        return bodyData;
    }
    
    return null;
}

/**
 * Các hàm tiện ích global cho admin
 */
window.AdminToast = {
    success: function(message) {
        showToast('success', message);
    },
    
    error: function(message) {
        showToast('error', message);
    },
    
    warning: function(message) {
        showToast('warning', message);
    },
    
    info: function(message) {
        showToast('info', message);
    },
    
    // Hàm để các trang khác có thể gọi
    show: function(type, message) {
        showToast(type, message);
    }
};