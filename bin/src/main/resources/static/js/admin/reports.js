
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
            case 'process': return 'đánh dấu đang xử lý';
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

    viewButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const reportId = this.getAttribute('data-id');
            // In a real application, we would fetch report details based on the ID
            console.log(`View report: ${reportId}`);
            openModal();
        });
    });

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
});