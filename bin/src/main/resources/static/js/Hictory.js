function addActivityLog(actionText) {
    const container = document.getElementById('activityHistory');
    const log = document.createElement('div');
    log.classList.add('activity-item');

    const time = new Date().toLocaleString('vi-VN');

    // Thêm icon 📌 hoặc FontAwesome (ví dụ fa-history)
    log.innerHTML = `
      <span class="icon">📌</span>
      <strong>[${time}]</strong> ${actionText}
    `;
    container.prepend(log);

    // Tự động cuộn lên trên cùng khi có dòng mới
    container.scrollTop = 0;
}

// Thêm 3 dữ liệu mẫu:
addActivityLog('Bạn đã đổi mật khẩu.', '2025-06-05 09:15:23');
addActivityLog('Bạn đã thay đổi email đăng nhập.', '2025-06-05 10:02:45');
addActivityLog('Bạn đã bật xác thực hai bước.', '2025-06-05 10:45:12');