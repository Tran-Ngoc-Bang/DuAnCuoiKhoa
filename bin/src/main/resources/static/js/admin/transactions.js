
document.addEventListener('DOMContentLoaded', function () {
    // Khởi tạo flatpickr cho date picker nếu có
    if (typeof flatpickr === 'function') {
        flatpickr("#startDate", {
            dateFormat: "d/m/Y",
            locale: "vi",
            defaultDate: "01/01/2025"
        });

        flatpickr("#endDate", {
            dateFormat: "d/m/Y",
            locale: "vi",
            defaultDate: "31/12/2025"
        });
    }

    // Xử lý nút xem chi tiết giao dịch
    const viewTransactionButtons = document.querySelectorAll('.view-transaction');
    const transactionDetailModal = document.getElementById('transactionDetailModal');

    if (viewTransactionButtons.length > 0 && transactionDetailModal) {
        viewTransactionButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const txId = row.querySelector('td:first-child').textContent;

                // Ở đây có thể lấy thêm thông tin từ row và điền vào modal
                // Nhưng hiện tại chỉ hiển thị modal mẫu

                transactionDetailModal.classList.add('active');
            });
        });
    }

    // Xử lý nút hoàn tiền
    const refundButtons = document.querySelectorAll('.refund-btn');
    const refundModal = document.getElementById('refundModal');

    if (refundButtons.length > 0 && refundModal) {
        refundButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                let txId, amount;

                // Kiểm tra nếu nút được bấm từ trong modal chi tiết
                if (row && row.tagName === 'TR') {
                    txId = row.querySelector('td:first-child').textContent;
                    amount = row.querySelector('td:nth-child(7)').textContent;
                } else {
                    // Lấy từ modal chi tiết giao dịch
                    txId = document.querySelector('.transaction-id h4').textContent;
                    amount = document.querySelector('.info-row:nth-child(4) .info-value').textContent;
                    // Đóng modal chi tiết giao dịch
                    transactionDetailModal.classList.remove('active');
                }

                // Cập nhật thông tin trong modal hoàn tiền
                document.getElementById('refundTransactionId').textContent = txId;
                document.getElementById('refundAmount').textContent = amount;

                // Hiển thị modal hoàn tiền
                refundModal.classList.add('active');
            });
        });
    }

    // Xử lý nút xác nhận hoàn tiền
    const confirmRefundBtn = document.getElementById('confirmRefundBtn');

    if (confirmRefundBtn) {
        confirmRefundBtn.addEventListener('click', function () {
            const txId = document.getElementById('refundTransactionId').textContent;
            const reason = document.getElementById('refundReason').value;
            const note = document.getElementById('refundNote').value;
            const notifyUser = document.getElementById('notifyUser').checked;
            const notifyCreator = document.getElementById('notifyCreator').checked;

            // Kiểm tra dữ liệu nhập
            if (!reason) {
                showToast('Vui lòng chọn lý do hoàn tiền', 'error');
                return;
            }

            // Thực hiện hoàn tiền (giả lập)
            const refundData = {
                transactionId: txId,
                reason: reason,
                note: note,
                notifyUser: notifyUser,
                notifyCreator: notifyCreator
            };

            console.log('Refund data:', refundData);

            // Cập nhật UI cho hàng được hoàn tiền
            const transactionRows = document.querySelectorAll('.transactions-table tbody tr');
            transactionRows.forEach(row => {
                if (row.querySelector('td:first-child').textContent === txId.replace('#', '')) {
                    // Đổi trạng thái
                    const statusCell = row.querySelector('td:nth-child(9)');
                    statusCell.innerHTML = '<span class="status-badge refunded">Đã hoàn tiền</span>';

                    // Đổi các nút thao tác
                    const actionCell = row.querySelector('td:last-child');
                    actionCell.innerHTML = `
                        <div class="action-buttons">
                            <button class="action-btn view-transaction" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                        </div>
                    `;

                    // Thêm sự kiện cho nút mới
                    const newViewBtn = actionCell.querySelector('.view-transaction');
                    newViewBtn.addEventListener('click', function () {
                        transactionDetailModal.classList.add('active');
                    });
                }
            });

            // Đóng modal hoàn tiền
            refundModal.classList.remove('active');

            // Hiển thị thông báo thành công
            showToast('Đã hoàn tiền thành công cho giao dịch ' + txId, 'success');
        });
    }

    // Xử lý nút duyệt/từ chối giao dịch
    const approveButtons = document.querySelectorAll('.action-btn.approve');
    const rejectButtons = document.querySelectorAll('.action-btn.reject');

    // Xử lý nút duyệt
    if (approveButtons.length > 0) {
        approveButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const txId = row.querySelector('td:first-child').textContent;
                const userName = row.querySelector('.user-info span').textContent;

                // Xác nhận duyệt yêu cầu
                if (confirm(`Xác nhận duyệt yêu cầu rút tiền cho giao dịch ${txId} của ${userName}?`)) {
                    // Cập nhật trạng thái giao dịch
                    const statusCell = row.querySelector('td:nth-child(9)');
                    statusCell.innerHTML = '<span class="status-badge completed">Hoàn thành</span>';

                    // Cập nhật các nút thao tác
                    const actionCell = row.querySelector('td:last-child');
                    actionCell.innerHTML = `
                        <div class="action-buttons">
                            <button class="action-btn view-transaction" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                            <button class="action-btn" title="Xuất hóa đơn"><i class="fas fa-file-invoice"></i></button>
                        </div>
                    `;

                    // Thêm sự kiện cho nút mới
                    const newViewBtn = actionCell.querySelector('.view-transaction');
                    newViewBtn.addEventListener('click', function () {
                        transactionDetailModal.classList.add('active');
                    });

                    // Hiển thị thông báo thành công
                    showToast(`Đã duyệt giao dịch ${txId} thành công`, 'success');
                }
            });
        });
    }

    // Xử lý nút từ chối
    if (rejectButtons.length > 0) {
        rejectButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const txId = row.querySelector('td:first-child').textContent;
                const userName = row.querySelector('.user-info span').textContent;

                // Hiển thị prompt để nhập lý do từ chối
                const reason = prompt(`Nhập lý do từ chối giao dịch ${txId} của ${userName}:`, 'Thông tin không hợp lệ');

                if (reason !== null) {
                    // Cập nhật trạng thái giao dịch
                    const statusCell = row.querySelector('td:nth-child(9)');
                    statusCell.innerHTML = '<span class="status-badge canceled">Đã hủy</span>';

                    // Cập nhật các nút thao tác
                    const actionCell = row.querySelector('td:last-child');
                    actionCell.innerHTML = `
                        <div class="action-buttons">
                            <button class="action-btn view-transaction" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                        </div>
                    `;

                    // Thêm sự kiện cho nút mới
                    const newViewBtn = actionCell.querySelector('.view-transaction');
                    newViewBtn.addEventListener('click', function () {
                        transactionDetailModal.classList.add('active');
                    });

                    // Hiển thị thông báo thành công
                    showToast(`Đã từ chối giao dịch ${txId}`, 'info');
                }
            });
        });
    }
});
