// Notification dropdown functionality - MVC pattern
function toggleNotificationDropdown() {
    console.log("Toggle notification clicked!");
    const dropdown = document.getElementById("notificationDropdown");

    if (dropdown.classList.contains("show")) {
        dropdown.classList.remove("show");
        console.log("Dropdown closed");
    } else {
        dropdown.classList.add("show");
        console.log("Dropdown opened");
    }
}

// Mark all as read function - với toast notification (simplified version)
function markAllAsRead() {
    // Hiển thị toast ngay lập tức
    if (typeof window.Toast !== 'undefined') {
        window.Toast.success('Đã đánh dấu tất cả thông báo là đã đọc', {
            icon: 'fas fa-check',
            duration: 3000
        });
    }
    
    // Cập nhật UI ngay lập tức
    updateNotificationUI();
    
    // Đóng dropdown
    const dropdown = document.getElementById("notificationDropdown");
    if (dropdown) {
        dropdown.classList.remove("show");
    }
    
    // Gửi request để cập nhật server (không chờ response)
    setTimeout(() => {
        window.location.href = '/admin/notifications/mark-all-read?redirect=' + encodeURIComponent(window.location.pathname);
    }, 1500); // Delay để user thấy toast
}

// Simple toast function for success messages
function showToast(type, message) {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : 'danger'}`;
    toast.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        ${message}
    `;
    
    // Add to page
    document.body.appendChild(toast);
    
    // Position toast
    toast.style.position = 'fixed';
    toast.style.top = '20px';
    toast.style.right = '20px';
    toast.style.zIndex = '10000';
    toast.style.minWidth = '300px';
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 3000);
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
    console.log("Notification script loaded!");

    // Close dropdown when clicking outside
    document.addEventListener("click", function (e) {
        const dropdown = document.getElementById("notificationDropdown");
        const button = document.getElementById("notificationBtn");

        if (
            dropdown &&
            button &&
            !dropdown.contains(e.target) &&
            !button.contains(e.target)
        ) {
            dropdown.classList.remove("show");
        }
    });

    // Handle notification item clicks - mark as read
    document.addEventListener("click", function (e) {
        const notificationItem = e.target.closest(".notification-item");
        if (notificationItem && notificationItem.classList.contains('unread')) {
            const notificationId = notificationItem.getAttribute('data-id');
            
            if (notificationId) {
                markSingleAsRead(notificationId, notificationItem);
            }
        }
    });
});

// Notification utilities for alerts and toasts
class NotificationManager {
    static success(message, duration = 5000) {
        return this.showAlert(message, 'success', duration);
    }

    static error(message, duration = 5000) {
        return this.showAlert(message, 'error', duration);
    }

    static warning(message, duration = 5000) {
        return this.showAlert(message, 'warning', duration);
    }

    static info(message, duration = 5000) {
        return this.showAlert(message, 'info', duration);
    }

    static showAlert(message, type = 'info', duration = 5000) {
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

    static hideAlert(alert) {
        if (!alert) return;
        
        alert.classList.add('fade-out');
        setTimeout(() => {
            alert.remove();
        }, 500);
    }
}

// Đánh dấu một thông báo đã đọc (simplified version)
function markSingleAsRead(notificationId, notificationElement) {
    // Hiển thị toast ngay lập tức
    if (typeof window.Toast !== 'undefined') {
        window.Toast.success('Đã đánh dấu thông báo là đã đọc', {
            icon: 'fas fa-check',
            duration: 2500
        });
    }
    
    // Cập nhật UI cho thông báo này
    notificationElement.classList.remove('unread');
    
    // Cập nhật badge
    updateNotificationBadge();
    
    // Gửi request để cập nhật server (không chờ response)
    setTimeout(() => {
        window.location.href = `/admin/notifications/${notificationId}/mark-read?redirect=` + encodeURIComponent(window.location.pathname);
    }, 1000); // Delay để user thấy toast
}

// Cập nhật UI thông báo
function updateNotificationUI() {
    // Đánh dấu tất cả thông báo là đã đọc
    const unreadItems = document.querySelectorAll('.notification-item.unread');
    unreadItems.forEach(item => {
        item.classList.remove('unread');
    });
    
    // Cập nhật badge
    updateNotificationBadge();
}

// Cập nhật badge số lượng thông báo
function updateNotificationBadge() {
    const badge = document.getElementById('notificationBadge');
    const unreadCount = document.querySelectorAll('.notification-item.unread').length;
    
    if (badge) {
        if (unreadCount > 0) {
            badge.textContent = unreadCount;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    }
}

// Export for global use
window.NotificationManager = NotificationManager;