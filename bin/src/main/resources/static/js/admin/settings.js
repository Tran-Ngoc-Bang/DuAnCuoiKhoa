
// Initialize the application
document.addEventListener('DOMContentLoaded', function () {
    setupEventListeners();
});

function setupEventListeners() {
    // Settings navigation
    document.querySelectorAll('.settings-nav-item').forEach(item => {
        item.addEventListener('click', function () {
            const section = this.dataset.section;
            switchSection(section);
        });
    });
}

function switchSection(sectionName) {
    // Update navigation
    document.querySelectorAll('.settings-nav-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-section="${sectionName}"]`).classList.add('active');

    // Update content
    document.querySelectorAll('.settings-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionName).classList.add('active');
}

function saveAllSettings() {
    showToast('Đang lưu cài đặt...', 'info');

    // Simulate save process
    setTimeout(() => {
        showToast('Đã lưu tất cả cài đặt thành công!', 'success');
    }, 1500);
}

function resetSettings() {
    if (confirm('Bạn có chắc chắn muốn khôi phục tất cả cài đặt về mặc định? Thao tác này không thể hoàn tác.')) {
        showToast('Đang khôi phục cài đặt mặc định...', 'info');

        setTimeout(() => {
            showToast('Đã khôi phục cài đặt mặc định thành công!', 'success');
            // Simulate page reload
            setTimeout(() => {
                location.reload();
            }, 1000);
        }, 1500);
    }
}

function exportSettings() {
    showToast('Đang xuất file cấu hình...', 'info');

    // Simulate export process
    setTimeout(() => {
        showToast('Đã xuất file cấu hình thành công!', 'success');
    }, 1500);
}

function createBackup() {
    showToast('Đang tạo backup...', 'info');

    // Simulate backup process
    setTimeout(() => {
        showToast('Đã tạo backup thành công!', 'success');
    }, 3000);
}

function viewBackups() {
    showToast('Chức năng xem danh sách backup đang được phát triển', 'info');
}

function restoreBackup() {
    if (confirm('Bạn có chắc chắn muốn phục hồi từ backup? Dữ liệu hiện tại sẽ bị ghi đè.')) {
        showToast('Chức năng phục hồi backup đang được phát triển', 'info');
    }
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const iconMap = {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-circle',
        'warning': 'fas fa-exclamation-triangle',
        'info': 'fas fa-info-circle'
    };

    toast.innerHTML = `
        <i class="${iconMap[type] || iconMap.info}"></i>
        <span>${message}</span>
        <button onclick="this.parentElement.remove()" style="background: none; border: none; color: inherit; cursor: pointer; margin-left: auto;">
            <i class="fas fa-times"></i>
        </button>
    `;

    document.getElementById('toastContainer').appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) {
            toast.remove();
        }
    }, 5000);
}
