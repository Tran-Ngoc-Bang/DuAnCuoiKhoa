document.querySelectorAll('.tab-button').forEach(button => {
    button.addEventListener('click', function () {
        const status = this.getAttribute('data-status');
        document.getElementById('tabStatusInput').value = status;
        document.getElementById('tabFilterForm').submit();
    });
});




document.addEventListener('DOMContentLoaded', function () {
    // Admin dropdown toggle
    const adminProfile = document.querySelector('.admin-profile');
    if (adminProfile) {
        adminProfile.addEventListener('click', function () {
            this.classList.toggle('active');
        });
    }

    // Select all checkboxes
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.checkbox-wrapper input[type="checkbox"]:not(#selectAll)');
    const selectedCount = document.querySelector('.selected-count');

    if (selectAll) {
        selectAll.addEventListener('change', function () {
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            updateSelectedCount();
        });

        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function () {
                const allChecked = [...checkboxes].every(c => c.checked);
                const someChecked = [...checkboxes].some(c => c.checked);

                selectAll.checked = allChecked;
                selectAll.indeterminate = someChecked && !allChecked;

                updateSelectedCount();
            });
        });
    }

    function updateSelectedCount() {
        const count = [...checkboxes].filter(c => c.checked).length;
        if (selectedCount) {
            selectedCount.textContent = `${count} báo cáo được chọn`;
        }

        // Show/hide bulk actions
        const bulkActions = document.querySelector('.bulk-actions');
        if (bulkActions) {
            if (count > 0) {
                bulkActions.classList.add('active');
            } else {
                bulkActions.classList.remove('active');
            }
        }
    }

    // Apply bulk action
    const bulkActionSelect = document.getElementById('bulkActionSelect');
    const applyActionBtn = document.querySelector('.apply-action-btn');

    if (applyActionBtn && bulkActionSelect) {
        applyActionBtn.addEventListener('click', function () {
            const action = bulkActionSelect.value;
            if (!action) return;

            const selectedItems = [...checkboxes].filter(c => c.checked);
            if (selectedItems.length === 0) return;

            // Confirm action
            const confirmMessage = `Bạn có chắc chắn muốn ${getActionText(action)} ${selectedItems.length} báo cáo đã chọn?`;
            if (confirm(confirmMessage)) {
                // Perform action (simulation)
                alert(`Đã ${getActionText(action)} ${selectedItems.length} báo cáo thành công!`);

                // Reset selection
                checkboxes.forEach(checkbox => {
                    checkbox.checked = false;
                });
                selectAll.checked = false;
                selectAll.indeterminate = false;
                bulkActionSelect.value = '';
                updateSelectedCount();
            }
        });
    }

    function getActionText(action) {
        switch (action) {
            case 'pending': return 'đánh dấu đang xử lý';
            case 'resolve': return 'đánh dấu đã giải quyết';
            case 'reject': return 'từ chối';
            default: return 'xử lý';
        }
    }

    // Tab switching
    const tabButtons = document.querySelectorAll('.tab-button');

    tabButtons.forEach(button => {
        button.addEventListener('click', function () {
            tabButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            const tab = this.getAttribute('data-tab');
            // In a real application, we would filter the table based on the selected tab
            console.log(`Switch to tab: ${tab}`);
        });
    });

    // Report Detail Modal
    const viewButtons = document.querySelectorAll('.view-report');
    const reportDetailModal = document.getElementById('reportDetailModal');
    const modalOverlay = document.querySelector('.modal-overlay');
    const modalCloseBtn = document.querySelector('.modal-close');
    const cancelBtn = document.querySelector('.cancel-btn');

    function openModal() {
        if (reportDetailModal) {
            reportDetailModal.classList.add('open');
            document.body.style.overflow = 'hidden';
        }
    }

    function closeModal() {
        if (reportDetailModal) {
            reportDetailModal.classList.remove('open');
            document.body.style.overflow = '';
        }
    }

    // Đã được xử lý ở phần dưới

    if (modalOverlay) {
        modalOverlay.addEventListener('click', closeModal);
    }

    if (modalCloseBtn) {
        modalCloseBtn.addEventListener('click', closeModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    // Process Report
    const processButtons = document.querySelectorAll('.process-report');

    processButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const reportId = this.getAttribute('data-id');
            // In a real application, we would update report status to "processing"
            console.log(`Process report: ${reportId}`);
            alert(`Báo cáo #${reportId} đã được chuyển sang trạng thái đang xử lý.`);
        });
    });

    // Resolve Report
    const resolveButtons = document.querySelectorAll('.resolve-report');

    resolveButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const reportId = this.getAttribute('data-id');
            // In a real application, we would update report status to "resolved"
            console.log(`Resolve report: ${reportId}`);
            alert(`Báo cáo #${reportId} đã được đánh dấu là đã giải quyết.`);
        });
    });

    // Reject Report
    const rejectButtons = document.querySelectorAll('.reject-report');

    rejectButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const reportId = this.getAttribute('data-id');
            // In a real application, we would update report status to "rejected"
            console.log(`Reject report: ${reportId}`);
            alert(`Báo cáo #${reportId} đã bị từ chối.`);
        });
    });

    // Reopen Report
    const reopenButtons = document.querySelectorAll('.reopen-report');

    reopenButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const reportId = this.getAttribute('data-id');
            // In a real application, we would update report status to "new"
            console.log(`Reopen report: ${reportId}`);
            alert(`Báo cáo #${reportId} đã được mở lại.`);
        });
    });

    // Pagination
    const pageButtons = document.querySelectorAll('.page-btn');
    const prevButton = document.querySelector('.prev-btn');
    const nextButton = document.querySelector('.next-btn');

    pageButtons.forEach(button => {
        button.addEventListener('click', function () {
            pageButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            // Update prev/next button states
            if (this.textContent === '1') {
                prevButton.disabled = true;
            } else {
                prevButton.disabled = false;
            }

            if (this.textContent === '14') {
                nextButton.disabled = true;
            } else {
                nextButton.disabled = false;
            }
        });
    });





    const selectAllCheckbox = document.getElementById("selectAll");

    const reportCheckboxes = document.querySelectorAll(".report-checkbox");




    // Khi nhấn vào checkbox "Chọn tất cả"
    selectAllCheckbox.addEventListener("change", function () {
        const isChecked = this.checked;

        // Đặt trạng thái cho tất cả các checkbox
        reportCheckboxes.forEach(checkbox => {
            checkbox.checked = isChecked;
        });

        // Gửi danh sách ID được chọn đến console
        const selectedIds = getSelectedIds();
        console.log("Selected IDs:", selectedIds);
    });

    // Khi nhấn vào từng checkbox riêng lẻ
    reportCheckboxes.forEach(checkbox => {
        checkbox.addEventListener("change", function () {
            // Nếu một checkbox bị bỏ chọn, bỏ chọn "Chọn tất cả"
            if (!this.checked) {
                selectAllCheckbox.checked = false;
            }

            // Nếu tất cả checkbox được chọn, chọn "Chọn tất cả"
            if (Array.from(reportCheckboxes).every(cb => cb.checked)) {
                selectAllCheckbox.checked = true;
            }

            // Gửi danh sách ID được chọn đến console
            const selectedIds = getSelectedIds();
            console.log("Selected IDs:", selectedIds);
        });
    });

    // Hàm lấy danh sách ID được chọn
    function getSelectedIds() {
        const selectedIds = Array.from(reportCheckboxes)
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.getAttribute("data-id"));
        return selectedIds;
    }










    function showReportDetail(report) {
        const modal = document.getElementById("reportDetailModal");

        // Cập nhật nội dung modal
        modal.querySelector(".modal-header h3").textContent = "Chi tiết báo cáo #RPT" + report.reportId;
        modal.querySelector(".status-badge").textContent = mapStatus(report.status);
        modal.querySelector(".date-value").textContent = formatDate(report.reportDate);
        modal.querySelector(".violation-type").textContent = mapViolation(report.violationType);
        modal.querySelector(".user-name").textContent = report.reporterName;
        modal.querySelector(".user-email").textContent = report.reporterEmail;
        modal.querySelector(".description p").textContent = report.reason;
        modal.querySelector(".document-title").textContent = report.documentTitle;
        modal.querySelector(".document-meta .meta-item:nth-child(1)").innerHTML = `<i class="fas fa-user"></i> ${report.documentUploader}`;
        modal.querySelector(".document-meta .meta-item:nth-child(2)").innerHTML = `<i class="fas fa-calendar"></i> Đăng ngày: ${formatDate(report.documentUploadDate)}`;
        modal.querySelector(".document-meta .meta-item:nth-child(3)").innerHTML = `<i class="fas fa-download"></i> ${report.documentDownloads} lượt tải`;

        // Cập nhật lịch sử xử lý
        updateProcessingHistory(report);

        openModal();
    }

    function updateProcessingHistory(report) {
        const timelineContainer = document.querySelector(".history-timeline");
        timelineContainer.innerHTML = ""; // Xóa nội dung cũ

        // 1. Timeline item cho việc tạo báo cáo
        const createItem = document.createElement("div");
        createItem.className = "timeline-item";
        createItem.innerHTML = `
            <div class="timeline-icon">
                <i class="fas fa-flag"></i>
            </div>
            <div class="timeline-content">
                <div class="timeline-header">
                    <span class="timeline-title">Báo cáo được tạo</span>
                    <span class="timeline-date">${formatDate(report.reportDate)}</span>
                </div>
                <div class="timeline-body">
                    ${report.reporterName} đã tạo báo cáo ${mapViolation(report.violationType).toLowerCase()} cho tài liệu "${report.documentTitle}".
                </div>
            </div>
        `;
        timelineContainer.appendChild(createItem);

        // 2. Timeline item cho việc xử lý (nếu có)
        if (report.reviewedAt && report.reviewerName) {
            const reviewItem = document.createElement("div");
            reviewItem.className = "timeline-item";
            
            let actionText = "";
            let iconClass = "";
            
            switch(report.status) {
                case 'pending':
                    actionText = "đang xử lý";
                    iconClass = "fas fa-hourglass-half";
                    break;
                case 'resolved':
                    actionText = "đã giải quyết";
                    iconClass = "fas fa-check-circle";
                    break;
                case 'rejected':
                    actionText = "đã từ chối";
                    iconClass = "fas fa-times-circle";
                    break;
                default:
                    actionText = "đã xem xét";
                    iconClass = "fas fa-eye";
            }
            
            reviewItem.innerHTML = `
                <div class="timeline-icon">
                    <i class="${iconClass}"></i>
                </div>
                <div class="timeline-content">
                    <div class="timeline-header">
                        <span class="timeline-title">Báo cáo ${actionText}</span>
                        <span class="timeline-date">${formatDate(report.reviewedAt)}</span>
                    </div>
                    <div class="timeline-body">
                        ${report.reviewerName} đã đánh dấu báo cáo này là ${actionText}.
                    </div>
                </div>
            `;
            timelineContainer.appendChild(reviewItem);
        }
    }

    function mapStatus(status) {
        switch (status) {
            case 'new': return 'Mới';
            case 'pending': return 'Đang xử lý';
            case 'resolved': return 'Đã giải quyết';
            case 'rejected': return 'Đã từ chối';
            default: return 'Không rõ';
        }
    }

    function mapViolation(type) {
        switch (type) {
            case 'copyright': return 'Vi phạm bản quyền';
            case 'inappropriate': return 'Nội dung không phù hợp';
            case 'spam': return 'Spam / Quảng cáo';
            case 'fake': return 'Tài liệu giả mạo';
            case 'other': return 'Vi phạm khác';
            default: return 'Không xác định';
        }
    }

    function formatDate(dateStr) {
        const date = new Date(dateStr);
        return date.toLocaleDateString('vi-VN') + " - " + date.toLocaleTimeString('vi-VN');
    }

    // Xử lý form cập nhật báo cáo
    const saveBtn = document.querySelector('.save-btn');
    let currentReportId = null;

    // Lưu reportId khi mở modal
    viewButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            currentReportId = this.getAttribute("data-id");
            fetch(`/admin/reports/${currentReportId}`)
                .then(response => response.json())
                .then(data => {
                    showReportDetail(data);
                    // Cập nhật form với dữ liệu hiện tại
                    updateProcessForm(data);
                })
                .catch(error => {
                    console.error("Lỗi khi lấy chi tiết báo cáo:", error);
                });
        });
    });

    function updateProcessForm(report) {
        // Cập nhật trạng thái hiện tại trong select
        const statusSelect = document.getElementById('reportStatus');
        if (statusSelect) {
            statusSelect.value = report.status;
        }
    }

    // Xử lý khi nhấn nút "Lưu & Cập nhật"
    if (saveBtn) {
        saveBtn.addEventListener('click', function() {
            if (!currentReportId) {
                alert('Không tìm thấy ID báo cáo');
                return;
            }

            // Lấy dữ liệu từ form
            const status = document.getElementById('reportStatus').value;
            const documentAction = document.getElementById('documentAction').value;
            const adminNote = document.getElementById('adminNote').value;
            const responseToReporter = document.getElementById('responseToReporter').value;

            // Tạo FormData
            const formData = new FormData();
            formData.append('status', status);
            formData.append('documentAction', documentAction);
            formData.append('adminNote', adminNote);
            formData.append('responseToReporter', responseToReporter);

            // Gửi request
            fetch(`/admin/reports/${currentReportId}/process`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Báo cáo đã được xử lý thành công!');
                    closeModal();
                    // Reload trang để cập nhật danh sách
                    window.location.reload();
                } else {
                    alert('Có lỗi xảy ra: ' + (data.message || 'Không xác định'));
                }
            })
            .catch(error => {
                console.error('Lỗi khi xử lý báo cáo:', error);
                alert('Có lỗi xảy ra khi xử lý báo cáo');
            });
        });
    }


});