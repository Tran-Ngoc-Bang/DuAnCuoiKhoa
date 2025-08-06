
// Khởi tạo date picker
document.addEventListener('DOMContentLoaded', function () {
    flatpickr('#startDate', {
        dateFormat: 'd/m/Y',
        locale: 'vn',
        disableMobile: true
    });

    flatpickr('#endDate', {
        dateFormat: 'd/m/Y',
        locale: 'vn',
        disableMobile: true
    });

    // Xử lý các nút lọc nhanh theo ngày
    const quickFilters = document.querySelectorAll('.quick-filter');
    quickFilters.forEach(filter => {
        filter.addEventListener('click', function () {
            quickFilters.forEach(f => f.classList.remove('active'));
            this.classList.add('active');

            // Logic lọc theo ngày dựa trên nút được chọn
            // (Sẽ được cài đặt khi tích hợp với backend)
        });
    });

    // Xử lý mở/đóng các modal
    const modals = document.querySelectorAll('.modal');
    const modalOverlays = document.querySelectorAll('.modal-overlay');
    const modalCloseButtons = document.querySelectorAll('.modal-close');

    // Đóng modal khi nhấn vào overlay hoặc nút đóng
    modalOverlays.forEach(overlay => {
        overlay.addEventListener('click', function () {
            const modal = this.closest('.modal');
            closeModal(modal);
        });
    });

    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function () {
            const modal = this.closest('.modal');
            closeModal(modal);
        });
    });

    // Đóng modal chi tiết khi nhấn nút đóng bên trong
    document.getElementById('closeDetailBtn')?.addEventListener('click', function () {
        const modal = document.getElementById('withdrawalDetailModal');
        closeModal(modal);
    });

    // Từ chối - hủy
    document.getElementById('cancelRejectBtn')?.addEventListener('click', function () {
        const modal = document.getElementById('rejectWithdrawalModal');
        closeModal(modal);
    });

    // Hủy xử lý chuyển khoản
    document.getElementById('cancelProcessBtn')?.addEventListener('click', function () {
        const modal = document.getElementById('processWithdrawalModal');
        closeModal(modal);
    });

    // Hủy hoàn thành
    document.getElementById('cancelCompleteBtn')?.addEventListener('click', function () {
        const modal = document.getElementById('completeWithdrawalModal');
        closeModal(modal);
    });

    // Hàm đóng modal
    function closeModal(modal) {
        if (modal) {
            modal.classList.remove('active');
        }
    }

    // Hàm mở modal
    function openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
        }
    }

    // Xử lý nút xem chi tiết
    const viewWithdrawalButtons = document.querySelectorAll('.view-withdrawal');
    viewWithdrawalButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Làm trong thực tế: Lấy ID yêu cầu rút tiền và gọi API để lấy thông tin chi tiết

            // Hiện thị modal chi tiết
            openModal('withdrawalDetailModal');
        });
    });

    // Xử lý nút duyệt yêu cầu
    const approveButtons = document.querySelectorAll('.action-btn.approve');
    approveButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Lấy id yêu cầu từ hàng dữ liệu
            const withdrawalRow = this.closest('tr');
            const withdrawalId = withdrawalRow.querySelector('td:first-child').textContent;
            const userName = withdrawalRow.querySelector('.user-info span').textContent;

            // Xử lý duyệt yêu cầu (thực hiện API trong thực tế)
            if (confirm(`Xác nhận duyệt yêu cầu rút tiền #${withdrawalId} của ${userName}?`)) {
                // Gọi API và cập nhật UI
                // Ví dụ: updateWithdrawalStatus(withdrawalId, 'approved')

                // Cập nhật giao diện
                const statusCell = withdrawalRow.querySelector('td:nth-child(7)');
                statusCell.innerHTML = '<span class="status-badge approved">Đã duyệt</span>';

                // Thay đổi các nút hành động
                const actionButtons = withdrawalRow.querySelector('.action-buttons');
                actionButtons.innerHTML = `
                    <button class="action-btn process" title="Xử lý chuyển khoản"><i class="fas fa-money-bill-wave"></i></button>
                    <button class="action-btn view-withdrawal" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                `;

                // Gắn lại sự kiện cho các nút mới
                const newViewButton = actionButtons.querySelector('.view-withdrawal');
                newViewButton.addEventListener('click', function () {
                    openModal('withdrawalDetailModal');
                });

                const newProcessButton = actionButtons.querySelector('.process');
                newProcessButton.addEventListener('click', function () {
                    openModal('processWithdrawalModal');
                });

                // Hiển thị thông báo
                showToast('success', `Đã duyệt yêu cầu rút tiền #${withdrawalId}`);
            }
        });
    });

    // Xử lý nút từ chối yêu cầu
    const rejectButtons = document.querySelectorAll('.action-btn.reject');
    let currentWithdrawalId = '';
    let currentWithdrawalRow = null;

    rejectButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Lấy id yêu cầu từ hàng dữ liệu
            currentWithdrawalRow = this.closest('tr');
            currentWithdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;

            // Mở modal từ chối
            openModal('rejectWithdrawalModal');
        });
    });

    // Xử lý nút xác nhận từ chối yêu cầu
    document.getElementById('confirmRejectBtn')?.addEventListener('click', function () {
        const withdrawalId = document.querySelector('#rejectWithdrawalModal').dataset.withdrawalId;
        const reasonSelect = document.getElementById('withdrawalRejectReason');
        const noteTextarea = document.getElementById('withdrawalRejectNote');
        const notifyUser = document.getElementById('notifyUserReject').checked;
        const confirmBtn = this;

        // Xác thực form trước khi xử lý
        if (!validateRejectForm(reasonSelect, noteTextarea)) {
            return; // Dừng nếu form không hợp lệ
        }

        // Hiển thị hộp thoại xác nhận trước khi từ chối
        if (confirm('Bạn có chắc chắn muốn từ chối yêu cầu rút tiền này không?')) {

            // Hiển thị trạng thái loading
            showLoadingState(confirmBtn, 'Đang xử lý...');

            // Gọi API từ chối yêu cầu (thực hiện trong thực tế)
            // Ví dụ: rejectWithdrawal(currentWithdrawalId, reasonSelect.value, noteTextarea.value, notifyUser)

            // Mô phỏng API call (xóa trong môi trường thực tế)
            setTimeout(() => {
                // Cập nhật giao diện
                if (currentWithdrawalRow) {
                    const statusCell = currentWithdrawalRow.querySelector('td:nth-child(7)');
                    statusCell.innerHTML = '<span class="status-badge refunded">Đã từ chối</span>';

                    // Thay đổi các nút hành động
                    const actionButtons = currentWithdrawalRow.querySelector('.action-buttons');
                    actionButtons.innerHTML = `
                    <button class="action-btn view-withdrawal" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                `;

                    // Gắn lại sự kiện cho nút xem chi tiết
                    const newViewButton = actionButtons.querySelector('.view-withdrawal');
                    newViewButton.addEventListener('click', function () {
                        const withdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;
                        loadWithdrawalDetail(withdrawalId);
                        openModal('withdrawalDetailModal');
                    });
                }

                // Đóng modal và reset form
                closeModal(document.getElementById('rejectWithdrawalModal'));
                reasonSelect.value = '';
                noteTextarea.value = '';

                // Hiển thị thông báo
                showToast('error', `Đã từ chối yêu cầu rút tiền #${currentWithdrawalId}`);

                // Reset biến tạm
                currentWithdrawalId = '';
                currentWithdrawalRow = null;

                // Trả nút về trạng thái ban đầu
                resetLoadingState(confirmBtn, 'Xác nhận');
            }, 800); // Thời gian mô phỏng API call
        }
    });

    // Hàm xác thực form từ chối
    function validateRejectForm(reasonSelect, noteTextarea) {
        // Xóa các thông báo lỗi cũ (nếu có)
        document.querySelectorAll('.form-error').forEach(error => error.remove());

        let isValid = true;

        // Xác thực lý do từ chối
        if (!reasonSelect.value) {
            showFormError(reasonSelect, 'Vui lòng chọn lý do từ chối');
            isValid = false;
        }

        // Xác thực ghi chú
        if (!noteTextarea.value.trim()) {
            showFormError(noteTextarea, 'Vui lòng nhập ghi chú');
            isValid = false;
        }

        return isValid;
    }

    // Xử lý nút xử lý chuyển khoản
    const processButtons = document.querySelectorAll('.action-btn.process');
    processButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Lấy id yêu cầu từ hàng dữ liệu
            currentWithdrawalRow = this.closest('tr');
            currentWithdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;

            // Mở modal xử lý chuyển khoản
            openModal('processWithdrawalModal');

            // Cập nhật thông tin của yêu cầu vào modal
            // Thực hiện trong thực tế: Các giá trị sẽ được lấy từ API
        });
    });

    // Xử lý nút xác nhận xử lý chuyển khoản
    // Helper function để hiển thị lỗi form
    function showFormError(element, message) {
        const formGroup = element.closest('.form-group');
        if (formGroup) {
            formGroup.classList.add('error');
            const errorSpan = document.createElement('span');
            errorSpan.className = 'form-error';
            errorSpan.textContent = message;
            formGroup.appendChild(errorSpan);
        }
    }

    // Helper function để đặt trạng thái loading cho nút
    function showLoadingState(button, loadingText) {
        button.classList.add('btn-loading');
        button.disabled = true;

        // Lưu text gốc vào thuộc tính data
        button.dataset.originalText = button.textContent;
        button.textContent = loadingText || 'Đang xử lý...';
    }

    // Helper function để reset trạng thái loading cho nút
    function resetLoadingState(button, originalText) {
        button.classList.remove('btn-loading');
        button.disabled = false;

        // Khôi phục text gốc
        button.textContent = originalText || button.dataset.originalText || 'Xác nhận';
    }

    // Xử lý nút xác nhận xử lý
    document.getElementById('confirmProcessBtn')?.addEventListener('click', function () {
        const transactionCode = document.getElementById('transactionCode');
        const processNote = document.getElementById('processNote');
        const notifyUser = document.getElementById('notifyUserProcess').checked;
        const confirmBtn = this;

        // Xác thực form trước khi xử lý
        if (!validateProcessForm(transactionCode, processNote)) {
            return; // Dừng nếu form không hợp lệ
        }

        // Hiển thị trạng thái loading
        showLoadingState(confirmBtn, 'Đang xử lý...');

        // Gọi API xử lý yêu cầu (thực hiện trong thực tế)
        // Ví dụ: processWithdrawal(currentWithdrawalId, transactionCode.value, processNote.value, notifyUser)

        // Mô phỏng API call (xóa trong môi trường thực tế)
        setTimeout(() => {
            // Cập nhật giao diện
            if (currentWithdrawalRow) {
                const statusCell = currentWithdrawalRow.querySelector('td:nth-child(7)');
                statusCell.innerHTML = '<span class="status-badge processing">Đang xử lý</span>';

                // Thay đổi các nút hành động
                const actionButtons = currentWithdrawalRow.querySelector('.action-buttons');
                actionButtons.innerHTML = `
                    <button class="action-btn complete" title="Hoàn thành"><i class="fas fa-check-double"></i></button>
                    <button class="action-btn view-withdrawal" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                `;

                // Gắn lại sự kiện cho các nút
                const newViewButton = actionButtons.querySelector('.view-withdrawal');
                newViewButton.addEventListener('click', function () {
                    const withdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;
                    loadWithdrawalDetail(withdrawalId);
                    openModal('withdrawalDetailModal');
                });

                const newCompleteButton = actionButtons.querySelector('.action-btn.complete');
                newCompleteButton.addEventListener('click', function () {
                    currentWithdrawalRow = this.closest('tr');
                    currentWithdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;
                    openModal('completeWithdrawalModal');
                });
            }

            // Đóng modal và reset form
            closeModal(document.getElementById('processWithdrawalModal'));
            transactionCode.value = '';
            processNote.value = '';

            // Hiển thị thông báo
            showToast('info', `Đã chuyển yêu cầu rút tiền #${currentWithdrawalId} sang trạng thái đang xử lý`);

            // Reset biến tạm
            currentWithdrawalId = '';
            currentWithdrawalRow = null;

            // Trả nút về trạng thái ban đầu
            resetLoadingState(confirmBtn, 'Xác nhận');
        }, 800);
    });

    // Hàm xác thực form xử lý
    function validateProcessForm(transactionCode, processNote) {
        // Xóa các thông báo lỗi cũ (nếu có)
        document.querySelectorAll('.form-error').forEach(error => error.remove());
        document.querySelectorAll('.form-group.error').forEach(group => group.classList.remove('error'));

        let isValid = true;

        // Xác thực mã giao dịch
        if (!transactionCode.value.trim()) {
            showFormError(transactionCode, 'Vui lòng nhập mã giao dịch');
            isValid = false;
        }

        // Xác thực ghi chú (không bắt buộc nhưng nên có)
        if (!processNote.value.trim()) {
            showFormError(processNote, 'Vui lòng nhập ghi chú về giao dịch');
            isValid = false;
        }

        return isValid;
    }

    // Xử lý nút hoàn thành
    const completeButtons = document.querySelectorAll('.action-btn.complete');
    completeButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Lấy id yêu cầu từ hàng dữ liệu
            currentWithdrawalRow = this.closest('tr');
            currentWithdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;

            // Mở modal hoàn thành
            openModal('completeWithdrawalModal');
        });
    });

    // Xử lý nút xác nhận hoàn thành
    document.getElementById('confirmCompleteBtn')?.addEventListener('click', function () {
        const completeNote = document.getElementById('completeNote');
        const notifyUser = document.getElementById('notifyUserComplete').checked;
        const confirmBtn = this;

        // Xác thực form trước khi xử lý
        if (!validateCompleteForm(completeNote)) {
            return; // Dừng nếu form không hợp lệ
        }

        // Hiển thị trạng thái loading
        showLoadingState(confirmBtn, 'Đang xử lý...');

        // Gọi API hoàn thành yêu cầu (thực hiện trong thực tế)
        // Ví dụ: completeWithdrawal(currentWithdrawalId, completeNote.value, notifyUser)

        // Mô phỏng API call (xóa trong môi trường thực tế)
        setTimeout(() => {
            // Cập nhật giao diện
            if (currentWithdrawalRow) {
                const statusCell = currentWithdrawalRow.querySelector('td:nth-child(7)');
                statusCell.innerHTML = '<span class="status-badge completed">Đã hoàn thành</span>';

                // Thay đổi các nút hành động
                const actionButtons = currentWithdrawalRow.querySelector('.action-buttons');
                actionButtons.innerHTML = `
                    <button class="action-btn view-withdrawal" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                `;

                // Gắn lại sự kiện cho nút xem chi tiết
                const newViewButton = actionButtons.querySelector('.view-withdrawal');
                newViewButton.addEventListener('click', function () {
                    const withdrawalId = currentWithdrawalRow.querySelector('td:first-child').textContent;
                    loadWithdrawalDetail(withdrawalId);
                    openModal('withdrawalDetailModal');
                });
            }

            // Đóng modal và reset form
            closeModal(document.getElementById('completeWithdrawalModal'));
            completeNote.value = '';

            // Hiển thị thông báo
            showToast('success', `Đã hoàn thành yêu cầu rút tiền #${currentWithdrawalId}`);

            // Reset biến tạm
            currentWithdrawalId = '';
            currentWithdrawalRow = null;

            // Trả nút về trạng thái ban đầu
            resetLoadingState(confirmBtn, 'Xác nhận');
        }, 800);
    });

    // Hàm xác thực form hoàn thành
    function validateCompleteForm(completeNote) {
        // Xóa các thông báo lỗi cũ (nếu có)
        document.querySelectorAll('.form-error').forEach(error => error.remove());
        document.querySelectorAll('.form-group.error').forEach(group => group.classList.remove('error'));

        let isValid = true;

        // Xác thực ghi chú
        if (!completeNote.value.trim()) {
            showFormError(completeNote, 'Vui lòng nhập ghi chú về việc hoàn thành');
            isValid = false;
        }

        return isValid;
    }

    // Xử lý hiển thị thông báo toast
    function showToast(type, message) {
        // Tạo toast element
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        // Icon cho từng loại toast
        const icons = {
            'success': 'fa-check-circle',
            'error': 'fa-exclamation-circle',
            'warning': 'fa-exclamation-triangle',
            'info': 'fa-info-circle'
        };

        // Nội dung toast
        toast.innerHTML = `
            <div class="toast-content">
                <i class="fas ${icons[type]}"></i>
                <div class="toast-message">${message}</div>
            </div>
            <i class="fas fa-times toast-close"></i>
        `;

        // Thêm vào document
        document.body.appendChild(toast);

        // Hiện toast
        setTimeout(() => {
            toast.classList.add('active');
        }, 10);

        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            toast.classList.remove('active');
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, 5000);

        // Xử lý nút đóng
        const closeBtn = toast.querySelector('.toast-close');
        closeBtn.addEventListener('click', function () {
            toast.classList.remove('active');
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        });
    }

    // ----- Xử lý các nút trong modal chi tiết rút tiền -----
    // Khai báo biến để lưu trữ dữ liệu của yêu cầu rút tiền đang xem
    let currentDetailWithdrawalId = '';

    // Nút đóng modal chi tiết
    document.getElementById('closeDetailBtn')?.addEventListener('click', function () {
        closeModal(document.getElementById('withdrawalDetailModal'));
    });

    // Nút từ chối trong modal chi tiết
    document.querySelector('.reject-withdrawal-btn')?.addEventListener('click', function () {
        // Lấy ID từ modal chi tiết
        currentWithdrawalId = document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');

        // Đóng modal chi tiết
        closeModal(document.getElementById('withdrawalDetailModal'));

        // Mở modal từ chối
        openModal('rejectWithdrawalModal');
    });

    // Nút xử lý trong modal chi tiết
    document.querySelector('.process-withdrawal-btn')?.addEventListener('click', function () {
        // Lấy ID từ modal chi tiết
        currentWithdrawalId = document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');

        // Đóng modal chi tiết
        closeModal(document.getElementById('withdrawalDetailModal'));

        // Mở modal xử lý
        openModal('processWithdrawalModal');
    });

    // Nút hoàn thành trong modal chi tiết
    document.querySelector('.approve-withdrawal-btn')?.addEventListener('click', function () {
        // Lấy ID từ modal chi tiết
        currentWithdrawalId = document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');

        // Đóng modal chi tiết
        closeModal(document.getElementById('withdrawalDetailModal'));

        // Mở modal hoàn thành
        openModal('completeWithdrawalModal');
    });

    // Nút gửi email trong modal chi tiết
    document.querySelector('.email-btn')?.addEventListener('click', function () {
        // Lấy ID từ modal chi tiết
        const withdrawalId = document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');
        const emailBtn = this;

        // Hiển thị trạng thái loading
        showLoadingState(emailBtn, 'Đang gửi...');

        // Vô hiệu hóa nút trong khi xử lý
        emailBtn.disabled = true;

        // Mô phỏng gọi API gửi email (trong môi trường thực tế sẽ gọi API thực)
        // Ví dụ: sendEmail(withdrawalId, 'withdrawal_update')
        setTimeout(() => {
            // Mô phỏng API gọi thành công
            showToast('success', `Đã gửi email thông báo thành công đến khách hàng #${withdrawalId}`);

            // Cập nhật lịch sử giao dịch bằng hàm chung
            updateWithdrawalHistory(withdrawalId, 'envelope', 'Gửi email thông báo', 'Đã gửi email thông báo trạng thái giao dịch đến khách hàng.');

            // Trả nút về trạng thái ban đầu
            resetLoadingState(emailBtn, '<i class="fas fa-envelope"></i> Gửi email');
            emailBtn.disabled = false;
        }, 1200);
    });

    // Nút lịch sử trong modal chi tiết
    document.querySelector('.history-btn')?.addEventListener('click', function () {
        // Lấy ID từ modal chi tiết
        const withdrawalId = document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');
        const historyBtn = this;
        const historySection = document.querySelector('.withdrawal-timeline');

        // Hiển thị trạng thái loading
        showLoadingState(historyBtn, 'Đang tải...');

        // Kiểm tra xem phần lịch sử có tồn tại không
        if (historySection) {
            // Đảm bảo phần lịch sử được hiển thị
            if (historySection.style.display === 'none') {
                historySection.style.display = 'block';
            }

            // Thêm hiệu ứng mở rộng của phần lịch sử nếu nó đang thu gọn
            const timelineContainer = historySection.closest('.detail-section');
            if (timelineContainer && timelineContainer.classList.contains('collapsed')) {
                // Tự động mở rộng nếu đang thu gọn
                const expandBtn = timelineContainer.querySelector('.expand-btn');
                if (expandBtn) {
                    expandBtn.click();
                }
            }

            setTimeout(() => {
                // Scroll đến khu vực lịch sử
                historySection.scrollIntoView({ behavior: 'smooth', block: 'start' });

                // Thêm hiệu ứng highlight
                const originalBackground = historySection.style.backgroundColor || 'transparent';
                const originalTransition = historySection.style.transition || '';

                historySection.style.transition = 'background-color 0.3s ease-in-out';
                historySection.style.backgroundColor = 'rgba(67, 97, 238, 0.1)';

                setTimeout(() => {
                    historySection.style.backgroundColor = originalBackground;
                    setTimeout(() => {
                        historySection.style.transition = originalTransition;
                    }, 500);
                }, 1500);

                // Thêm hiệu ứng nhấp nháy cho tiêu đề lịch sử
                const historyTitle = timelineContainer?.querySelector('.section-title');
                if (historyTitle) {
                    historyTitle.classList.add('highlight-text');
                    setTimeout(() => {
                        historyTitle.classList.remove('highlight-text');
                    }, 2000);
                }

                // Trả nút về trạng thái ban đầu
                resetLoadingState(historyBtn, '<i class="fas fa-history"></i> Lịch sử');

                // Cập nhật lịch sử
                updateWithdrawalHistory(withdrawalId, 'clock', 'Xem lịch sử', 'Quản trị viên đã xem lịch sử thay đổi của yêu cầu rút tiền.');
                showToast('info', 'Hiển thị lịch sử thay đổi của yêu cầu rút tiền');
            }, 400);
        } else {
            // Nếu không tìm thấy phần lịch sử
            showToast('error', 'Không tìm thấy lịch sử giao dịch');
            resetLoadingState(historyBtn, '<i class="fas fa-history"></i> Lịch sử');
        }
    });

    // Xử lý textarea ghi chú admin với debounce để tránh lưu quá nhiều
    const adminNotesTextarea = document.querySelector('#withdrawalDetailModal .admin-notes');
    let saveTimeout;

    adminNotesTextarea?.addEventListener('input', function () {
        const textareaWrapper = this.closest('.admin-notes-section');

        // Xóa hẹn giờ trước nếu có
        clearTimeout(saveTimeout);

        // Xóa bịt thông báo đã lưu cũ nếu có
        const oldIndicator = textareaWrapper.querySelector('.save-indicator');
        if (oldIndicator) {
            oldIndicator.remove();
        }

        // Tạo đối tượng thông báo đang soạn thảo
        const editingIndicator = document.createElement('span');
        editingIndicator.className = 'save-indicator editing';
        editingIndicator.textContent = 'Đang soạn thảo...';
        textareaWrapper.appendChild(editingIndicator);

        // Lưu sau 800ms không có thao tác
        saveTimeout = setTimeout(() => {
            // Lấy ID từ biến toàn cục hoặc từ modal
            const withdrawalId = currentWithdrawalId || document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');
            localStorage.setItem(`withdrawal_note_${withdrawalId}`, this.value);

            // Xóa thông báo đang soạn thảo
            if (editingIndicator) {
                editingIndicator.remove();
            }

            // Hiển thị thông báo đã lưu
            const savedIndicator = document.createElement('span');
            savedIndicator.className = 'save-indicator saved';
            savedIndicator.textContent = 'Đã lưu';
            textareaWrapper.appendChild(savedIndicator);

            // Tự động xóa thông báo sau 2 giây
            setTimeout(() => {
                if (savedIndicator) {
                    savedIndicator.remove();
                }
            }, 2000);

            // Cập nhật lịch sử (chỉ khi nội dung có thay đổi và không rỗng)
            if (this.value.trim() && withdrawalId) {
                // Cập nhật lịch sử giao dịch (mô phỏng)
                updateWithdrawalHistory(withdrawalId, 'note', 'Cập nhật ghi chú', 'Quản trị viên đã cập nhật ghi chú cho yêu cầu rút tiền.');
            }

        }, 800);
    });

    // Hàm cập nhật lịch sử giao dịch
    function updateWithdrawalHistory(withdrawalId, type, title, description) {
        const historySection = document.querySelector('.withdrawal-timeline');
        if (!historySection) return;

        // Tạo icon phù hợp với loại hành động
        const iconMap = {
            'reject': 'fa-times-circle',
            'process': 'fa-sync',
            'complete': 'fa-check-circle',
            'email': 'fa-envelope',
            'note': 'fa-sticky-note',
            'edit': 'fa-edit',
            'view': 'fa-eye'
        };

        const icon = iconMap[type] || 'fa-info-circle';

        // Tạo mục lịch sử mới với icon và nội dung
        const newHistoryItem = document.createElement('div');
        newHistoryItem.className = 'timeline-item';
        const now = new Date();
        const timeString = now.toLocaleString('vi-VN');

        // Tạo nội dung HTML cho mục lịch sử
        newHistoryItem.innerHTML = `
            <div class="timeline-icon">
                <i class="fas ${icon}"></i>
            </div>
            <div class="timeline-content">
                <div class="timeline-time">${timeString}</div>
                <div class="timeline-title">${title}</div>
                <div class="timeline-desc">${description}</div>
            </div>
        `;

        // Chèn vào đầu danh sách lịch sử (để mội nhất hiển thị trên cùng)
        historySection.prepend(newHistoryItem);

        // Thêm hiệu ứng highlight cho mục mới thêm vào
        setTimeout(() => {
            newHistoryItem.classList.add('highlight');
            setTimeout(() => {
                newHistoryItem.classList.remove('highlight');
            }, 2000);
        }, 100);

        // Phần này đã được xử lý ở trên
    }

    // Xử lý đóng mọi modal an toàn
    function closeAllModals() {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            if (modal.classList.contains('open')) {
                closeModal(modal);
            }
        });
    }

    // Xử lý lưu trạng thái các modal trong session
    function saveModalState() {
        // Lưu trạng thái modal đang mở
        const openModal = document.querySelector('.modal.open');
        if (openModal) {
            sessionStorage.setItem('lastOpenModal', openModal.id);
        } else {
            sessionStorage.removeItem('lastOpenModal');
        }
    }

    // Hàm để load dữ liệu vào modal chi tiết rút tiền
    function loadWithdrawalDetail(withdrawalId) {
        // Cập nhật ID và lưu lại ID hiện tại vào biến toàn cục
        document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent = '#' + withdrawalId;
        currentDetailWithdrawalId = withdrawalId;
        currentWithdrawalId = withdrawalId;

        // Tìm hàng tương ứng trong bảng để lấy trạng thái thực tế
        const allTableRows = document.querySelectorAll('table.withdrawal-table tbody tr');
        let tableRow = null;
        let currentStatus = 'Chờ xử lý'; // Mặc định trạng thái

        allTableRows.forEach(row => {
            const idCell = row.querySelector('td:first-child');
            if (idCell && idCell.textContent.trim() === withdrawalId) {
                tableRow = row;
                const statusBadge = row.querySelector('.status-badge');
                if (statusBadge) {
                    currentStatus = statusBadge.textContent.trim();
                }
            }
        });

        // Lấy ghi chú từ localStorage nếu có
        const notesTextarea = document.querySelector('#withdrawalDetailModal .admin-notes');
        if (notesTextarea) {
            notesTextarea.value = localStorage.getItem(`withdrawal_note_${withdrawalId}`) || '';
        }

        // Hiển thị/ẩn các nút hành động dựa trên trạng thái hiện tại
        const detailModal = document.getElementById('withdrawalDetailModal');
        const rejectBtn = detailModal.querySelector('.reject-withdrawal-btn');
        const processBtn = detailModal.querySelector('.process-withdrawal-btn');
        const approveBtn = detailModal.querySelector('.approve-withdrawal-btn');
        const emailBtn = detailModal.querySelector('.email-btn');
        const historyBtn = detailModal.querySelector('.history-btn');

        // Ẩn tất cả các nút hành động trước (nhưng vẫn giữ email và lịch sử)
        if (rejectBtn) rejectBtn.style.display = 'none';
        if (processBtn) processBtn.style.display = 'none';
        if (approveBtn) approveBtn.style.display = 'none';

        // Hiển thị nút dựa trên trạng thái
        switch (currentStatus) {
            case 'Chờ xử lý':
                // Hiển thị nút Từ chối và Xử lý
                if (rejectBtn) rejectBtn.style.display = 'inline-block';
                if (processBtn) processBtn.style.display = 'inline-block';
                break;

            case 'Đã duyệt':
            case 'Đang xử lý':
                // Hiển thị nút Hoàn thành và Từ chối
                if (approveBtn) approveBtn.style.display = 'inline-block';
                if (rejectBtn) rejectBtn.style.display = 'inline-block';
                break;

            case 'Đã hoàn thành':
            case 'Đã từ chối':
                // Không hiển thị nút hành động nào
                break;

            default:
                // Trường hợp mặc định hiển thị tất cả các nút
                if (rejectBtn) rejectBtn.style.display = 'inline-block';
                if (processBtn) processBtn.style.display = 'inline-block';
                break;
        }

        // Lưu ghi lại hoạt động xem chi tiết trong lịch sử
        setTimeout(() => {
            updateWithdrawalHistory(withdrawalId, 'view', 'Xem chi tiết yêu cầu', 'Quản trị viên đã xem chi tiết yêu cầu rút tiền.');
        }, 100);

        // TODO: Cập nhật các thông tin khác từ API thực tế trong tương lai
    }

    // Gắn sự kiện cho các nút xem chi tiết
    document.querySelectorAll('.view-withdrawal').forEach(button => {
        button.addEventListener('click', function () {
            // Lấy ID từ hàng dữ liệu
            const withdrawalId = this.closest('tr').querySelector('td:first-child').textContent;

            // Load dữ liệu vào modal
            loadWithdrawalDetail(withdrawalId);

            // Mở modal
            openModal('withdrawalDetailModal');
        });
    });

    // Xử lý textarea ghi chú admin với debounce để tránh lưu quá nhiều
    document.querySelector('#withdrawalDetailModal .admin-notes')?.addEventListener('input', function () {
        const textareaWrapper = this.closest('.admin-notes-section');
        let noteSaveTimeout;

        // Xóa hẹn giờ trước nếu có
        clearTimeout(noteSaveTimeout);

        // Xóa thông báo đã lưu cũ nếu có
        const oldIndicator = textareaWrapper.querySelector('.save-indicator');
        if (oldIndicator) {
            oldIndicator.remove();
        }

        // Tạo đối tượng thông báo đang soạn thảo
        const editingIndicator = document.createElement('span');
        editingIndicator.className = 'save-indicator editing';
        editingIndicator.textContent = 'Đang soạn thảo...';
        textareaWrapper.appendChild(editingIndicator);

        // Lưu sau 800ms không có thao tác
        noteSaveTimeout = setTimeout(() => {
            // Lấy ID từ biến toàn cục hoặc từ modal
            const withdrawalId = currentWithdrawalId || document.querySelector('#withdrawalDetailModal .withdrawal-id').textContent.replace('#', '');
            localStorage.setItem(`withdrawal_note_${withdrawalId}`, this.value);

            // Xóa thông báo đang soạn thảo
            if (editingIndicator) {
                editingIndicator.remove();
            }

            // Hiển thị thông báo đã lưu
            const savedIndicator = document.createElement('span');
            savedIndicator.className = 'save-indicator saved';
            savedIndicator.textContent = 'Đã lưu';
            textareaWrapper.appendChild(savedIndicator);

            // Tự động xóa thông báo sau 2 giây
            setTimeout(() => {
                if (savedIndicator) {
                    savedIndicator.remove();
                }
            }, 2000);

            // Cập nhật lịch sử ghi chú
            updateWithdrawalHistory(withdrawalId, 'note', 'Cập nhật ghi chú', 'Quản trị viên đã cập nhật ghi chú cho yêu cầu rút tiền.');
        }, 800);
    });

    // Xử lý tải file minh chứng
    const transferProofInput = document.getElementById('transferProof');
    if (transferProofInput) {
        transferProofInput.addEventListener('change', function () {
            const filePreview = document.querySelector('.file-preview');

            // Xóa nội dung hiện tại
            filePreview.innerHTML = '';

            if (this.files && this.files[0]) {
                const file = this.files[0];
                const fileExt = file.name.split('.').pop().toLowerCase();

                // Kiểm tra định dạng file
                const allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'pdf'];
                if (!allowedExts.includes(fileExt)) {
                    alert('Định dạng file không hợp lệ. Chỉ chấp nhận file ảnh hoặc PDF.');
                    this.value = '';
                    return;
                }

                // Kiểm tra kích thước file (tối đa 5MB)
                if (file.size > 5 * 1024 * 1024) {
                    alert('Kích thước file quá lớn. Tối đa 5MB.');
                    this.value = '';
                    return;
                }

                // Hiển thị thông tin file
                const fileItem = document.createElement('div');
                fileItem.className = 'file-item';

                // Icon tương ứng với loại file
                let fileIcon = '';
                if (fileExt === 'pdf') {
                    fileIcon = '<i class="fas fa-file-pdf"></i>';
                } else {
                    fileIcon = '<i class="fas fa-file-image"></i>';
                }

                // Hiển thị thông tin file
                fileItem.innerHTML = `
                    ${fileIcon}
                    <div class="file-info">
                        <div class="file-name">${file.name}</div>
                        <div class="file-size">${formatFileSize(file.size)}</div>
                    </div>
                    <button type="button" class="remove-file"><i class="fas fa-times"></i></button>
                `;

                filePreview.appendChild(fileItem);

                // Xử lý nút xóa file
                const removeBtn = fileItem.querySelector('.remove-file');
                removeBtn.addEventListener('click', function () {
                    transferProofInput.value = '';
                    filePreview.innerHTML = '';
                });
            }
        });
    }

    // Hàm định dạng kích thước file
    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        else return (bytes / 1048576).toFixed(1) + ' MB';
    }

    // ----------------- Xử lý phân trang -----------------
    const paginationButtons = document.querySelectorAll('.pagination-btn');
    paginationButtons.forEach(button => {
        if (!button.classList.contains('prev-btn') && !button.classList.contains('next-btn')) {
            button.addEventListener('click', function () {
                // Bỏ chọn tất cả các nút
                paginationButtons.forEach(btn => btn.classList.remove('active'));

                // Chọn nút hiện tại
                this.classList.add('active');

                // Thực hiện hàm tìm kiếm với trang mới
                filterWithdrawals();

                // Cập nhật trạng thái nút prev/next
                updatePaginationButtonsState();
            });
        }
    });

    // Xử lý nút Previous và Next
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');

    prevBtn?.addEventListener('click', function () {
        if (!this.disabled) {
            const activePage = document.querySelector('.pagination-btn.active');
            if (activePage && activePage.previousElementSibling && !activePage.previousElementSibling.classList.contains('prev-btn')) {
                activePage.previousElementSibling.click();
            }
        }
    });

    nextBtn?.addEventListener('click', function () {
        if (!this.disabled) {
            const activePage = document.querySelector('.pagination-btn.active');
            if (activePage && activePage.nextElementSibling && !activePage.nextElementSibling.classList.contains('next-btn')) {
                activePage.nextElementSibling.click();
            }
        }
    });

    function updatePaginationButtonsState() {
        const activePage = document.querySelector('.pagination-btn.active');

        // Kiểm tra và cập nhật trạng thái cho nút prev
        if (prevBtn && activePage) {
            prevBtn.disabled = !activePage.previousElementSibling || activePage.previousElementSibling.classList.contains('prev-btn');
        }

        // Kiểm tra và cập nhật trạng thái cho nút next
        if (nextBtn && activePage) {
            nextBtn.disabled = !activePage.nextElementSibling || activePage.nextElementSibling.classList.contains('next-btn');
        }
    }

    // ----------------- Xử lý date range picker -----------------
    // Khởi tạo date picker với Flatpickr sau khi trang đã tải xong
    let startDatePicker, endDatePicker;

    function initDatePickers() {
        startDatePicker = flatpickr('#startDate', {
            dateFormat: 'd/m/Y',
            locale: 'vn',
            onChange: function (selectedDates, dateStr) {
                // Cập nhật bộ lọc khi người dùng chọn ngày
                filterWithdrawals();
            }
        });

        endDatePicker = flatpickr('#endDate', {
            dateFormat: 'd/m/Y',
            locale: 'vn',
            onChange: function (selectedDates, dateStr) {
                // Cập nhật bộ lọc khi người dùng chọn ngày
                filterWithdrawals();
            }
        });

        // Xử lý các nút quick filter
        document.querySelectorAll('.quick-filter').forEach(button => {
            button.addEventListener('click', function () {
                // Bỏ chọn nút hiện tại và chọn nút mới
                document.querySelector('.quick-filter.active')?.classList.remove('active');
                this.classList.add('active');

                const today = new Date();
                let startDate, endDate;

                // Thiết lập ngày bắt đầu và ngày kết thúc dựa trên bộ lọc được chọn
                switch (this.textContent.trim()) {
                    case 'Hôm nay': {
                        startDate = today;
                        endDate = today;
                        break;
                    }
                    case '7 ngày qua': {
                        startDate = new Date(today);
                        startDate.setDate(today.getDate() - 6);
                        endDate = today;
                        break;
                    }
                    case '30 ngày qua': {
                        startDate = new Date(today);
                        startDate.setDate(today.getDate() - 29);
                        endDate = today;
                        break;
                    }
                    case 'Tháng này': {
                        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
                        endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);
                        break;
                    }
                    case 'Quý này': {
                        const quarter = Math.floor(today.getMonth() / 3);
                        startDate = new Date(today.getFullYear(), quarter * 3, 1);
                        endDate = new Date(today.getFullYear(), (quarter * 3) + 3, 0);
                        break;
                    }
                }

                // Cập nhật date picker
                if (startDate && endDate) {
                    const formatDate = date => {
                        const day = date.getDate().toString().padStart(2, '0');
                        const month = (date.getMonth() + 1).toString().padStart(2, '0');
                        const year = date.getFullYear();
                        return `${day}/${month}/${year}`;
                    };

                    startDatePicker.setDate(startDate);
                    endDatePicker.setDate(endDate);

                    // Cập nhật bộ lọc
                    filterWithdrawals();
                }
            });
        });

        // Xử lý nút Áp dụng
        document.querySelector('.date-inputs .primary-btn').addEventListener('click', function () {
            filterWithdrawals();
            showToast('info', 'Bộ lọc đã được cập nhật');
        });

        // Xử lý nút Đặt lại
        document.querySelector('.date-inputs .secondary-btn').addEventListener('click', function () {
            startDatePicker.clear();
            endDatePicker.clear();
            document.querySelector('.quick-filter.active')?.classList.remove('active');
            document.querySelector('.quick-filter:first-child')?.classList.add('active');
            filterWithdrawals();
            showToast('info', 'Đã đặt lại bộ lọc ngày tháng');
        });
    }

    // Gọi hàm khởi tạo date picker sau khi trang tải xong
    document.addEventListener('DOMContentLoaded', function () {
        initDatePickers();
    });

    // Hàm xử lý lọc dữ liệu yêu cầu rút tiền
    function filterWithdrawals() {
        // Lấy giá trị từ các bộ lọc
        const searchInput = document.getElementById('searchWithdrawals')?.value.toLowerCase();
        const statusSelect = document.getElementById('statusFilter')?.value;
        const minAmount = document.getElementById('minAmount')?.value;
        const maxAmount = document.getElementById('maxAmount')?.value;
        const startDate = document.getElementById('startDate')?.value;
        const endDate = document.getElementById('endDate')?.value;
        const currentPage = document.querySelector('.pagination-btn.active')?.dataset.page || 1;

        // Hiển thị trạng thái loading
        const tableBody = document.querySelector('.withdrawal-table tbody');
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center"><div class="loading-spinner"></div><p>Đang tải dữ liệu...</p></td></tr>';

        // Giả lập gọi API - thực tế sẽ gọi API với các tham số lọc
        setTimeout(() => {
            // Giả định dữ liệu được trả về từ API
            // Trong thực tế, bạn sẽ gọi API và xử lý dữ liệu trả về
            renderWithdrawalTable();

            // Hiển thị thông báo
            showToast('success', 'Dữ liệu đã được cập nhật');
        }, 800);
    }

    // Hàm render bảng dữ liệu yêu cầu rút tiền
    function renderWithdrawalTable() {
        // Trong dự án thực tế, dữ liệu này sẽ đến từ API
        const withdrawalData = [
            {
                id: 'WD-2025060301',
                user: {
                    name: 'Nguyễn Văn A',
                    avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
                    email: 'nguyenvana@example.com'
                },
                amount: 500000,
                status: 'Chờ xử lý',
                bankInfo: 'Vietcombank - **** **** **** 1234',
                createdAt: '09/06/2025 09:15:00',
                updatedAt: '09/06/2025 09:15:00'
            },
            {
                id: 'WD-2025060302',
                user: {
                    name: 'Trần Thị B',
                    avatar: 'https://randomuser.me/api/portraits/women/44.jpg',
                    email: 'tranthib@example.com'
                },
                amount: 1500000,
                status: 'Đã từ chối',
                bankInfo: 'BIDV - **** **** **** 5678',
                createdAt: '09/06/2025 10:20:00',
                updatedAt: '09/06/2025 11:05:00'
            },
            {
                id: 'WD-2025060303',
                user: {
                    name: 'Lê Văn C',
                    avatar: 'https://randomuser.me/api/portraits/men/22.jpg',
                    email: 'levanc@example.com'
                },
                amount: 1000000,
                status: 'Đang xử lý',
                bankInfo: 'Techcombank - **** **** **** 9012',
                createdAt: '09/06/2025 13:45:00',
                updatedAt: '09/06/2025 14:30:00'
            },
            {
                id: 'WD-2025060304',
                user: {
                    name: 'Phạm Thị D',
                    avatar: 'https://randomuser.me/api/portraits/women/63.jpg',
                    email: 'phamthid@example.com'
                },
                amount: 3000000,
                status: 'Đã hoàn thành',
                bankInfo: 'ACB - **** **** **** 3456',
                createdAt: '09/06/2025 15:10:00',
                updatedAt: '10/06/2025 09:20:00'
            }
        ];

        const tableBody = document.querySelector('.withdrawal-table tbody');
        tableBody.innerHTML = '';

        withdrawalData.forEach(item => {
            // Tạo class tương ứng với trạng thái
            let statusClass = '';
            switch (item.status) {
                case 'Chờ xử lý': statusClass = 'pending'; break;
                case 'Đang xử lý': statusClass = 'processing'; break;
                case 'Đã hoàn thành': statusClass = 'completed'; break;
                case 'Đã từ chối': statusClass = 'rejected'; break;
                default: statusClass = 'pending';
            }

            // Xác định các nút hành động dựa trên trạng thái
            let actionButtons = '';
            switch (item.status) {
                case 'Chờ xử lý':
                    actionButtons = `
                        <button class="action-btn process" data-id="${item.id}"><i class="fas fa-money-check-alt"></i> Xử lý</button>
                        <button class="action-btn reject" data-id="${item.id}"><i class="fas fa-times-circle"></i> Từ chối</button>
                    `;
                    break;
                case 'Đang xử lý':
                    actionButtons = `
                        <button class="action-btn approve" data-id="${item.id}"><i class="fas fa-check-circle"></i> Hoàn thành</button>
                        <button class="action-btn reject" data-id="${item.id}"><i class="fas fa-times-circle"></i> Từ chối</button>
                    `;
                    break;
                case 'Đã hoàn thành':
                    actionButtons = ''; // Không có hành động
                    break;
                case 'Đã từ chối':
                    actionButtons = ''; // Không có hành động
                    break;
                default:
                    actionButtons = `<button class="action-btn process" data-id="${item.id}"><i class="fas fa-money-check-alt"></i> Xử lý</button>`;
            }

            // Thêm nút xem chi tiết cho tất cả các trạng thái
            actionButtons += `<button class="action-btn view-withdrawal" data-id="${item.id}"><i class="fas fa-eye"></i> Chi tiết</button>`;

            // Tạo hàng dữ liệu
            const row = `
                <tr>
                    <td>${item.id}</td>
                    <td>
                        <div class="user-info">
                            <img src="${item.user.avatar}" alt="User" class="user-avatar">
                            <div>
                                <div class="user-name">${item.user.name}</div>
                                <div class="user-email">${item.user.email}</div>
                            </div>
                        </div>
                    </td>
                    <td><strong>${new Intl.NumberFormat('vi-VN').format(item.amount)} VNĐ</strong></td>
                    <td>${item.bankInfo}</td>
                    <td><span class="status-badge ${statusClass}">${item.status}</span></td>
                    <td>${item.createdAt}</td>
                    <td>
                        <div class="action-buttons">
                            ${actionButtons}
                        </div>
                    </td>
                </tr>
            `;

            tableBody.innerHTML += row;
        });

        // Gắn lại sự kiện cho các nút hành động
        attachActionButtonEvents();
    }

    // Hàm gắn sự kiện cho các nút hành động
    function attachActionButtonEvents() {
        // Nút xem chi tiết
        document.querySelectorAll('.action-btn.view-withdrawal').forEach(button => {
            button.addEventListener('click', function () {
                const withdrawalId = this.dataset.id;
                loadWithdrawalDetail(withdrawalId);
            });
        });

        // Nút xử lý
        document.querySelectorAll('.action-btn.process').forEach(button => {
            button.addEventListener('click', function () {
                const withdrawalId = this.dataset.id;
                openModal('processWithdrawalModal');
            });
        });

        // Nút từ chối
        document.querySelectorAll('.action-btn.reject').forEach(button => {
            button.addEventListener('click', function () {
                const withdrawalId = this.dataset.id;
                openModal('rejectWithdrawalModal');
            });
        });

        // Nút hoàn thành
        document.querySelectorAll('.action-btn.approve').forEach(button => {
            button.addEventListener('click', function () {
                const withdrawalId = this.dataset.id;
                openModal('completeWithdrawalModal');
            });
        });
    }

    // Xử lý các bộ lọc tìm kiếm
    // Tìm kiếm theo ID hoặc người dùng
    document.getElementById('searchWithdrawals')?.addEventListener('input', debounce(() => {
        filterWithdrawals();
    }, 500));

    // Lọc theo trạng thái
    document.getElementById('statusFilter')?.addEventListener('change', function () {
        filterWithdrawals();
    });

    // Lọc theo số tiền
    const minAmountInput = document.getElementById('minAmount');
    const maxAmountInput = document.getElementById('maxAmount');

    minAmountInput?.addEventListener('input', debounce(() => {
        filterWithdrawals();
    }, 500));

    maxAmountInput?.addEventListener('input', debounce(() => {
        filterWithdrawals();
    }, 500));

    // Hàm debounce để tránh gọi quá nhiều lần
    function debounce(func, delay) {
        let timeout;
        return function () {
            const context = this;
            const args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(context, args), delay);
        };
    }

    // Khởi tạo bảng ban đầu
    document.addEventListener('DOMContentLoaded', function () {
        // Khởi tạo bộ lọc
        filterWithdrawals();
    });

    // Xử lý nút prev/next
    document.querySelector('.next-btn')?.addEventListener('click', function () {
        const activeBtn = document.querySelector('.pagination-btn.active:not(.prev-btn):not(.next-btn)');
        if (!activeBtn) return;

        const nextBtn = activeBtn.nextElementSibling;

        if (nextBtn && !nextBtn.classList.contains('next-btn')) {
            // Click vào nút tiếp theo
            nextBtn.click();

            // Cập nhật trạng thái nút prev/next
            document.querySelector('.prev-btn').disabled = false;
            if (!nextBtn.nextElementSibling || nextBtn.nextElementSibling.classList.contains('next-btn')) {
                this.disabled = true;
            }
        }
    });

    document.querySelector('.prev-btn')?.addEventListener('click', function () {
        const activeBtn = document.querySelector('.pagination-btn.active:not(.prev-btn):not(.next-btn)');
        if (!activeBtn) return;

        const prevBtn = activeBtn.previousElementSibling;

        if (prevBtn && !prevBtn.classList.contains('prev-btn')) {
            // Click vào nút trước đó
            prevBtn.click();

            // Cập nhật trạng thái nút prev/next
            document.querySelector('.next-btn').disabled = false;
            if (!prevBtn.previousElementSibling || prevBtn.previousElementSibling.classList.contains('prev-btn')) {
                this.disabled = true;
            }
        }
    });
});
