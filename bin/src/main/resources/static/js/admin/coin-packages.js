
document.addEventListener('DOMContentLoaded', function () {
    // Flatpickr cho date picker
    if (typeof flatpickr === 'function') {
        flatpickr(".date-picker", {
            dateFormat: "d/m/Y",
            locale: "vi"
        });
    }

    // Xử lý nút thêm gói xu
    const addPackageBtn = document.getElementById('addPackageBtn');
    if (addPackageBtn) {
        addPackageBtn.addEventListener('click', function () {
            // Reset form về giá trị mặc định
            document.getElementById('coinPackageForm').reset();
            document.getElementById('packageModalTitle').textContent = 'Thêm gói xu mới';

            // Hiển thị modal
            const modal = document.getElementById('coinPackageModal');
            if (modal) {
                modal.classList.add('active');
            }
        });
    }

    // Gắn sự kiện cho nút chỉnh sửa gói xu
    const editButtons = document.querySelectorAll('.edit-package');
    if (editButtons.length > 0) {
        editButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const cells = row.querySelectorAll('td');

                // Lấy thông tin từ hàng trong bảng
                const packageId = cells[1].textContent;
                const packageName = cells[2].textContent;
                const goldAmount = parseInt(cells[3].textContent);
                const blueAmount = parseInt(cells[4].textContent);
                const packagePrice = parseFloat(cells[5].textContent.replace(/[^\d]/g, ''));
                const discountPercent = parseInt(cells[6].textContent.replace('%', ''));
                const status = cells[7].querySelector('.status-badge').classList.contains('active') ? 'active' : 'promotion';

                // Điền vào form
                document.getElementById('packageId').value = packageId;
                document.getElementById('packageName').value = packageName;
                document.getElementById('goldAmount').value = goldAmount;
                document.getElementById('blueAmount').value = blueAmount;
                document.getElementById('packagePrice').value = packagePrice;
                document.getElementById('discountPercent').value = discountPercent;
                document.getElementById('packageStatus').value = status;

                // Cập nhật tiêu đề modal
                document.getElementById('packageModalTitle').textContent = 'Chỉnh sửa gói xu';

                // Hiển thị modal
                const modal = document.getElementById('coinPackageModal');
                if (modal) {
                    modal.classList.add('active');
                }
            });
        });
    }

    // Gắn sự kiện cho nút xóa gói xu
    const deleteButtons = document.querySelectorAll('.delete-package');
    if (deleteButtons.length > 0) {
        deleteButtons.forEach(button => {
            button.addEventListener('click', function () {
                const row = this.closest('tr');
                const packageId = row.querySelector('td:nth-child(2)').textContent;
                const packageName = row.querySelector('td:nth-child(3)').textContent;

                // Điền thông tin vào modal xóa
                document.getElementById('deletePackageId').textContent = packageId;
                document.getElementById('deletePackageName').textContent = packageName;

                // Hiển thị modal xóa
                const modal = document.getElementById('deletePackageModal');
                if (modal) {
                    modal.classList.add('active');
                }
            });
        });
    }

    // Xử lý nút confirm xóa
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function () {
            const packageId = document.getElementById('deletePackageId').textContent;

            // Tìm hàng cần xóa
            const packageRows = document.querySelectorAll('.packages-table tbody tr');
            let targetRow = null;

            packageRows.forEach(row => {
                const id = row.querySelector('td:nth-child(2)').textContent;
                if (id === packageId) {
                    targetRow = row;
                }
            });

            if (targetRow) {
                // Xóa hàng từ bảng
                targetRow.parentNode.removeChild(targetRow);

                // Ẩn modal xóa
                const modal = document.getElementById('deletePackageModal');
                if (modal) {
                    modal.classList.remove('active');
                }

                // Hiển thị thông báo
                showToast('Đã xóa gói xu thành công', 'success');
            }
        });
    }

    // Xử lý submit form gói xu
    const coinPackageForm = document.getElementById('coinPackageForm');
    if (coinPackageForm) {
        coinPackageForm.addEventListener('submit', function (e) {
            e.preventDefault();

            // Lấy dữ liệu từ form
            const formData = {
                id: document.getElementById('packageId').value,
                name: document.getElementById('packageName').value,
                gold: document.getElementById('goldAmount').value,
                blue: document.getElementById('blueAmount').value,
                price: document.getElementById('packagePrice').value,
                discount: document.getElementById('discountPercent').value,
                status: document.getElementById('packageStatus').value
            };

            // Xử lý thêm/sửa gói xu
            // Trong trường hợp này, chỉ đóng modal và hiển thị thông báo
            const modal = document.getElementById('coinPackageModal');
            if (modal) {
                modal.classList.remove('active');
            }

            showToast('Đã lưu gói xu thành công', 'success');
        });
    }
});
