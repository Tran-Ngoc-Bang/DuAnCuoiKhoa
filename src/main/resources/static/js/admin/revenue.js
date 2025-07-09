
// Report data objects - defined outside any function for global access
const revenueTimeReport = {
    period: {
        start: "2025-01-01T00:00:00Z",
        end: "2025-12-31T23:59:59Z",
        granularity: "MONTH" // DAY, WEEK, MONTH, QUARTER, YEAR
    },
    data: [
        {
            label: "T1",
            totalRevenue: 1500000,
            platformFee: 150000,
            creatorEarnings: 1350000,
            transactions: 45,
            refunds: 3,
            refundAmount: 120000,
            netRevenue: 1380000,
            byPaymentMethod: {
                card: 900000,
                eWallet: 450000,
                bankTransfer: 150000
            },
            byType: {
                documentPurchase: 1050000,
                subscription: 300000,
                donation: 150000,
                featured: 0
            }
        }
        // Thêm dữ liệu tháng khác ở đây nếu cần
    ],
    totals: {
        totalRevenue: 28500000,
        platformFee: 2850000,
        creatorEarnings: 25650000,
        transactions: 243,
        refunds: 12,
        refundAmount: 750000,
        netRevenue: 27750000
    },
    previousPeriod: {
        totalRevenue: 25350000,
        change: 12.5, // Phần trăm thay đổi
        trend: "up" // up, down, neutral
    }
};

const revenueByCategoryReport = {
    period: {
        start: "2025-01-01T00:00:00Z",
        end: "2025-12-31T23:59:59Z"
    },
    data: [
        {
            category: {
                id: "CAT001",
                name: "Công nghệ thông tin",
                icon: "laptop-code"
            },
            totalRevenue: 12500000,
            percentOfTotal: 43.86,
            transactions: 112,
            averagePrice: 111607,
            topDocuments: [
                {
                    id: "DOC001",
                    title: "Giáo trình lập trình Java nâng cao",
                    revenue: 1500000,
                    sales: 10
                }
                // Các tài liệu khác
            ]
        }
        // Dữ liệu cho các danh mục khác
    ],
    totals: {
        totalRevenue: 28500000,
        transactions: 243
    }
};

const revenueByCreatorReport = {
    period: {
        start: "2025-01-01T00:00:00Z",
        end: "2025-12-31T23:59:59Z"
    },
    data: [
        {
            creator: {
                id: "USR002",
                name: "Trần Thị B",
                avatar: "https://randomuser.me/api/portraits/women/65.jpg"
            },
            totalRevenue: 3500000,
            platformFee: 350000,
            creatorEarnings: 3150000,
            percentOfTotal: 12.28,
            transactions: 35,
            documentsCount: 12,
            topDocuments: [
                {
                    id: "DOC001",
                    title: "Giáo trình lập trình Java nâng cao",
                    revenue: 1500000,
                    sales: 10
                }
                // Các tài liệu khác
            ]
        }
        // Dữ liệu cho các người sáng tạo khác
    ],
    totals: {
        totalRevenue: 28500000,
        transactions: 243,
        creators: 45
    }
};

// Chart data
const revenueTimeData = {
    labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
    datasets: [
        {
            label: 'Tổng doanh thu',
            data: [1500000, 1800000, 2200000, 1900000, 2500000, 2800000, 2600000, 3100000, 2900000, 3200000, 3500000, 2850000],
            borderColor: '#4361ee',
            backgroundColor: 'rgba(67, 97, 238, 0.1)',
            fill: true,
            tension: 0.4
        },
        {
            label: 'Phí nền tảng',
            data: [150000, 180000, 220000, 190000, 250000, 280000, 260000, 310000, 290000, 320000, 350000, 285000],
            borderColor: '#3f37c9',
            borderDash: [5, 5],
            backgroundColor: 'transparent',
            tension: 0.4
        }
    ]
};

const revenuePieData = {
    labels: ['Mua tài liệu', 'Đăng ký gói', 'Quyên góp', 'Phí đăng tải'],
    datasets: [{
        data: [18950000, 7125000, 1425000, 1000000],
        backgroundColor: [
            '#4361ee',
            '#3f37c9',
            '#4895ef',
            '#4cc9f0'
        ],
        borderWidth: 0
    }]
};

document.addEventListener('DOMContentLoaded', function () {
    // Initialize date pickers
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
    flatpickr("#promotionStart", {
        dateFormat: "d/m/Y",
        locale: "vi"
    });
    flatpickr("#promotionEnd", {
        dateFormat: "d/m/Y",
        locale: "vi"
    });


    // Initialize the select-all checkbox functionality
    const selectAllCheckbox = document.querySelector('.select-all-checkbox');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            const checkboxes = document.querySelectorAll('.row-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });

            // Show/hide bulk actions based on selection
            const bulkActions = document.querySelector('.bulk-actions');
            if (bulkActions) {
                if (this.checked) {
                    bulkActions.classList.add('active');
                } else {
                    // Check if any individual checkbox is still checked
                    const anyChecked = [...checkboxes].some(cb => cb.checked);
                    if (!anyChecked) {
                        bulkActions.classList.remove('active');
                    }
                }
            }
        });
    }

    // Individual row checkboxes
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    if (rowCheckboxes.length > 0) {
        rowCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function () {
                const bulkActions = document.querySelector('.bulk-actions');
                if (bulkActions) {
                    // Check if any checkbox is checked
                    const anyChecked = [...document.querySelectorAll('.row-checkbox')].some(cb => cb.checked);
                    if (anyChecked) {
                        bulkActions.classList.add('active');
                    } else {
                        bulkActions.classList.remove('active');
                    }
                }

                // Update select all checkbox state
                const selectAllCheckbox = document.querySelector('.select-all-checkbox');
                const allChecked = [...document.querySelectorAll('.row-checkbox')].every(cb => cb.checked);
                if (selectAllCheckbox) {
                    selectAllCheckbox.checked = allChecked;
                }
            });
        });
    }

    // Bulk action buttons
    const bulkActionButtons = document.querySelectorAll('.bulk-action-btn');
    if (bulkActionButtons.length > 0) {
        bulkActionButtons.forEach(button => {
            button.addEventListener('click', function () {
                const action = this.dataset.action;
                const checkedRows = document.querySelectorAll('.row-checkbox:checked');

                if (checkedRows.length === 0) {
                    showToast('error', 'Vui lòng chọn ít nhất một gói xu');
                    return;
                }

                // Get IDs of checked rows
                const selectedIds = [...checkedRows].map(checkbox => {
                    return checkbox.closest('tr').querySelector('td:nth-child(2)').textContent;
                });

                if (action === 'activate') {
                    // Activate selected packages
                    console.log('Activating packages:', selectedIds);
                    checkedRows.forEach(checkbox => {
                        const statusBadge = checkbox.closest('tr').querySelector('.status-badge');
                        statusBadge.className = 'status-badge active';
                        statusBadge.textContent = 'Hoạt động';
                    });
                    showToast('success', `Đã kích hoạt ${selectedIds.length} gói xu`);
                } else if (action === 'deactivate') {
                    // Deactivate selected packages
                    console.log('Deactivating packages:', selectedIds);
                    checkedRows.forEach(checkbox => {
                        const statusBadge = checkbox.closest('tr').querySelector('.status-badge');
                        statusBadge.className = 'status-badge inactive';
                        statusBadge.textContent = 'Không hoạt động';
                    });
                    showToast('success', `Đã vô hiệu hóa ${selectedIds.length} gói xu`);
                } else if (action === 'delete') {
                    // Delete selected packages
                    if (confirm(`Bạn có chắc chắn muốn xóa ${selectedIds.length} gói xu đã chọn?`)) {
                        console.log('Deleting packages:', selectedIds);
                        checkedRows.forEach(checkbox => {
                            checkbox.closest('tr').remove();
                        });
                        showToast('success', `Đã xóa ${selectedIds.length} gói xu`);

                        // Reset select all checkbox
                        const selectAllCheckbox = document.querySelector('.select-all-checkbox');
                        if (selectAllCheckbox) {
                            selectAllCheckbox.checked = false;
                        }

                        // Hide bulk actions
                        const bulkActions = document.querySelector('.bulk-actions');
                        if (bulkActions) {
                            bulkActions.classList.remove('active');
                        }
                    }
                }
            });
        });
    }

    // Initialize charts
    const revenueTimeChart = new Chart(document.getElementById('revenueTimeChart'), {
        type: 'line',
        data: revenueTimeData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: function (context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.parsed.y !== null) {
                                label += new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(context.parsed.y);
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function (value, index, values) {
                            if (value >= 1000000) {
                                return value / 1000000 + 'tr';
                            }
                            return value;
                        }
                    }
                }
            }
        }
    });

    const revenuePieChart = new Chart(document.getElementById('revenuePieChart'), {
        type: 'doughnut',
        data: revenuePieData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            let label = context.label || '';
                            if (label) {
                                label += ': ';
                            }
                            label += context.formattedValue + '%';
                            return label;
                        }
                    }
                }
            },
            cutout: '70%'
        }
    });

    // Date range button handlers
    const dateButtons = document.querySelectorAll('.date-btn');
    const customDateRange = document.querySelector('.custom-date-range');

    dateButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Remove active class from all buttons
            dateButtons.forEach(btn => btn.classList.remove('active'));

            // Add active class to clicked button
            this.classList.add('active');

            // Show/hide custom date range inputs
            if (this.dataset.range === 'custom') {
                customDateRange.style.display = 'flex';
            } else {
                customDateRange.style.display = 'none';
                // Here you would filter data based on selected date range
                filterDataByDateRange(this.dataset.range);
            }
        });
    });

    // Initialize modals
    const viewDetailButtons = document.querySelectorAll('.action-btn[title="Xem chi tiết"]');
    const refundButtons = document.querySelectorAll('.action-btn[title="Hoàn tiền"]');
    const transactionDetailModal = document.getElementById('transactionDetailModal');
    const refundModal = document.getElementById('refundModal');
    const coinPackageModal = document.getElementById('coinPackageModal');
    const modalCloseButtons = document.querySelectorAll('.modal-close, .modal-overlay');
    const refundBtnInDetail = document.querySelector('.refund-btn');

    // Currency filter handling
    const currencyFilter = document.querySelector('.filter-currency');
    if (currencyFilter) {
        currencyFilter.addEventListener('change', function () {
            const selectedCurrency = this.value;
            const transactionRows = document.querySelectorAll('.transactions-table tbody tr');

            transactionRows.forEach(row => {
                const currencyCell = row.querySelector('td:nth-child(3)');
                const currencyText = currencyCell.textContent.toLowerCase();

                if (selectedCurrency === 'all' ||
                    (selectedCurrency === 'gold' && currencyText.includes('gold')) ||
                    (selectedCurrency === 'blue' && currencyText.includes('blue'))) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });

            // Update row count display if available
            const countDisplay = document.querySelector('.showing-entries span');
            if (countDisplay) {
                const visibleRows = [...transactionRows].filter(row => row.style.display !== 'none').length;
                countDisplay.textContent = `Hiển thị ${visibleRows} / ${transactionRows.length} mục`;
            }
        });
    }

    viewDetailButtons.forEach(button => {
        button.addEventListener('click', function () {
            transactionDetailModal.classList.add('active');
        });
    });

    refundButtons.forEach(button => {
        button.addEventListener('click', function () {
            refundModal.classList.add('active');
        });
    });

    if (refundBtnInDetail) {
        refundBtnInDetail.addEventListener('click', function () {
            transactionDetailModal.classList.remove('active');
            refundModal.classList.add('active');
        });
    }

    modalCloseButtons.forEach(button => {
        button.addEventListener('click', function () {
            document.querySelectorAll('.modal.active').forEach(modal => {
                modal.classList.remove('active');
            });
        });
    });

    // Xử lý nút duyệt giao dịch rút tiền
    const approveButtons = document.querySelectorAll('.action-btn[title="Duyệt"]');
    if (approveButtons.length > 0) {
        approveButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const txId = row.querySelector('td:first-child').textContent;
                const userName = row.querySelector('.user-info span').textContent;

                // Hiển thị modal xác nhận với OTP (tùy chọn)
                if (confirm(`Xác nhận duyệt yêu cầu rút tiền cho giao dịch ${txId} của ${userName}?`)) {
                    // Đổi trạng thái giao dịch thành thành công
                    const statusBadge = row.querySelector('.status-badge');
                    statusBadge.textContent = 'Thành công';
                    statusBadge.className = 'status-badge success';

                    // Ẩn nút duyệt và từ chối, hiển thị nút xem chi tiết
                    const actionButtons = row.querySelector('.action-buttons');
                    actionButtons.innerHTML = `
                        <button class="action-btn" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                        <button class="action-btn" title="Xuất hóa đơn"><i class="fas fa-file-invoice"></i></button>
                    `;

                    // Kích hoạt lại event listener cho nút xem chi tiết
                    const newDetailBtn = actionButtons.querySelector('.action-btn[title="Xem chi tiết"]');
                    newDetailBtn.addEventListener('click', function () {
                        transactionDetailModal.classList.add('active');
                    });

                    showToast('success', `Đã duyệt giao dịch ${txId} thành công`);
                }
            });
        });
    }

    // Xử lý nút từ chối giao dịch rút tiền
    const rejectButtons = document.querySelectorAll('.action-btn[title="Từ chối"]');
    if (rejectButtons.length > 0) {
        rejectButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const txId = row.querySelector('td:first-child').textContent;
                const userName = row.querySelector('.user-info span').textContent;

                // Hiển thị modal từ chối với lý do
                const reason = prompt(`Nhập lý do từ chối giao dịch ${txId} của ${userName}:`, 'Thông tin không hợp lệ');

                if (reason !== null) {
                    // Đổi trạng thái giao dịch thành từ chối
                    const statusBadge = row.querySelector('.status-badge');
                    statusBadge.textContent = 'Đã hủy';
                    statusBadge.className = 'status-badge canceled';

                    // Ẩn nút duyệt và từ chối, hiển thị nút xem chi tiết
                    const actionButtons = row.querySelector('.action-buttons');
                    actionButtons.innerHTML = `
                        <button class="action-btn" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                    `;

                    // Kích hoạt lại event listener cho nút xem chi tiết
                    const newDetailBtn = actionButtons.querySelector('.action-btn[title="Xem chi tiết"]');
                    newDetailBtn.addEventListener('click', function () {
                        transactionDetailModal.classList.add('active');
                    });

                    showToast('info', `Đã từ chối giao dịch ${txId}`);
                }
            });
        });
    }

    // Xử lý form hoàn tiền
    const refundForm = document.getElementById('refundForm');
    if (refundForm) {
        refundForm.addEventListener('submit', function (e) {
            e.preventDefault();

            // Lấy thông tin từ form
            const formData = new FormData(this);
            const refundData = {
                transactionId: formData.get('transactionId'),
                refundAmount: formData.get('refundAmount'),
                refundReason: formData.get('refundReason'),
                refundNote: formData.get('refundNote'),
                notifyCustomer: formData.get('notifyCustomer') === 'on'
            };

            console.log('Refund data:', refundData);

            // Đóng modal
            refundModal.classList.remove('active');

            // Hiển thị thông báo thành công
            showToast('success', `Đã hoàn tiền thành công cho giao dịch ${refundData.transactionId}`);

            // Cập nhật trạng thái giao dịch trong bảng
            const transactionRows = document.querySelectorAll('.transactions-table tbody tr');
            for (const row of transactionRows) {
                const txId = row.querySelector('td:first-child').textContent;
                if (txId === refundData.transactionId) {
                    const statusBadge = row.querySelector('.status-badge');
                    statusBadge.textContent = 'Đã hoàn tiền';
                    statusBadge.className = 'status-badge refunded';
                    break;
                }
            }
        });
    }

    // Coin Package Management
    const addPackageBtn = document.getElementById('addPackageBtn');
    const packageForm = document.getElementById('coinPackageForm');
    const packageModalTitle = document.querySelector('#coinPackageModal .modal-header h3');
    const deletePackageModal = document.getElementById('deletePackageModal');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    let currentDeleteRow = null; // Theo dõi hàng đang được chọn để xóa

    // Hàm gắn sự kiện nút chỉnh sửa
    function attachEditEvents() {
        const editPackageButtons = document.querySelectorAll('.action-btn[title="Chỉnh sửa"]');
        editPackageButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const packageId = row.querySelector('td:nth-child(2)').textContent;
                const packageName = row.querySelector('td:nth-child(3)').textContent;
                const goldAmount = parseInt(row.querySelector('td:nth-child(4)').textContent);
                const blueBonus = parseInt(row.querySelector('td:nth-child(5)').textContent) || 0;
                const price = parseInt(row.querySelector('td:nth-child(6)').textContent.replace(/[^\d]/g, ''));
                const discount = parseInt(row.querySelector('td:nth-child(7)').textContent) || 0;
                const statusBadge = row.querySelector('td:nth-child(8) .status-badge');
                let status = 'inactive';

                // Xác định trạng thái chính xác
                if (statusBadge.classList.contains('active')) {
                    status = 'active';
                } else if (statusBadge.classList.contains('promotion')) {
                    status = 'promotion';
                }

                // Điền dữ liệu vào form
                document.getElementById('packageId').value = packageId;
                document.getElementById('packageName').value = packageName;
                document.getElementById('goldAmount').value = goldAmount;
                document.getElementById('blueBonus').value = blueBonus;
                document.getElementById('packagePrice').value = price;
                document.getElementById('discountPercent').value = discount;
                document.getElementById('packageStatus').value = status;

                packageModalTitle.textContent = 'Chỉnh sửa gói xu';
                packageForm.dataset.mode = 'edit';
                packageForm.dataset.id = packageId;
                coinPackageModal.classList.add('active');
            });
        });
    }

    // Hàm gắn sự kiện nút xóa
    function attachDeleteEvents() {
        const deletePackageButtons = document.querySelectorAll('.action-btn[title="Xóa"]');
        deletePackageButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const packageId = row.querySelector('td:nth-child(2)').textContent;
                const packageName = row.querySelector('td:nth-child(3)').textContent;

                // Hiển thị thông tin gói xu trong modal xác nhận
                document.getElementById('deletePackageId').textContent = packageId;
                document.getElementById('deletePackageName').textContent = packageName;

                // Lưu trữ hàng đang được chọn để xóa
                currentDeleteRow = row;

                // Hiển thị modal xác nhận
                deletePackageModal.classList.add('active');
            });
        });
    }

    // Nút thêm gói xu mới
    if (addPackageBtn) {
        addPackageBtn.addEventListener('click', function () {
            // Reset form về giá trị mặc định
            packageForm.reset();
            packageModalTitle.textContent = 'Thêm gói xu mới';
            packageForm.dataset.mode = 'add';
            packageForm.dataset.id = '';
            coinPackageModal.classList.add('active');

            // Reset date pickers nếu có
            if (window.startDatePicker && window.endDatePicker) {
                window.startDatePicker.clear();
                window.endDatePicker.clear();
            }
        });
    }

    // Xử lý nút xác nhận xóa
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function () {
            if (currentDeleteRow) {
                // Ở đây sẽ gọi API xóa gói xu
                // Nhưng hiện tại chỉ xóa hàng khỏi bảng
                currentDeleteRow.remove();
                showToast('success', 'Đã xóa gói xu thành công');

                // Đóng modal
                deletePackageModal.classList.remove('active');
                currentDeleteRow = null;
            }
        });
    }

    // Gắn sự kiện cho các nút hiện tại
    attachEditEvents();
    attachDeleteEvents();


    // Handle package form submission
    if (packageForm) {
        packageForm.addEventListener('submit', function (e) {
            e.preventDefault();

            // Validate form
            if (!validatePackageForm()) {
                return;
            }

            // Get form data
            const formData = new FormData(this);
            const packageData = {
                id: formData.get('packageId'),
                name: formData.get('packageName'),
                goldAmount: parseInt(formData.get('goldAmount')),
                blueBonus: parseInt(formData.get('blueBonus')) || 0,
                price: parseInt(formData.get('packagePrice')),
                discount: parseInt(formData.get('discountPercent')) || 0,
                status: formData.get('packageStatus'),
                description: formData.get('packageDescription'),
                promotionStart: formData.get('promotionStart'),
                promotionEnd: formData.get('promotionEnd')
            };

            // Here we would make an API call to save the package
            console.log('Saving package:', packageData);

            // Close modal and show success message
            coinPackageModal.classList.remove('active');
            const isNewPackage = this.dataset.mode === 'add';
            showToast('success', isNewPackage ? 'Thêm gói xu mới thành công' : 'Cập nhật gói xu thành công');

            if (isNewPackage) {
                // For demo purposes, add a new row to the table
                addPackageRow(packageData);
            } else {
                // Update existing row
                updatePackageRow(packageData);
            }
        });
    }

    // Validate package form
    function validatePackageForm() {
        // Simple validation example
        const packageId = document.getElementById('packageId').value;
        const packageName = document.getElementById('packageName').value;
        const goldAmount = document.getElementById('goldAmount').value;
        const price = document.getElementById('packagePrice').value;

        if (!packageId || !packageName || !goldAmount || !price) {
            showToast('error', 'Vui lòng điền đầy đủ thông tin bắt buộc');
            return false;
        }

        if (parseInt(goldAmount) <= 0 || parseInt(price) <= 0) {
            showToast('error', 'Số lượng xu và giá phải lớn hơn 0');
            return false;
        }

        return true;
    }

    // Add a new package row to the table
    function addPackageRow(packageData) {
        // In a real implementation, we would regenerate this from server data
        // This is demo code to show functionality
        const tbody = document.querySelector('.packages-table tbody');
        const newRow = document.createElement('tr');

        const formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
        const formattedPrice = formatter.format(packageData.price);

        newRow.innerHTML = `
            <td><input type="checkbox" class="row-checkbox"></td>
            <td>${packageData.id}</td>
            <td>${packageData.name}</td>
            <td>${packageData.goldAmount}</td>
            <td>${packageData.blueBonus}</td>
            <td>${formattedPrice}</td>
            <td>${packageData.discount}%</td>
            <td><span class="status-badge ${packageData.status}">${packageData.status === 'active' ? 'Hoạt động' : 'Không hoạt động'}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn" title="Chỉnh sửa"><i class="fas fa-edit"></i></button>
                    <button class="action-btn" title="Xóa"><i class="fas fa-trash-alt"></i></button>
                </div>
            </td>
        `;

        tbody.prepend(newRow);

        // Add event listeners to the new buttons
        const editBtn = newRow.querySelector('.action-btn[title="Chỉnh sửa"]');
        const deleteBtn = newRow.querySelector('.action-btn[title="Xóa"]');

        editBtn.addEventListener('click', function () {
            // Same edit logic as above, but for the new row
            const row = this.closest('tr');
            const packageId = row.querySelector('td:nth-child(2)').textContent;
            // Fill the form and show modal (similar to the edit button code above)
        });

        deleteBtn.addEventListener('click', function () {
            if (confirm('Bạn có chắc chắn muốn xóa gói xu này?')) {
                this.closest('tr').remove();
                showToast('success', 'Đã xóa gói xu thành công');
            }
        });
    }

    // Update existing package row
    function updatePackageRow(packageData) {
        // In a real implementation, we would fetch updated data from the server
        // This is demo code to show functionality
        const rows = document.querySelectorAll('.packages-table tbody tr');
        const formatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

        for (const row of rows) {
            const rowId = row.querySelector('td:nth-child(2)').textContent;
            if (rowId === packageData.id) {
                row.querySelector('td:nth-child(3)').textContent = packageData.name;
                row.querySelector('td:nth-child(4)').textContent = packageData.goldAmount;
                row.querySelector('td:nth-child(5)').textContent = packageData.blueBonus;
                row.querySelector('td:nth-child(6)').textContent = formatter.format(packageData.price);
                row.querySelector('td:nth-child(7)').textContent = packageData.discount + '%';

                const statusBadge = row.querySelector('td:nth-child(8) .status-badge');
                statusBadge.textContent = packageData.status === 'active' ? 'Hoạt động' : 'Không hoạt động';
                statusBadge.className = 'status-badge ' + packageData.status;
                break;
            }
        }
    }

    // Toast notification function
    function showToast(type, message) {
        // Create toast element
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        // Add icon based on type
        let icon = '';
        switch (type) {
            case 'success':
                icon = '<i class="fas fa-check-circle"></i>';
                break;
            case 'error':
                icon = '<i class="fas fa-exclamation-circle"></i>';
                break;
            case 'info':
                icon = '<i class="fas fa-info-circle"></i>';
                break;
            case 'warning':
                icon = '<i class="fas fa-exclamation-triangle"></i>';
                break;
        }

        // Set content with icon and message
        toast.innerHTML = `
            <div class="toast-content">
                <div class="toast-icon">${icon}</div>
                <div class="toast-message">${message}</div>
            </div>
            <button class="toast-close"><i class="fas fa-times"></i></button>
        `;

        // Add to document body
        const toastContainer = document.querySelector('.toast-container') || createToastContainer();
        toastContainer.appendChild(toast);

        // Auto remove after 3 seconds
        setTimeout(() => {
            toast.classList.add('hide');
            setTimeout(() => {
                toast.remove();

                // Remove container if no more toasts
                if (!document.querySelector('.toast-container .toast')) {
                    toastContainer.remove();
                }
            }, 300); // Match CSS transition time
        }, 3000);

        // Add click listener to close button
        toast.querySelector('.toast-close').addEventListener('click', () => {
            toast.classList.add('hide');
            setTimeout(() => {
                toast.remove();

                // Remove container if no more toasts
                if (!document.querySelector('.toast-container .toast')) {
                    toastContainer.remove();
                }
            }, 300);
        });
    }

    // Create toast container if not exists
    function createToastContainer() {
        const container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
        return container;
    }

    // Date range selector handling
    const dateRangeSelector = document.querySelector('.date-range-selector');
    if (dateRangeSelector) {
        dateRangeSelector.addEventListener('change', function () {
            const selectedRange = this.value;
            filterDataByDateRange(selectedRange);
        });
    }

    // Chart type selector handling
    const chartTypeSelect = document.querySelector('.chart-type-select');
    if (chartTypeSelect) {
        chartTypeSelect.addEventListener('change', function () {
            const selectedChartType = this.value;
            updateChartType(selectedChartType);
        });
    }

    // Chart period selector handling
    const chartPeriodSelect = document.querySelector('.chart-period-select');
    if (chartPeriodSelect) {
        chartPeriodSelect.addEventListener('change', function () {
            const selectedPeriod = this.value;
            updateChartPeriod(selectedPeriod);
        });
    }

    // Search input handling
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function () {
            const searchTerm = this.value.toLowerCase().trim();
            searchTable(searchTerm);
        });
    }

    // Filter select handling (transactions)
    const filterSelect = document.querySelector('.filter-select');
    if (filterSelect) {
        filterSelect.addEventListener('change', function () {
            const selectedFilter = this.value;
            filterTransactions(selectedFilter);
        });
    }

    // Bulk action select handling
    const bulkActionSelect = document.querySelector('.bulk-action-select');
    const applyActionBtn = document.querySelector('.apply-action.secondary-btn');
    if (bulkActionSelect && applyActionBtn) {
        applyActionBtn.addEventListener('click', function () {
            const selectedAction = bulkActionSelect.value;
            const checkedRows = document.querySelectorAll('.row-checkbox:checked');

            if (checkedRows.length === 0) {
                showToast('error', 'Vui lòng chọn ít nhất một mục');
                return;
            }

            applyBulkAction(selectedAction, checkedRows);
        });
    }

    // Export button handling
    const exportBtns = document.querySelectorAll('.export-btn');
    if (exportBtns.length > 0) {
        exportBtns.forEach(btn => {
            btn.addEventListener('click', function () {
                const exportType = this.dataset.type || 'csv';
                exportData(exportType);
            });
        });
    }

    // Action buttons (general) handling
    const actionButtons = document.querySelectorAll('.action-buttons .action-btn:not([title="Duyệt"]):not([title="Từ chối"])');
    if (actionButtons.length > 0) {
        actionButtons.forEach(btn => {
            btn.addEventListener('click', function () {
                const action = this.getAttribute('title');
                const row = this.closest('tr');
                const txId = row?.querySelector('td:first-child')?.textContent || 'N/A';

                switch (action) {
                    case 'Xem chi tiết':
                        // Already handled
                        break;
                    case 'Xuất hóa đơn':
                        generateInvoice(txId);
                        break;
                    case 'Hoàn tiền':
                        // Already handled
                        break;
                    // Add more action cases as needed
                }
            });
        });
    }

    // Secondary buttons (generic)
    const secondaryBtns = document.querySelectorAll('.secondary-btn:not(.apply-action)');
    if (secondaryBtns.length > 0) {
        secondaryBtns.forEach(btn => {
            btn.addEventListener('click', function () {
                const action = this.dataset.action;
                if (action) {
                    handleSecondaryAction(action, this);
                }
            });
        });
    }

    // Functions for handling various actions
    function filterDataByDateRange(range) {
        console.log('Filtering by date range:', range);
        showToast('info', `Lọc dữ liệu theo khoảng thời gian: ${range}`);
        // Implement actual filtering logic here
    }

    function updateChartType(chartType) {
        console.log('Updating chart type to:', chartType);
        showToast('info', `Đã đổi loại biểu đồ thành: ${chartType}`);
        // Implement chart type update logic here
    }

    function updateChartPeriod(period) {
        console.log('Updating chart period to:', period);
        showToast('info', `Đã đổi kỳ thống kê thành: ${period}`);
        // Implement chart period update logic here
    }

    function searchTable(term) {
        // Get the active table (either packages or transactions)
        const activeTable = document.querySelector('.data-table:not([style*="display: none"])');
        if (!activeTable) return;

        const rows = activeTable.querySelectorAll('tbody tr');

        if (term === '') {
            // Reset all rows to visible
            rows.forEach(row => { row.style.display = ''; });
        } else {
            // Filter rows based on search term
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(term)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        }

        // Update row count display if available
        const countDisplay = document.querySelector('.showing-entries span');
        if (countDisplay) {
            const visibleRows = [...rows].filter(row => row.style.display !== 'none').length;
            countDisplay.textContent = `Hiển thị ${visibleRows} / ${rows.length} mục`;
        }
    }

    function filterTransactions(filter) {
        console.log('Filtering transactions by:', filter);
        const rows = document.querySelectorAll('.transactions-table tbody tr');

        rows.forEach(row => {
            const transactionType = row.querySelector('td:nth-child(2)').textContent.toLowerCase();
            const status = row.querySelector('.status-badge').textContent.toLowerCase();

            if (filter === 'all') {
                row.style.display = '';
            } else if (filter === 'recharge' && transactionType.includes('nạp')) {
                row.style.display = '';
            } else if (filter === 'purchase' && transactionType.includes('mua')) {
                row.style.display = '';
            } else if (filter === 'withdrawal' && transactionType.includes('rút')) {
                row.style.display = '';
            } else if (filter === 'pending' && status.includes('chờ')) {
                row.style.display = '';
            } else if (filter === 'success' && status.includes('thành công')) {
                row.style.display = '';
            } else if (filter === 'canceled' && status.includes('hủy')) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });

        // Update row count display if available
        const countDisplay = document.querySelector('.showing-entries span');
        if (countDisplay) {
            const visibleRows = [...rows].filter(row => row.style.display !== 'none').length;
            countDisplay.textContent = `Hiển thị ${visibleRows} / ${rows.length} mục`;
        }
    }

    function applyBulkAction(action, checkedRows) {
        const selectedIds = [...checkedRows].map(checkbox => {
            return checkbox.closest('tr').querySelector('td:nth-child(2)')?.textContent;
        }).filter(Boolean);

        console.log(`Applying bulk action '${action}' to ids:`, selectedIds);

        switch (action) {
            case 'delete':
                if (confirm(`Bạn có chắc chắn muốn xóa ${selectedIds.length} mục đã chọn?`)) {
                    checkedRows.forEach(checkbox => {
                        checkbox.closest('tr').remove();
                    });
                    showToast('success', `Đã xóa ${selectedIds.length} mục`);
                }
                break;
            case 'export':
                showToast('info', `Đang xuất ${selectedIds.length} mục đã chọn...`);
                setTimeout(() => {
                    showToast('success', `Đã xuất ${selectedIds.length} mục thành công!`);
                }, 1500);
                break;
            case 'approve':
                if (confirm(`Xác nhận duyệt ${selectedIds.length} mục đã chọn?`)) {
                    checkedRows.forEach(checkbox => {
                        const statusBadge = checkbox.closest('tr').querySelector('.status-badge');
                        if (statusBadge) {
                            statusBadge.textContent = 'Thành công';
                            statusBadge.className = 'status-badge success';
                        }
                    });
                    showToast('success', `Đã duyệt ${selectedIds.length} mục thành công!`);
                }
                break;
            default:
                showToast('error', 'Hành động không được hỗ trợ');
        }

        // Reset checkboxes
        document.querySelector('.select-all-checkbox').checked = false;
        checkedRows.forEach(checkbox => {
            checkbox.checked = false;
        });

        // Hide bulk actions
        document.querySelector('.bulk-actions')?.classList.remove('active');
    }

    function exportData(type) {
        console.log('Exporting data as', type);
        showToast('info', 'Đang chuẩn bị xuất dữ liệu...');

        setTimeout(() => {
            showToast('success', `Đã xuất dữ liệu dưới dạng ${type.toUpperCase()} thành công!`);
        }, 1500);
    }

    function handleSecondaryAction(action, button) {
        console.log('Handling secondary action:', action);

        switch (action) {
            case 'refresh':
                showToast('info', 'Đang tải lại dữ liệu...');
                setTimeout(() => {
                    showToast('success', 'Đã tải lại dữ liệu thành công!');
                }, 1000);
                break;
            case 'reset':
                // Reset filters
                document.querySelectorAll('select').forEach(select => {
                    select.selectedIndex = 0;
                });
                document.querySelectorAll('input[type="text"]').forEach(input => {
                    input.value = '';
                });

                // Show all rows
                document.querySelectorAll('.data-table tbody tr').forEach(row => {
                    row.style.display = '';
                });

                showToast('info', 'Đã xóa tất cả bộ lọc');
                break;
            case 'invoice':
                const txId = button.dataset.txid;
                if (txId) {
                    generateInvoice(txId);
                }
                break;
            case 'email':
                const emailData = button.dataset.email;
                if (emailData) {
                    sendEmail(emailData);
                } else {
                    showToast('info', 'Đang chuẩn bị gửi email...');
                }
                break;
            default:
                if (button.textContent.includes('Thêm mới') || button.textContent.includes('Tạo mới')) {
                    showToast('info', 'Đang chuẩn bị thêm mới...');
                }
        }
    }

    // Function to generate and download invoice
    function generateInvoice(transactionId) {
        showToast('info', 'Đang tạo hóa đơn...');

        // In thực tế, chúng ta sẽ gọi API để tạo hóa đơn
        // Code này chỉ để demo chức năng
        setTimeout(function () {
            showToast('success', `Đã tạo hóa đơn cho giao dịch ${transactionId}`);
            // Mô phỏng tải xuống file PDF
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = '#';
            a.download = `invoice_${transactionId}.pdf`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            // Option: Simulate file download
            // const link = document.createElement('a');
            // link.href = URL.createObjectURL(new Blob(['Invoice content'], {type: 'application/pdf'}));
            // link.download = `invoice-${transactionId}.pdf`;
            // link.click();
        }, 1500);
    }

    // Function to send email
    function sendEmail(emailData) {
        let recipient = 'user@example.com';
        let subject = 'Thông báo giao dịch';

        try {
            if (emailData) {
                const parsedData = JSON.parse(emailData);
                recipient = parsedData.email || recipient;
                subject = parsedData.subject || subject;
            }
        } catch (e) {
            console.error('Invalid email data', e);
        }

        showToast('info', `Đang gửi email đến ${recipient}...`);

        // Simulated email sending
        setTimeout(() => {
            showToast('success', `Đã gửi email thành công đến ${recipient}`);
        }, 1500);
    }

    // Hàm xử lý các chức năng quản lý yêu cầu rút tiền
    function initWithdrawalRequestHandlers() {
        console.log("Initializing withdrawal request handlers");

        // Dữ liệu mẫu về trạng thái yêu cầu rút tiền
        const withdrawalStatuses = {
            'pending': {
                text: 'Chờ duyệt',
                class: 'status-pending',
                icon: 'fa-clock'
            },
            'approved': {
                text: 'Đã duyệt',
                class: 'status-approved',
                icon: 'fa-check-circle'
            },
            'processing': {
                text: 'Đang chuyển khoản',
                class: 'status-processing',
                icon: 'fa-spinner fa-spin'
            },
            'completed': {
                text: 'Hoàn thành',
                class: 'status-completed',
                icon: 'fa-check-double'
            },
            'rejected': {
                text: 'Từ chối',
                class: 'status-rejected',
                icon: 'fa-times-circle'
            }
        };

        // Danh sách dữ liệu mẫu các yêu cầu rút tiền
        const withdrawalRequests = [
            {
                id: 'WD-123456',
                userId: 'UID-7890',
                userName: 'Nguyễn Văn A',
                email: 'nguyenvana@example.com',
                phone: '0912345678',
                amount: 2500000,
                fee: 15000,
                actualAmount: 2485000,
                bankName: 'VietcomBank',
                bankAccount: '1234567890',
                accountHolder: 'NGUYEN VAN A',
                branch: 'Hồ Chí Minh',
                requestDate: '05/06/2025 09:15',
                status: 'pending',
                notes: '',
                history: [
                    {
                        time: '05/06/2025 09:15',
                        content: 'đã tạo yêu cầu rút tiền.'
                    }
                ]
            },
            {
                id: 'WD-123457',
                userId: 'UID-7891',
                userName: 'Trần Thị B',
                email: 'tranthib@example.com',
                phone: '0923456789',
                amount: 1800000,
                fee: 10000,
                actualAmount: 1790000,
                bankName: 'Agribank',
                bankAccount: '9876543210',
                accountHolder: 'TRAN THI B',
                branch: 'Hà Nội',
                requestDate: '04/06/2025 14:30',
                status: 'approved',
                approvedDate: '04/06/2025 15:45',
                notes: 'Đã duyệt yêu cầu',
                history: [
                    {
                        time: '04/06/2025 14:30',
                        content: 'đã tạo yêu cầu rút tiền.'
                    },
                    {
                        time: '04/06/2025 15:45',
                        content: 'Admin đã duyệt yêu cầu.'
                    }
                ]
            },
            {
                id: 'WD-123458',
                userId: 'UID-7892',
                userName: 'Lê Văn C',
                email: 'levanc@example.com',
                phone: '0934567890',
                amount: 5000000,
                fee: 25000,
                actualAmount: 4975000,
                bankName: 'TPBank',
                bankAccount: '5678901234',
                accountHolder: 'LE VAN C',
                branch: 'Đà Nẵng',
                requestDate: '03/06/2025 10:20',
                status: 'completed',
                approvedDate: '03/06/2025 11:30',
                processedDate: '03/06/2025 14:15',
                completedDate: '03/06/2025 16:45',
                transactionId: 'TX-987654',
                notes: 'Đã chuyển khoản thành công',
                history: [
                    {
                        time: '03/06/2025 10:20',
                        content: 'đã tạo yêu cầu rút tiền.'
                    },
                    {
                        time: '03/06/2025 11:30',
                        content: 'Admin đã duyệt yêu cầu.'
                    },
                    {
                        time: '03/06/2025 14:15',
                        content: 'Admin đã bắt đầu xử lý chuyển khoản.'
                    },
                    {
                        time: '03/06/2025 16:45',
                        content: 'Admin đã hoàn thành chuyển khoản. Mã giao dịch: TX-987654.'
                    }
                ]
            },
            {
                id: 'WD-123459',
                userId: 'UID-7893',
                userName: 'Phạm Thị D',
                email: 'phamthid@example.com',
                phone: '0945678901',
                amount: 1000000,
                fee: 8000,
                actualAmount: 992000,
                bankName: 'BIDV',
                bankAccount: '0987654321',
                accountHolder: 'PHAM THI D',
                branch: 'Cần Thơ',
                requestDate: '02/06/2025 16:10',
                status: 'rejected',
                rejectedDate: '02/06/2025 17:20',
                rejectionReason: 'Thông tin tài khoản ngân hàng không chính xác',
                notes: 'Từ chối do thông tin sai',
                history: [
                    {
                        time: '02/06/2025 16:10',
                        content: 'đã tạo yêu cầu rút tiền.'
                    },
                    {
                        time: '02/06/2025 17:20',
                        content: 'Admin đã từ chối yêu cầu với lý do: Thông tin tài khoản ngân hàng không chính xác.'
                    }
                ]
            },
            {
                id: 'WD-123460',
                userId: 'UID-7894',
                userName: 'Hoàng Văn E',
                email: 'hoangvane@example.com',
                phone: '0956789012',
                amount: 3500000,
                fee: 20000,
                actualAmount: 3480000,
                bankName: 'Techcombank',
                bankAccount: '6789012345',
                accountHolder: 'HOANG VAN E',
                branch: 'Hải Phòng',
                requestDate: '01/06/2025 08:45',
                status: 'processing',
                approvedDate: '01/06/2025 09:30',
                processedDate: '01/06/2025 14:00',
                notes: 'Đang xử lý chuyển khoản',
                history: [
                    {
                        time: '01/06/2025 08:45',
                        content: 'đã tạo yêu cầu rút tiền.'
                    },
                    {
                        time: '01/06/2025 09:30',
                        content: 'Admin đã duyệt yêu cầu.'
                    },
                    {
                        time: '01/06/2025 14:00',
                        content: 'Admin đã bắt đầu xử lý chuyển khoản.'
                    }
                ]
            }
        ];

        // Hàm định dạng tiền thành định dạng VND
        function formatCurrency(amount) {
            return amount.toLocaleString('vi-VN') + ' đ';
        }

        // Hàm xử lý sự kiện xem chi tiết yêu cầu rút tiền
        function showWithdrawalDetail(requestId) {
            // Tìm yêu cầu rút tiền theo ID từ dữ liệu mẫu
            const request = withdrawalRequests.find(req => req.id === requestId);
            if (!request) {
                showToast('Không tìm thấy thông tin yêu cầu rút tiền', 'error');
                return;
            }

            // Cập nhật thông tin trong modal
            document.getElementById('withdrawalIdDetail').textContent = request.id;
            document.getElementById('withdrawalStatusDetail').innerHTML = `
                <span class="status ${withdrawalStatuses[request.status].class}">
                    <i class="fas ${withdrawalStatuses[request.status].icon}"></i>
                    ${withdrawalStatuses[request.status].text}
                </span>
            `;
            document.getElementById('withdrawalDateDetail').textContent = request.requestDate;
            document.getElementById('withdrawalAmountDetail').textContent = formatCurrency(request.amount);
            document.getElementById('withdrawalFeeDetail').textContent = formatCurrency(request.fee);
            document.getElementById('withdrawalActualAmountDetail').textContent = formatCurrency(request.actualAmount);

            // Cập nhật thông tin người dùng
            document.getElementById('withdrawalUserIdDetail').textContent = request.userId;
            document.getElementById('withdrawalUserNameDetail').textContent = request.userName;
            document.getElementById('withdrawalUserEmailDetail').textContent = request.email;
            document.getElementById('withdrawalUserPhoneDetail').textContent = request.phone;

            // Cập nhật thông tin ngân hàng
            document.getElementById('withdrawalBankNameDetail').textContent = request.bankName;
            document.getElementById('withdrawalAccountNumberDetail').textContent = request.bankAccount;
            document.getElementById('withdrawalAccountHolderDetail').textContent = request.accountHolder;
            document.getElementById('withdrawalBankBranchDetail').textContent = request.branch || 'Không có';

            // Cập nhật thông tin ghi chú
            document.getElementById('withdrawalNotesDetail').textContent = request.notes || 'Không có ghi chú';

            // Cập nhật timeline dựa trên trạng thái hiện tại
            updateTimeline(request.status, request);

            // Cập nhật lịch sử xử lý
            const historyLogs = document.getElementById('historyLogsDetail');
            historyLogs.innerHTML = '';

            if (request.history && request.history.length > 0) {
                request.history.forEach(log => {
                    const logItem = document.createElement('div');
                    logItem.className = 'log-item';
                    logItem.innerHTML = `
                        <div class="log-time">${log.time}</div>
                        <div class="log-content">
                            ${log.content.startsWith('Admin') ? log.content : `<span class="highlight">Người dùng</span> ${log.content}`}
                        </div>
                    `;
                    historyLogs.appendChild(logItem);
                });
            } else {
                historyLogs.innerHTML = '<div class="log-item">Không có lịch sử xử lý</div>';
            }

            // Hiển thị hoặc ẩn các nút hành động dựa trên trạng thái hiện tại
            updateWithdrawalActionButtons(request.status);

            // Lưu ID yêu cầu vào các nút hành động để xử lý sau này
            document.querySelectorAll('#withdrawalDetailModal .transaction-actions button').forEach(btn => {
                btn.setAttribute('data-request-id', requestId);
            });

            // Hiển thị modal
            showModal('withdrawalDetailModal');
        }

        // Hàm cập nhật timeline dựa trên trạng thái hiện tại
        function updateTimeline(status, requestData) {
            const timelineSteps = document.querySelectorAll('#withdrawalTimeline .timeline-step');

            // Reset tất cả các bước
            timelineSteps.forEach(step => step.classList.remove('completed'));

            // Cập nhật các bước đã hoàn thành dựa trên trạng thái
            switch (status) {
                case 'completed':
                    document.querySelector('[data-step="completed"]').classList.add('completed');
                    document.querySelector('[data-step="completed"] .timeline-date').textContent = requestData.completedDate || '';
                // Fall through để thêm các bước trước đó

                case 'processing':
                    document.querySelector('[data-step="processing"]').classList.add('completed');
                    document.querySelector('[data-step="processing"] .timeline-date').textContent = requestData.processedDate || '';
                // Fall through để thêm các bước trước đó

                case 'approved':
                    document.querySelector('[data-step="approved"]').classList.add('completed');
                    document.querySelector('[data-step="approved"] .timeline-date').textContent = requestData.approvedDate || '';
                // Fall through để thêm bước đầu tiên

                case 'pending':
                    document.querySelector('[data-step="pending"]').classList.add('completed');
                    document.querySelector('[data-step="pending"] .timeline-date').textContent = requestData.requestDate || '';
                    break;

                case 'rejected':
                    document.querySelector('[data-step="pending"]').classList.add('completed');
                    document.querySelector('[data-step="pending"] .timeline-date').textContent = requestData.requestDate || '';

                    // Đánh dấu bước từ chối riêng
                    const rejectedEl = document.createElement('div');
                    rejectedEl.className = 'rejected-notice';
                    rejectedEl.innerHTML = `
                        <div class="status status-rejected"><i class="fas fa-times-circle"></i> Từ chối</div>
                        <div class="rejected-date">${requestData.rejectedDate || ''}</div>
                        <div class="rejected-reason">${requestData.rejectionReason || 'Không có lý do'}</div>
                    `;

                    const timelineEl = document.getElementById('withdrawalTimeline');
                    const existingRejected = timelineEl.querySelector('.rejected-notice');
                    if (existingRejected) {
                        existingRejected.remove();
                    }
                    timelineEl.appendChild(rejectedEl);
                    break;
            }
        }

        // Hàm hiển thị hoặc ẩn các nút hành động dựa trên trạng thái hiện tại
        function updateWithdrawalActionButtons(status) {
            // Lấy tất cả các nút hành động
            const approveBtn = document.getElementById('approveWithdrawalBtn');
            const rejectBtn = document.getElementById('rejectWithdrawalBtn');
            const processBtn = document.getElementById('processWithdrawalBtn');
            const completeBtn = document.getElementById('completeWithdrawalBtn');
            const printBtn = document.getElementById('printWithdrawalBtn');

            // Ẩn tất cả các nút trước
            [approveBtn, rejectBtn, processBtn, completeBtn].forEach(btn => {
                if (btn) btn.style.display = 'none';
            });

            // Hiển thị nút in cho tất cả các trạng thái
            if (printBtn) printBtn.style.display = 'inline-flex';

            // Hiển thị các nút phù hợp với trạng thái
            switch (status) {
                case 'pending':
                    if (approveBtn) approveBtn.style.display = 'inline-flex';
                    if (rejectBtn) rejectBtn.style.display = 'inline-flex';
                    break;

                case 'approved':
                    if (processBtn) processBtn.style.display = 'inline-flex';
                    if (rejectBtn) rejectBtn.style.display = 'inline-flex';
                    break;

                case 'processing':
                    if (completeBtn) completeBtn.style.display = 'inline-flex';
                    break;

                case 'completed':
                case 'rejected':
                    // Không hiển thị nút hành động nào ngoài nút in
                    break;
            }
        }

        // Đăng ký sự kiện cho các nút xem chi tiết yêu cầu rút tiền
        document.querySelectorAll('.view-withdrawal-detail-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                const requestId = this.getAttribute('data-id');
                showWithdrawalDetail(requestId);
            });
        });

        // Xử lý nút duyệt yêu cầu rút tiền
        const approveWithdrawalBtn = document.getElementById('approveWithdrawalBtn');
        if (approveWithdrawalBtn) {
            approveWithdrawalBtn.addEventListener('click', function () {
                const requestId = this.getAttribute('data-request-id');
                if (!requestId) return;

                // Hiển thị xác nhận duyệt
                if (confirm('Xác nhận duyệt yêu cầu rút tiền này?')) {
                    // Mô phỏng xử lý API
                    showToast('Đang xử lý...', 'info');

                    setTimeout(() => {
                        // Tìm và cập nhật dữ liệu
                        const requestIndex = withdrawalRequests.findIndex(req => req.id === requestId);
                        if (requestIndex !== -1) {
                            // Cập nhật trạng thái
                            withdrawalRequests[requestIndex].status = 'approved';
                            withdrawalRequests[requestIndex].approvedDate = getCurrentDateTime();

                            // Thêm vào lịch sử
                            withdrawalRequests[requestIndex].history.push({
                                time: getCurrentDateTime(),
                                content: 'Admin đã duyệt yêu cầu.'
                            });

                            // Cập nhật giao diện
                            updateTimeline('approved', withdrawalRequests[requestIndex]);
                            updateWithdrawalActionButtons('approved');
                            document.getElementById('withdrawalStatusDetail').innerHTML = `
                                <span class="status ${withdrawalStatuses['approved'].class}">
                                    <i class="fas ${withdrawalStatuses['approved'].icon}"></i>
                                    ${withdrawalStatuses['approved'].text}
                                </span>
                            `;

                            // Thêm vào lịch sử trên giao diện
                            addToHistory('đã duyệt yêu cầu.');

                            showToast('Đã duyệt yêu cầu rút tiền thành công!', 'success');
                        } else {
                            showToast('Không tìm thấy thông tin yêu cầu rút tiền', 'error');
                        }
                    }, 1000);
                }
            });
        }

        // Xử lý nút từ chối yêu cầu rút tiền
        const rejectWithdrawalBtn = document.getElementById('rejectWithdrawalBtn');
        if (rejectWithdrawalBtn) {
            rejectWithdrawalBtn.addEventListener('click', function () {
                const requestId = this.getAttribute('data-request-id');
                if (!requestId) return;

                // Hiển thị modal từ chối
                document.getElementById('rejectRequestId').value = requestId;
                showModal('rejectWithdrawalModal');
            });
        }

        // Xử lý nút xác nhận từ chối trong modal từ chối
        const confirmRejectBtn = document.getElementById('confirmRejectBtn');
        if (confirmRejectBtn) {
            confirmRejectBtn.addEventListener('click', function () {
                const requestId = document.getElementById('rejectRequestId').value;
                const rejectionReason = document.getElementById('rejectionReason').value;
                const sendNotification = document.getElementById('sendRejectionEmail').checked;

                // Xác thực lý do từ chối
                if (!rejectionReason.trim()) {
                    showToast('Vui lòng nhập lý do từ chối', 'error');
                    return;
                }

                // Mô phỏng xử lý API
                showToast('Đang xử lý...', 'info');

                setTimeout(() => {
                    // Tìm và cập nhật dữ liệu
                    const requestIndex = withdrawalRequests.findIndex(req => req.id === requestId);
                    if (requestIndex !== -1) {
                        // Cập nhật trạng thái
                        withdrawalRequests[requestIndex].status = 'rejected';
                        withdrawalRequests[requestIndex].rejectedDate = getCurrentDateTime();
                        withdrawalRequests[requestIndex].rejectionReason = rejectionReason;

                        // Thêm vào lịch sử
                        withdrawalRequests[requestIndex].history.push({
                            time: getCurrentDateTime(),
                            content: `Admin đã từ chối yêu cầu với lý do: ${rejectionReason}.`
                        });

                        // Cập nhật giao diện
                        updateTimeline('rejected', withdrawalRequests[requestIndex]);
                        updateWithdrawalActionButtons('rejected');
                        document.getElementById('withdrawalStatusDetail').innerHTML = `
                            <span class="status ${withdrawalStatuses['rejected'].class}">
                                <i class="fas ${withdrawalStatuses['rejected'].icon}"></i>
                                ${withdrawalStatuses['rejected'].text}
                            </span>
                        `;

                        // Thêm vào lịch sử trên giao diện
                        addToHistory(`đã từ chối yêu cầu với lý do: ${rejectionReason}.`);

                        // Đóng modal từ chối
                        hideModal('rejectWithdrawalModal');
                        document.getElementById('rejectionReason').value = '';
                        document.getElementById('sendRejectionEmail').checked = false;

                        showToast('Đã từ chối yêu cầu rút tiền', 'success');

                        // Hiển thị thông báo nếu đã gửi email
                        if (sendNotification) {
                            showToast('Đã gửi email thông báo đến người dùng', 'info');
                        }
                    } else {
                        hideModal('rejectWithdrawalModal');
                        showToast('Không tìm thấy thông tin yêu cầu rút tiền', 'error');
                    }
                }, 1000);
            });
        }

        // Xử lý nút hủy từ chối
        const cancelRejectBtn = document.getElementById('cancelRejectBtn');
        if (cancelRejectBtn) {
            cancelRejectBtn.addEventListener('click', function () {
                hideModal('rejectWithdrawalModal');
                document.getElementById('rejectionReason').value = '';
                document.getElementById('sendRejectionEmail').checked = false;
            });
        }

        // Xử lý nút xử lý chuyển khoản
        const processWithdrawalBtn = document.getElementById('processWithdrawalBtn');
        if (processWithdrawalBtn) {
            processWithdrawalBtn.addEventListener('click', function () {
                const requestId = this.getAttribute('data-request-id');
                if (!requestId) return;

                // Hiển thị xác nhận xử lý chuyển khoản
                if (confirm('Bạn sẽ bắt đầu xử lý chuyển khoản cho yêu cầu này. Tiếp tục?')) {
                    // Mô phỏng xử lý API
                    showToast('Đang xử lý...', 'info');

                    setTimeout(() => {
                        // Tìm và cập nhật dữ liệu
                        const requestIndex = withdrawalRequests.findIndex(req => req.id === requestId);
                        if (requestIndex !== -1) {
                            // Cập nhật trạng thái
                            withdrawalRequests[requestIndex].status = 'processing';
                            withdrawalRequests[requestIndex].processedDate = getCurrentDateTime();

                            // Thêm vào lịch sử
                            withdrawalRequests[requestIndex].history.push({
                                time: getCurrentDateTime(),
                                content: 'Admin đã bắt đầu xử lý chuyển khoản.'
                            });

                            // Cập nhật giao diện
                            updateTimeline('processing', withdrawalRequests[requestIndex]);
                            updateWithdrawalActionButtons('processing');
                            document.getElementById('withdrawalStatusDetail').innerHTML = `
                                <span class="status ${withdrawalStatuses['processing'].class}">
                                    <i class="fas ${withdrawalStatuses['processing'].icon}"></i>
                                    ${withdrawalStatuses['processing'].text}
                                </span>
                            `;

                            // Thêm vào lịch sử trên giao diện
                            addToHistory('đã bắt đầu xử lý chuyển khoản.');

                            showToast('Đã cập nhật trạng thái sang đang xử lý', 'success');
                        } else {
                            showToast('Không tìm thấy thông tin yêu cầu rút tiền', 'error');
                        }
                    }, 1000);
                }
            });
        }

        // Xử lý nút hoàn thành chuyển khoản
        const completeWithdrawalBtn = document.getElementById('completeWithdrawalBtn');
        if (completeWithdrawalBtn) {
            completeWithdrawalBtn.addEventListener('click', function () {
                const requestId = this.getAttribute('data-request-id');
                if (!requestId) return;

                // Hiển thị prompt để nhập mã giao dịch
                const transactionId = prompt('Nhập mã giao dịch ngân hàng (nếu có):', '');

                // Mô phỏng xử lý API
                showToast('Đang xử lý...', 'info');

                setTimeout(() => {
                    // Tìm và cập nhật dữ liệu
                    const requestIndex = withdrawalRequests.findIndex(req => req.id === requestId);
                    if (requestIndex !== -1) {
                        // Cập nhật trạng thái
                        withdrawalRequests[requestIndex].status = 'completed';
                        withdrawalRequests[requestIndex].completedDate = getCurrentDateTime();
                        if (transactionId) {
                            withdrawalRequests[requestIndex].transactionId = transactionId;
                        }

                        // Cập nhật nội dung lịch sử
                        const historyContent = transactionId
                            ? `Admin đã hoàn thành chuyển khoản. Mã giao dịch: ${transactionId}.`
                            : 'Admin đã hoàn thành chuyển khoản.';

                        // Thêm vào lịch sử
                        withdrawalRequests[requestIndex].history.push({
                            time: getCurrentDateTime(),
                            content: historyContent
                        });

                        // Cập nhật giao diện
                        updateTimeline('completed', withdrawalRequests[requestIndex]);
                        updateWithdrawalActionButtons('completed');
                        document.getElementById('withdrawalStatusDetail').innerHTML = `
                            <span class="status ${withdrawalStatuses['completed'].class}">
                                <i class="fas ${withdrawalStatuses['completed'].icon}"></i>
                                ${withdrawalStatuses['completed'].text}
                            </span>
                        `;

                        // Thêm vào lịch sử trên giao diện
                        addToHistory(historyContent);

                        showToast('Đã hoàn thành yêu cầu rút tiền', 'success');
                    } else {
                        showToast('Không tìm thấy thông tin yêu cầu rút tiền', 'error');
                    }
                }, 1000);
            });
        }

        // Thêm xử lý sự kiện cho các nút lọc yêu cầu rút tiền
        const withdrawalFilters = document.querySelectorAll('.withdrawal-filter-status, .withdrawal-filter-amount, .withdrawal-filter-date');
        withdrawalFilters.forEach(filter => {
            filter.addEventListener('click', function (e) {
                e.preventDefault();
                const filterType = this.getAttribute('data-filter-type');
                const filterValue = this.getAttribute('data-filter-value');

                // Hiển thị thông báo lọc
                showToast(`Đang lọc yêu cầu rút tiền theo ${filterType}: ${filterValue}`, 'info');

                // Đánh dấu nút lọc được chọn
                const filterGroup = this.closest('.filter-group');
                filterGroup.querySelectorAll('a').forEach(item => item.classList.remove('active'));
                this.classList.add('active');

                // Mô phỏng lọc dữ liệu
                // Ở đây sẽ gọi API để lọc dữ liệu thực tế
                // Code xử lý lọc dữ liệu sẽ được thêm sau khi có API
                filterWithdrawalRequests(filterType, filterValue);
            });
        });

        // Xử lý nút tìm kiếm yêu cầu rút tiền
        const withdrawalSearchForm = document.querySelector('#withdrawalSearchForm');
        if (withdrawalSearchForm) {
            withdrawalSearchForm.addEventListener('submit', function (e) {
                e.preventDefault();
                const searchInput = this.querySelector('input[type="text"]');
                const searchTerm = searchInput.value.trim();

                if (searchTerm) {
                    showToast('info', `Đang tìm kiếm yêu cầu rút tiền với từ khóa: ${searchTerm}`);
                    // Mô phỏng tìm kiếm
                    filterWithdrawalRequests('search', searchTerm);
                }
            });
        }

        // Xử lý nút xuất báo cáo
        const exportReportBtn = document.querySelector('#exportWithdrawalsBtn');
        if (exportReportBtn) {
            exportReportBtn.addEventListener('click', function () {
                showToast('info', 'Đang chuẩn bị xuất báo cáo yêu cầu rút tiền...');

                // Mô phỏng xuất báo cáo
                setTimeout(() => {
                    const a = document.createElement('a');
                    a.style.display = 'none';
                    a.href = '#';
                    a.download = `withdrawal_requests_report_${new Date().toISOString().split('T')[0]}.xlsx`;
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);

                    showToast('success', 'Báo cáo đã được xuất thành công!');
                }, 1500);
            });
        }

        // Xử lý nút xem chi tiết yêu cầu rút tiền
        const viewDetailButtons = document.querySelectorAll('.withdrawal-detail-btn');
        if (viewDetailButtons.length > 0) {
            viewDetailButtons.forEach(btn => {
                btn.addEventListener('click', function () {
                    const requestId = this.getAttribute('data-id');
                    const row = this.closest('tr');
                    const status = row.querySelector('.status-badge').textContent;
                    const userName = row.querySelector('.user-info-name').textContent;
                    const goldCoins = row.querySelector('td:nth-child(3)').textContent;
                    const vndAmount = row.querySelector('td:nth-child(4)').textContent;
                    const bankInfo = row.querySelector('td:nth-child(5)').textContent;
                    const requestDate = row.querySelector('td:nth-child(6)').textContent;

                    // Cập nhật thông tin chi tiết trong modal
                    document.getElementById('withdrawalDetailId').textContent = requestId;
                    document.getElementById('requestIdDetail').textContent = requestId;
                    document.getElementById('withdrawalDetailStatus').textContent = status;
                    document.getElementById('withdrawalDetailStatus').className = 'status-badge ' + getStatusClass(status);
                    document.getElementById('userNameDetail').textContent = userName;
                    document.getElementById('requestDateDetail').textContent = requestDate;
                    document.getElementById('goldCoinDetail').textContent = goldCoins;
                    document.getElementById('amountDetail').textContent = vndAmount;

                    // Hiển thị các nút tác vụ theo trạng thái
                    toggleActionButtons(status);

                    // Hiển thị các bước trong timeline theo trạng thái
                    updateTimelineStatus(status);

                    // Hiển thị modal
                    showModal('withdrawalDetailModal');
                });
            });
        }

        // Thêm các hàm hỗ trợ
        function getStatusClass(status) {
            switch (status.toLowerCase()) {
                case 'chờ xử lý': return 'pending';
                case 'đã duyệt': return 'approved';
                case 'đang xử lý': return 'processing';
                case 'từ chối': return 'rejected';
                case 'hoàn thành': return 'completed';
                default: return '';
            }
        }

        function toggleActionButtons(status) {
            // Ẩn tất cả các nhóm nút
            document.getElementById('pendingActions').style.display = 'none';
            document.getElementById('approvedActions').style.display = 'none';
            document.getElementById('processingActions').style.display = 'none';

            // Hiển thị nhóm nút tương ứng với trạng thái
            switch (status.toLowerCase()) {
                case 'chờ xử lý':
                    document.getElementById('pendingActions').style.display = 'block';
                    break;
                case 'đã duyệt':
                    document.getElementById('approvedActions').style.display = 'block';
                    break;
                case 'đang xử lý':
                    document.getElementById('processingActions').style.display = 'block';
                    break;
            }
        }

        function updateTimelineStatus(status) {
            // Đặt lại tất cả các bước timeline
            document.querySelectorAll('.timeline-step').forEach(step => {
                step.classList.remove('completed');
            });

            // Đánh dấu các bước đã hoàn thành theo trạng thái
            document.querySelector('.timeline-step:first-child').classList.add('completed'); // Bước tạo yêu cầu luôn hoàn thành

            switch (status.toLowerCase()) {
                case 'đã duyệt':
                    document.getElementById('statusApproval').classList.add('completed');
                    break;
                case 'đang xử lý':
                    document.getElementById('statusApproval').classList.add('completed');
                    document.getElementById('statusProcess').classList.add('completed');
                    break;
                case 'hoàn thành':
                    document.getElementById('statusApproval').classList.add('completed');
                    document.getElementById('statusProcess').classList.add('completed');
                    document.getElementById('statusComplete').classList.add('completed');
                    break;
            }
        }

        // Hàm mô phỏng lọc yêu cầu rút tiền
        function filterWithdrawalRequests(filterType, filterValue) {
            const tableBody = document.querySelector('#withdrawalRequestsTable tbody');
            if (!tableBody) return;

            // Mô phỏng hiệu ứng đang tải
            tableBody.style.opacity = '0.5';

            // Mô phỏng thời gian xử lý
            setTimeout(() => {
                tableBody.style.opacity = '1';
                showToast('success', `Đã lọc yêu cầu rút tiền theo ${filterType}: ${filterValue}`);

                // Trong thực tế, chúng ta sẽ gọi API để lọc và cập nhật bảng
            }, 800);
        }

        // Xử lý nút duyệt yêu cầu rút tiền
        const approveBtn = document.getElementById('approveBtn');
        if (approveBtn) {
            approveBtn.addEventListener('click', function () {
                const requestId = document.getElementById('withdrawalDetailId').textContent;
                const userName = document.getElementById('userNameDetail').textContent;

                // Hiển thị xác nhận duyệt
                if (confirm(`Xác nhận duyệt yêu cầu rút tiền #${requestId} của người dùng ${userName}?`)) {
                    showToast('info', 'Đang xử lý yêu cầu duyệt...');

                    // Mô phỏng thời gian xử lý
                    setTimeout(() => {
                        // Cập nhật trạng thái
                        const newStatus = 'Đã duyệt';
                        document.getElementById('withdrawalDetailStatus').textContent = newStatus;
                        document.getElementById('withdrawalDetailStatus').className = 'status-badge approved';

                        // Cập nhật timeline
                        updateTimelineStatus(newStatus);
                        document.getElementById('approvalDate').textContent = getCurrentDateTime();
                        document.getElementById('approvalDesc').textContent = 'Admin đã duyệt yêu cầu';

                        // Cập nhật nút thao tác
                        toggleActionButtons(newStatus);

                        // Thêm vào lịch sử
                        addToHistory('Admin duyệt yêu cầu rút tiền');

                        showToast('success', `Đã duyệt yêu cầu rút tiền #${requestId} thành công!`);
                    }, 1000);
                }
            });
        }

        // Xử lý nút từ chối yêu cầu rút tiền
        const rejectBtn = document.getElementById('rejectBtn');
        if (rejectBtn) {
            rejectBtn.addEventListener('click', function () {
                const requestId = document.getElementById('withdrawalDetailId').textContent;
                const userName = document.getElementById('userNameDetail').textContent;

                // Cập nhật thông tin modal từ chối
                document.getElementById('rejectWithdrawalId').textContent = requestId;
                document.getElementById('rejectWithdrawalUser').textContent = userName;

                // Ẩn modal chi tiết và hiển thị modal từ chối
                document.getElementById('withdrawalDetailModal').classList.remove('active');
                showModal('rejectWithdrawalModal');
            });
        }

        // Xử lý nút xử lý chuyển khoản
        const processBtn = document.getElementById('processBtn');
        if (processBtn) {
            processBtn.addEventListener('click', function () {
                const requestId = document.getElementById('withdrawalDetailId').textContent;
                const userName = document.getElementById('userNameDetail').textContent;

                // Hiển thị xác nhận xử lý
                if (confirm(`Xác nhận bắt đầu xử lý chuyển khoản cho yêu cầu #${requestId} của người dùng ${userName}?`)) {
                    showToast('info', 'Đang cập nhật trạng thái đang xử lý...');

                    setTimeout(() => {
                        // Cập nhật trạng thái
                        const newStatus = 'Đang xử lý';
                        document.getElementById('withdrawalDetailStatus').textContent = newStatus;
                        document.getElementById('withdrawalDetailStatus').className = 'status-badge processing';

                        // Cập nhật timeline
                        updateTimelineStatus(newStatus);
                        document.getElementById('processDate').textContent = getCurrentDateTime();

                        // Cập nhật nút thao tác
                        toggleActionButtons(newStatus);

                        // Thêm vào lịch sử
                        addToHistory('Admin bắt đầu xử lý chuyển khoản');

                        showToast('success', `Đã cập nhật trạng thái thành Đang xử lý!`);
                    }, 1000);
                }
            });
        }

        // Xử lý nút hoàn thành
        const completeBtn = document.getElementById('completeBtn');
        if (completeBtn) {
            completeBtn.addEventListener('click', function () {
                const requestId = document.getElementById('withdrawalDetailId').textContent;
                const userName = document.getElementById('userNameDetail').textContent;

                // Hiển thị xác nhận hoàn thành
                if (confirm(`Xác nhận đã chuyển khoản thành công và hoàn thành yêu cầu #${requestId}?`)) {
                    showToast('info', 'Đang hoàn thành yêu cầu rút tiền...');

                    setTimeout(() => {
                        // Cập nhật trạng thái
                        const newStatus = 'Hoàn thành';
                        document.getElementById('withdrawalDetailStatus').textContent = newStatus;
                        document.getElementById('withdrawalDetailStatus').className = 'status-badge completed';

                        // Cập nhật timeline
                        updateTimelineStatus(newStatus);
                        document.getElementById('completeDate').textContent = getCurrentDateTime();

                        // Ẩn tất cả các nút hành động
                        document.getElementById('pendingActions').style.display = 'none';
                        document.getElementById('approvedActions').style.display = 'none';
                        document.getElementById('processingActions').style.display = 'none';

                        // Thêm vào lịch sử
                        addToHistory('Admin xác nhận đã chuyển khoản thành công');

                        // Gửi email thông báo
                        showToast('info', 'Đang gửi email thông báo hoàn thành...');

                        setTimeout(() => {
                            showToast('success', 'Đã gửi email thông báo hoàn thành tới người dùng');
                        }, 1000);

                        showToast('success', `Đã hoàn thành yêu cầu rút tiền #${requestId}!`);
                    }, 1500);
                }
            });
        }

        // Hàm lấy ngày giờ hiện tại theo định dạng
        function getCurrentDateTime() {
            const now = new Date();
            const day = String(now.getDate()).padStart(2, '0');
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const year = now.getFullYear();
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');

            return `${day}/${month}/${year} ${hours}:${minutes}`;
        }

        // Hàm thêm vào lịch sử xử lý
        function addToHistory(content) {
            const historyLogs = document.getElementById('historyLogsDetail');
            const now = getCurrentDateTime();

            const newLog = document.createElement('div');
            newLog.className = 'log-item';
            newLog.innerHTML = `
                <div class="log-time">${now}</div>
                <div class="log-content"><span class="highlight">Admin</span> ${content}</div>
            `;

            historyLogs.appendChild(newLog);
        }

        // Xử lý nút in thông tin yêu cầu
        const printWithdrawalBtn = document.getElementById('printWithdrawalBtn');
        if (printWithdrawalBtn) {
            printWithdrawalBtn.addEventListener('click', function () {
                const requestId = document.getElementById('withdrawalDetailId').textContent;
                showToast('info', `Đang chuẩn bị in thông tin yêu cầu #${requestId}...`);

                setTimeout(() => {
                    // Mô phỏng in thông tin
                    window.print();
                    showToast('success', 'Thông tin đã được gửi tới máy in!');
                }, 1000);
            });
        }

        // Xử lý nút cancel trong modal từ chối (nút thứ hai)
        const cancelRejectBtnSecondary = document.querySelector('#rejectWithdrawalModal .cancel-btn');
        if (cancelRejectBtnSecondary) {
            cancelRejectBtnSecondary.addEventListener('click', function () {
                // Ẩn modal từ chối
                document.getElementById('rejectWithdrawalModal').classList.remove('active');
                // Hiển thị lại modal chi tiết
                setTimeout(() => {
                    showModal('withdrawalDetailModal');
                }, 300);
            });
        }

        // Xử lý nút xác nhận từ chối (nút thứ hai)
        const confirmRejectBtnSecondary = document.getElementById('confirmRejectBtn');
        if (confirmRejectBtnSecondary) {
            confirmRejectBtnSecondary.addEventListener('click', function () {
                const requestId = document.getElementById('rejectWithdrawalId').textContent;
                const reason = document.getElementById('rejectReason').value.trim();

                if (!reason) {
                    alert('Vui lòng nhập lý do từ chối yêu cầu');
                    return;
                }

                showToast('info', 'Đang xử lý yêu cầu từ chối...');

                // Mô phỏng thời gian xử lý
                setTimeout(() => {
                    // Ẩn modal từ chối
                    document.getElementById('rejectWithdrawalModal').classList.remove('active');

                    // Cập nhật trạng thái trong modal chi tiết
                    document.getElementById('withdrawalDetailStatus').textContent = 'Từ chối';
                    document.getElementById('withdrawalDetailStatus').className = 'status-badge rejected';

                    // Ẩn tất cả các nút hành động
                    document.getElementById('pendingActions').style.display = 'none';
                    document.getElementById('approvedActions').style.display = 'none';
                    document.getElementById('processingActions').style.display = 'none';

                    // Cập nhật timeline
                    document.getElementById('approvalDate').textContent = getCurrentDateTime();
                    document.getElementById('approvalDesc').textContent = 'Admin từ chối yêu cầu: ' + reason;

                    // Thêm vào lịch sử
                    addToHistory(`Admin từ chối yêu cầu với lý do: ${reason}`);

                    // Kiểm tra gửi email
                    if (document.getElementById('notifyReject').checked) {
                        showToast('info', 'Đang gửi email thông báo từ chối...');

                        setTimeout(() => {
                            showToast('success', 'Đã gửi email thông báo từ chối tới người dùng');
                        }, 1000);
                    }

                    showToast('success', `Đã từ chối yêu cầu rút tiền #${requestId} thành công!`);

                    // Hiển thị lại modal chi tiết sau 500ms
                    setTimeout(() => {
                        showModal('withdrawalDetailModal');
                        document.getElementById('rejectReason').value = '';
                    }, 500);
                }, 1000);
            });
        }
    }

    // Xử lý các chức năng của phần quản lý yêu cầu rút tiền
    initWithdrawalRequestHandlers();

    // Pagination handling
    const paginationLinks = document.querySelectorAll('.pagination-link');
    if (paginationLinks.length > 0) {
        paginationLinks.forEach(link => {
            link.addEventListener('click', function (e) {
                e.preventDefault();

                // Remove active class from all links
                paginationLinks.forEach(l => l.classList.remove('active'));

                // Add active class to clicked link
                this.classList.add('active');

                const page = this.textContent;
                const table = this.closest('.tab-content').querySelector('.data-table');

                // Update table with new page data (simulated)
                showToast('info', `Đang chuyển đến trang ${page}...`);

                // Simulate loading
                const tableBody = table.querySelector('tbody');
                if (tableBody) {
                    tableBody.style.opacity = '0.5';
                    setTimeout(() => {
                        tableBody.style.opacity = '1';
                        showToast('success', `Đã chuyển đến trang ${page} thành công!`);
                    }, 800);
                }
            });
        });
    }

    // Cancel buttons
    const cancelBtns = document.querySelectorAll('.cancel-btn');
    if (cancelBtns.length > 0) {
        cancelBtns.forEach(btn => {
            btn.addEventListener('click', function () {
                // Find closest modal and close it
                const modal = this.closest('.modal');
                if (modal) {
                    modal.classList.remove('active');
                }

                // Reset form if exists
                const form = this.closest('form');
                if (form) {
                    form.reset();
                }
            });
        });
    }

    // Primary email buttons
    const emailBtns = document.querySelectorAll('.email-btn.primary-btn');
    if (emailBtns.length > 0) {
        emailBtns.forEach(btn => {
            btn.addEventListener('click', function () {
                const txId = this.dataset.txid || 'N/A';
                const userEmail = this.dataset.email || 'user@example.com';

                sendEmail(JSON.stringify({
                    txId: txId,
                    email: userEmail,
                    subject: `Thông báo về giao dịch #${txId}`
                }));
            });
        });
    }

    // Confirm refund button
    const confirmRefundBtn = document.querySelector('.confirm-refund-btn.danger-btn');
    if (confirmRefundBtn) {
        confirmRefundBtn.addEventListener('click', function () {
            const refundModal = document.getElementById('refundModal');
            const form = refundModal?.querySelector('form');

            if (form) {
                // Trigger form submit
                const submitEvent = new Event('submit', { cancelable: true, bubbles: true });
                form.dispatchEvent(submitEvent);
            }
        });
    }

    // Handle table row highlighting on hover
    const dataTableRows = document.querySelectorAll('.data-table tbody tr');
    if (dataTableRows.length > 0) {
        dataTableRows.forEach(row => {
            row.addEventListener('mouseenter', function () {
                this.classList.add('highlight');
            });

            row.addEventListener('mouseleave', function () {
                this.classList.remove('highlight');
            });
        });
    }
});