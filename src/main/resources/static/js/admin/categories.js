console.log('jQuery loaded:', typeof $);

// Cấu hình toastr
toastr.options = {
    closeButton: true,
    progressBar: true,
    positionClass: 'toast-top-right',
    timeOut: 2000 // Hiển thị thông báo trong 2 giây
};

let categories = [
    {
        id: 1,
        name: "Công nghệ thông tin",
        description: "Tài liệu về lập trình, khoa học máy tính, trí tuệ nhân tạo, an ninh mạng và nhiều lĩnh vực công nghệ thông tin khác.",
        iconClass: "fas fa-laptop-code",
        color: "#0ea5e9",
        documents: 1245,
        views: 25800,
        downloads: 12400,
        created: "2023-03-10",
        status: "active"
    },
    {
        id: 2,
        name: "Kinh tế - Quản trị",
        description: "Tài liệu về quản trị doanh nghiệp, kinh tế học, tài chính, marketing, quản trị nhân sự và các lĩnh vực kinh doanh.",
        iconClass: "fas fa-chart-line",
        color: "#f59e0b",
        documents: 985,
        views: 18500,
        downloads: 8600,
        created: "2023-03-12",
        status: "active"
    },
    {
        id: 3,
        name: "Ngoại ngữ",
        description: "Tài liệu học tiếng Anh, tiếng Pháp, tiếng Đức, tiếng Nhật, tiếng Hàn, tiếng Trung và các ngoại ngữ khác.",
        iconClass: "fas fa-language",
        color: "#10b981",
        documents: 756,
        views: 15200,
        downloads: 7800,
        created: "2023-03-15",
        status: "active"
    },
    {
        id: 4,
        name: "Kỹ thuật - Công nghệ",
        description: "Tài liệu về cơ khí, điện - điện tử, xây dựng, kiến trúc, vật liệu và các lĩnh vực kỹ thuật công nghệ khác.",
        iconClass: "fas fa-cogs",
        color: "#6366f1",
        documents: 654,
        views: 12300,
        downloads: 5200,
        created: "2023-03-18",
        status: "active"
    },
    {
        id: 5,
        name: "Khoa học xã hội",
        description: "Tâm lý học, xã hội học, nhân học, triết học, lịch sử, địa lý và các ngành khoa học xã hội khác.",
        iconClass: "fas fa-users",
        color: "#ef4444",
        documents: 543,
        views: 10800,
        downloads: 4700,
        created: "2023-03-20",
        status: "inactive"
    },
];

let filteredCategories = [...categories];
let selectedCategories = new Set();
let currentCatPage = 1;
const categoriesPerPage = 10; // Increased from 6 to 10 to match server pagination
let editingCategoryId = null;

// Global functions - định nghĩa trước để có thể truy cập từ mọi nơi
function updateBulkActionButtons() {
    const hasSelection = selectedCategories.size > 0;
    const deleteBtn = document.getElementById('deleteSelectedBtn');
    const restoreBtn = document.getElementById('restoreSelectedBtn');
      const permanentDeleteBtn = document.getElementById('permanentDeleteSelectedBtn');
    // permanentDeleteBtn removed
    
    // Update delete button state
    if (deleteBtn) {
        if (hasSelection) {
            deleteBtn.classList.remove('disabled');
            deleteBtn.disabled = false;
        } else {
            deleteBtn.classList.add('disabled');
            deleteBtn.disabled = true;
        }
    }
    
    // Update restore and permanent delete buttons
    if (restoreBtn) {
        if (hasSelection) {
            restoreBtn.style.display = 'inline-flex';
            restoreBtn.classList.remove('disabled');
            restoreBtn.disabled = false;
        } else {
            restoreBtn.style.display = 'inline-flex';
            restoreBtn.classList.add('disabled');
            restoreBtn.disabled = true;
        }
    }

    if (permanentDeleteBtn) {
        if (hasSelection) {
            permanentDeleteBtn.classList.remove('disabled');
            permanentDeleteBtn.disabled = false;
        } else {
            permanentDeleteBtn.classList.add('disabled');
            permanentDeleteBtn.disabled = true;
        }
    }
    
    // Update restore and permanent delete buttons
   

    // permanentDeleteBtn logic removed
}

function showConfirmModal(title, message, onConfirm, isDanger = false) {
    const modal = document.getElementById('confirmModal');
    const titleEl = document.getElementById('confirmTitle');
    const messageEl = document.getElementById('confirmMessage');
    const confirmBtn = document.getElementById('confirmOk');
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    
    // Set button style based on action type
    confirmBtn.className = isDanger ? 'modal-btn confirm-btn danger' : 'modal-btn confirm-btn';
    
    // Remove previous event listeners
    const newConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
    
    // Add new event listener
    newConfirmBtn.addEventListener('click', function() {
        hideConfirmModal();
        onConfirm();
    });
    
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function hideConfirmModal() {
    const modal = document.getElementById('confirmModal');
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

function updateSelectAllState() {
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.category-checkbox:not(#selectAll)');
    if (!selectAll) return;
    const allChecked = checkboxes.length && [...checkboxes].every(c => c.checked);
    const someChecked = [...checkboxes].some(c => c.checked);
    selectAll.checked = allChecked;
    selectAll.indeterminate = someChecked && !allChecked;
}

function deleteSelectedCategories() {
    if (selectedCategories.size === 0) {
        return; // Không làm gì cả, không hiển thị thông báo
    }
    
    showConfirmModal(
        'Xác nhận xóa',
        `Bạn có chắc muốn xóa ${selectedCategories.size} danh mục đã chọn?`,
        function() {
            performDeleteSelected();
        },
        true // isDanger = true
    );
}

function performDeleteSelected() {
    
    console.log('Deleting categories:', Array.from(selectedCategories));
    
    // Tạo form để submit với MVC
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/admin/categories/delete-multiple';
    form.style.display = 'none';
    
    // Thêm CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    console.log('CSRF Token found:', csrfToken ? csrfToken.getAttribute('content') : 'Not found');
    
    if (csrfToken) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken.getAttribute('content');
        form.appendChild(csrfInput);
    }
    
    // Thêm các category IDs
    let count = 0;
    selectedCategories.forEach(id => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'categoryIds';
        input.value = id;
        form.appendChild(input);
        count++;
        console.log(`Added categoryId[${count}]: ${id}`);
    });
    
    console.log('Form HTML:', form.innerHTML);
    document.body.appendChild(form);
    
    // Debug: log form data before submit
    const formData = new FormData(form);
    console.log('Form data:');
    for (let [key, value] of formData.entries()) {
        console.log(`${key}: ${value}`);
    }
    
    form.submit();
}

function deleteCategory(id) {
    console.log('Chuyển hướng đến trang xóa danh mục với ID:', id);
    // Chuyển hướng đến trang delete MVC
    window.location.href = '/admin/categories/' + id + '/delete';
}

function setupCategoryEventListeners() {
    const searchInput = document.querySelector('.search-input');
    const filterSelects = document.querySelectorAll('.filter-select');
    if (searchInput) {
        searchInput.addEventListener('input', filterCategories);
    }
    filterSelects.forEach(sel => sel.addEventListener('change', filterCategories));

    // Pagination buttons
    const pagination = document.querySelector('.pagination');
    if (pagination) {
        pagination.addEventListener('click', (e) => {
            const target = e.target.closest('.pagination-btn');
            if (!target) return;
            if (target.classList.contains('prev-btn')) previousCatPage();
            else if (target.classList.contains('next-btn')) nextCatPage();
            else if (target.classList.contains('page-btn')) goToCatPage(parseInt(target.textContent));
        });
    }

    // Grid & table selection
    document.body.addEventListener('change', (e) => {
        if (e.target.matches('.category-checkbox')) {
            const id = Number(e.target.dataset.categoryId);
            toggleCategorySelection(id, e.target.checked);
            updateSelectAllState();
        }
    });

    // Xử lý nút xóa đơn
    $(document).on('click', '.action-btn.delete', function() {
        console.log('Nút xóa đơn được nhấp');
        let categoryId = $(this).attr('data-category-id');
        console.log('Category ID:', categoryId);
        if (categoryId) {
            deleteCategory(Number(categoryId));
        } else {
            console.error('Không tìm thấy data-category-id');
            toastr.error('Không tìm thấy ID danh mục.');
        }
    });

    // Xử lý nút xóa hàng loạt
    $('#deleteSelectedBtn').on('click', deleteSelectedCategories);
    
    // Xử lý nút khôi phục hàng loạt
    $('#restoreSelectedBtn').on('click', function() {
        if (selectedCategories.size === 0) {
            return; // Không làm gì cả
        }
        
        showConfirmModal(
            'Xác nhận khôi phục',
            `Bạn có chắc muốn khôi phục ${selectedCategories.size} danh mục đã chọn?`,
            function() {
                // Create form and submit for bulk restore
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/admin/categories/restore-multiple';
                
                // Add CSRF token
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = '_csrf';
                csrfInput.value = csrfToken;
                form.appendChild(csrfInput);
                
                // Add selected category IDs
                selectedCategories.forEach(categoryId => {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'categoryIds';
                    input.value = categoryId;
                    form.appendChild(input);
                });
                
                document.body.appendChild(form);
                form.submit();
            }
        );
    });

   
    // Xử lý nút xóa vĩnh viễn hàng loạt
    $('#permanentDeleteSelectedBtn').on('click', function(event) {
    event.preventDefault();
    console.log('permanentDeleteSelectedBtn clicked - selectedCategories:', Array.from(selectedCategories));
    
    if (selectedCategories.size === 0) {
        console.log('No categories selected for permanent delete');
        toastr.warning('Vui lòng chọn ít nhất một danh mục để xóa vĩnh viễn!');
        return;
    }
    
    if ($(this).prop('disabled')) {
        console.warn('Button is disabled, click ignored');
        return;
    }
    
    showConfirmModal(
        'Xác nhận xóa vĩnh viễn',
        `Bạn có chắc muốn xóa vĩnh viễn ${selectedCategories.size} danh mục đã chọn? Hành động này không thể hoàn tác!`,
        function() {
            const form = document.getElementById('bulkPermanentDeleteForm');
            if (form) {
                // Xóa các input cũ
                const oldInputs = form.querySelectorAll('input[name="categoryIds"]');
                oldInputs.forEach(input => input.remove());

                // Thêm input categoryIds
                const selectedIds = Array.from(selectedCategories);
                selectedIds.forEach(id => {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'categoryIds';
                    input.value = id;
                    form.appendChild(input);
                });

                // Thêm CSRF token
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
                if (csrfToken) {
                    const csrfInput = document.createElement('input');
                    csrfInput.type = 'hidden';
                    csrfInput.name = '_csrf';
                    csrfInput.value = csrfToken;
                    form.appendChild(csrfInput);
                } else {
                    console.error('CSRF token not found');
                    toastr.error('Không thể xóa vĩnh viễn do lỗi CSRF token.');
                    return;
                }

                console.log('Form data before submit:');
                const formData = new FormData(form);
                for (let [key, value] of formData.entries()) {
                    console.log(`${key}: ${value}`);
                }

                console.log('Submitting bulk permanent delete form with IDs:', selectedIds);
                form.submit();
            } else {
                console.error('Form bulkPermanentDeleteForm not found');
                toastr.error('Không thể xóa vĩnh viễn do lỗi cấu hình form.');
            }
        },
        true
    );
});
    
    // Function đã được định nghĩa ở global scope
}

function filterCategories() {
    const searchTerm = (document.querySelector('.search-input')?.value || '').toLowerCase();
    const statusFilter = document.querySelectorAll('.filter-select')[0]?.value || 'all';
    const sortFilter = document.querySelectorAll('.filter-select')[1]?.value || 'none';

    filteredCategories = categories.filter(cat => {
        const matchesSearch = cat.name.toLowerCase().includes(searchTerm) ||
            cat.description.toLowerCase().includes(searchTerm);
        const matchesStatus = statusFilter === 'all' || (statusFilter === 'active' ? cat.status === 'active' : cat.status !== 'active');
        return matchesSearch && matchesStatus;
    });

    switch (sortFilter) {
        case 'name_asc':
            filteredCategories.sort((a, b) => a.name.localeCompare(b.name, 'vi'));
            break;
        case 'name_desc':
            filteredCategories.sort((a, b) => b.name.localeCompare(a.name, 'vi'));
            break;
        case 'date_newest':
            filteredCategories.sort((a, b) => new Date(b.created) - new Date(a.created));
            break;
        case 'date_oldest':
            filteredCategories.sort((a, b) => new Date(a.created) - new Date(b.created));
            break;
        case 'count_most':
            filteredCategories.sort((a, b) => b.documents - a.documents);
            break;
        case 'count_least':
            filteredCategories.sort((a, b) => a.documents - b.documents);
            break;
        default:
            break;
    }

    currentCatPage = 1;
    // Categories are rendered server-side, no need for client-side rendering
    if (typeof updateCatPagination === 'function') {
        updateCatPagination();
    }
}

function getStatusBadge(status) {
    return status === 'active' ? '<span class="status-badge approved">Hoạt động</span>' : '<span class="status-badge rejected">Vô hiệu hóa</span>';
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('vi-VN');
}

function updateCatPagination() {
    // Categories use server-side pagination, not client-side
    // The pagination is handled by Thymeleaf templates
    console.log('Categories pagination is handled server-side');
    
    // Don't override server-side pagination
    return;
}

function goToCatPage(page) {
    currentCatPage = page;
    // Categories are rendered server-side, no need for client-side rendering
    if (typeof updateCatPagination === 'function') {
        updateCatPagination();
    }
}

function previousCatPage() {
    if (currentCatPage > 1) {
        currentCatPage--;
        // Categories are rendered server-side, no need for client-side rendering
        if (typeof updateCatPagination === 'function') {
            updateCatPagination();
        }
    }
}

function nextCatPage() {
    const totalPages = Math.ceil(filteredCategories.length / categoriesPerPage);
    if (currentCatPage < totalPages) {
        currentCatPage++;
        // Categories are rendered server-side, no need for client-side rendering
        if (typeof updateCatPagination === 'function') {
            updateCatPagination();
        }
    }
}

function toggleCategorySelection(id, isChecked) {
    console.log('toggleCategorySelection - id:', id, 'isChecked:', isChecked, 'type:', typeof id);
    if (Number.isNaN(id)) {
        console.error('Invalid category ID:', id);
        return;
    }
    if (isChecked) {
        selectedCategories.add(id);
    } else {
        selectedCategories.delete(id);
    }
    console.log('Before calling toggleActionButtons - selectedCategories:', Array.from(selectedCategories));
    updateBulkActionButtons(); // hoặc toggleActionButtons()
    console.log('After calling toggleActionButtons - selectedCategories:', Array.from(selectedCategories));
}

function editCategory(id) {
    const cat = categories.find(c => c.id === id);
    if (!cat) return;
    editingCategoryId = id;
    populateCategoryForm(cat);
    openCategoryModal();
}

function openCategoryModal() {
    const modal = document.getElementById('addCategoryModal');
    if (modal) {
        modal.classList.add('open');
        document.body.style.overflow = 'hidden';
    }
}

function clearCategoryForm() {
    document.getElementById('categoryName').value = '';
    document.getElementById('categoryDescription').value = '';
    const iconItems = document.querySelectorAll('.icon-item');
    iconItems.forEach(i => i.classList.remove('selected'));
    if (iconItems[0]) iconItems[0].classList.add('selected');
    const selectedIcon = document.querySelector('.selected-icon i');
    if (selectedIcon) selectedIcon.className = iconItems[0]?.querySelector('i').className || 'fas fa-folder';
    const colorPicker = document.getElementById('categoryColor');
    const colorPreview = document.querySelector('.color-preview');
    const colorValue = document.querySelector('.color-value');
    if (colorPicker) {
        colorPicker.value = '#4361ee';
        if (colorPreview) colorPreview.style.backgroundColor = '#4361ee';
        if (colorValue) colorValue.textContent = '#4361ee';
    }
}

function populateCategoryForm(cat) {
    document.getElementById('categoryName').value = cat.name;
    document.getElementById('categoryDescription').value = cat.description;
    const iconItems = document.querySelectorAll('.icon-item');
    iconItems.forEach(i => i.classList.remove('selected'));
    iconItems.forEach(i => {
        if (i.querySelector('i').className === cat.iconClass) i.classList.add('selected');
    });
    const selectedIcon = document.querySelector('.selected-icon i');
    if (selectedIcon) selectedIcon.className = cat.iconClass;
    const colorPicker = document.getElementById('categoryColor');
    const colorPreview = document.querySelector('.color-preview');
    const colorValue = document.querySelector('.color-value');
    if (colorPicker) {
        colorPicker.value = cat.color;
        if (colorPreview) colorPreview.style.backgroundColor = cat.color;
        if (colorValue) colorValue.textContent = cat.color;
    }
}

function closeAddCategoryModal() {
    const modal = document.getElementById('addCategoryModal');
    if (modal) {
        modal.classList.remove('open');
        document.body.style.overflow = '';
    }
}

function closeModal() {
    closeAddCategoryModal();
}

function handleSaveCategory() {
    const nameInput = document.getElementById('categoryName');
    const descInput = document.getElementById('categoryDescription');
    const colorInput = document.getElementById('categoryColor');
    const selectedIconEl = document.querySelector('.selected-icon i');

    if (!nameInput || !descInput || !colorInput || !selectedIconEl) return;

    const name = nameInput.value.trim();
    if (!name) {
        toastr.error('Vui lòng nhập tên danh mục');
        return;
    }

    let catObj;
    if (editingCategoryId) {
        catObj = categories.find(c => c.id === editingCategoryId);
        if (!catObj) return;
        catObj.name = name;
        catObj.description = descInput.value.trim();
        catObj.iconClass = selectedIconEl.className || 'fas fa-folder';
        catObj.color = colorInput.value;
    } else {
        const newId = categories.length ? Math.max(...categories.map(c => c.id)) + 1 : 1;
        catObj = {
            id: newId,
            name,
            description: descInput.value.trim(),
            iconClass: selectedIconEl.className || 'fas fa-folder',
            color: colorInput.value,
            documents: 0,
            views: 0,
            downloads: 0,
            created: new Date().toISOString().split('T')[0],
            status: 'active'
        };
        categories.push(catObj);
    }

    editingCategoryId = null;
    clearCategoryForm();
    filterCategories();
    closeAddCategoryModal();
    toastr.success('Đã thêm/cập nhật danh mục thành công!');
}

document.addEventListener('DOMContentLoaded', function () {
    // Kiểm tra và hiển thị thông báo từ sessionStorage
    const toastMessage = sessionStorage.getItem('toastMessage');
    const toastType = sessionStorage.getItem('toastType');
    if (toastMessage && toastType) {
        if (toastType === 'success') {
            toastr.success(toastMessage);
        } else if (toastType === 'error') {
            toastr.error(toastMessage);
        }
        // Xóa thông báo sau khi hiển thị
        sessionStorage.removeItem('toastMessage');
        sessionStorage.removeItem('toastType');
    }

    // Admin dropdown toggle
    const adminProfile = document.querySelector('.admin-profile');
    if (adminProfile) {
        adminProfile.addEventListener('click', function () {
            this.classList.toggle('active');
        });
    }

    // View toggle (grid/table)
    const viewOptions = document.querySelectorAll('.view-option');
    const gridView = document.getElementById('gridView');
    const tableView = document.getElementById('tableView');

    viewOptions.forEach(option => {
        option.addEventListener('click', function () {
            viewOptions.forEach(opt => opt.classList.remove('active'));
            this.classList.add('active');

            const viewType = this.getAttribute('data-view');
            if (viewType === 'grid') {
                gridView.style.display = 'block';
                tableView.style.display = 'none';
            } else {
                gridView.style.display = 'none';
                tableView.style.display = 'block';
            }
        });
    });

    // Select all checkboxes
    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.addEventListener('change', function () {
            const visibleCheckboxes = document.querySelectorAll('.category-checkbox:not(#selectAll)');
            visibleCheckboxes.forEach(cb => {
                cb.checked = this.checked;
                const idVal = Number(cb.dataset.categoryId);
                if (!Number.isNaN(idVal)) {
                    toggleCategorySelection(idVal, cb.checked);
                }
            });
            updateSelectAllState();
        });
    }

    // Modal functionality
    const addCategoryBtn = document.getElementById('addCategoryBtn');
    const addCategoryModal = document.getElementById('addCategoryModal');
    const modalOverlay = document.querySelector('.modal-overlay');
    const modalCloseBtn = document.querySelector('.modal-close');
    const cancelBtn = document.querySelector('.cancel-btn');

    if (addCategoryBtn) {
        addCategoryBtn.addEventListener('click', () => {
            editingCategoryId = null;
            clearCategoryForm();
            openCategoryModal();
        });
    }

    if (modalOverlay) {
        modalOverlay.addEventListener('click', closeModal);
    }

    if (modalCloseBtn) {
        modalCloseBtn.addEventListener('click', closeAddCategoryModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    const saveBtn = addCategoryModal?.querySelector('.save-btn');
    if (saveBtn) {
        saveBtn.addEventListener('click', handleSaveCategory);
    }

    // Icon selection
    const iconItems = document.querySelectorAll('.icon-item');
    const selectedIcon = document.querySelector('.selected-icon i');

    iconItems.forEach(item => {
        item.addEventListener('click', function () {
            iconItems.forEach(i => i.classList.remove('selected'));
            this.classList.add('selected');

            const iconClass = this.querySelector('i').className;
            if (selectedIcon) {
                selectedIcon.className = iconClass;
            }
        });
    });

    // Color picker
    const colorPicker = document.getElementById('categoryColor');
    const colorPreview = document.querySelector('.color-preview');
    const colorValue = document.querySelector('.color-value');

    if (colorPicker && colorPreview && colorValue) {
        colorPicker.addEventListener('input', function () {
            const selectedColor = this.value;
            colorPreview.style.backgroundColor = selectedColor;
            colorValue.textContent = selectedColor;
        });
    }

    // Khởi tạo quản lý danh mục
    setupCategoryEventListeners();
    setupCustomModal();
    updateBulkActionButtons(); // Khởi tạo button state
    filterCategories();
    
    // Export functionality - with better timing
    setTimeout(() => {
        const exportBtn = document.getElementById('exportCategoriesBtn');
        console.log('Export button found:', exportBtn); // Debug
        if (exportBtn) {
            console.log('Adding click listener to export button'); // Debug
            exportBtn.addEventListener('click', function(e) {
                console.log('Export button clicked!'); // Debug
                e.preventDefault();
                e.stopPropagation();
                exportCategoriesToCSV();
            });
            
            // Test click programmatically
            console.log('Export button ready for clicks');
        } else {
            console.error('Export button not found! Available buttons:', 
                Array.from(document.querySelectorAll('button')).map(b => b.id || b.className));
        }
    }, 100); // Small delay to ensure DOM is ready
    
    // Backup: Event delegation
    document.body.addEventListener('click', function(e) {
        if (e.target && e.target.id === 'exportCategoriesBtn') {
            console.log('Export button clicked via delegation!'); // Debug
            e.preventDefault();
            e.stopPropagation();
            exportCategoriesToCSV();
        }
    });
    
    // Custom Modal Functions
    function setupCustomModal() {
        const modal = document.getElementById('confirmModal');
        const cancelBtn = document.getElementById('confirmCancel');
        const overlay = modal.querySelector('.modal-overlay');
        
        // Close modal when clicking cancel or overlay
        cancelBtn.addEventListener('click', hideConfirmModal);
        overlay.addEventListener('click', hideConfirmModal);
        
        // Close modal with Escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modal.style.display !== 'none') {
                hideConfirmModal();
            }
        });
    }
});

    // Export Categories to CSV - Fetch all data from server
    async function exportCategoriesToCSV() {
        console.log('Export categories function called'); // Debug
        
        try {
            // Removed loading notification as requested
            
            // Get ALL categories from server (active + inactive + deleted)
            const response = await fetch('/admin/categories?size=1000&tab=all&export=true');
            
            if (!response.ok) {
                throw new Error('Không thể tải dữ liệu từ server');
            }
            
            // Parse HTML response to extract real data
            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            const data = [];
            
            // Header
            data.push([
                'STT', 'Tên danh mục', 'Mô tả', 'Trạng thái', 'Ngày tạo'
            ]);
            
            // Get real data from server response
            const rows = doc.querySelectorAll('.category-card, .category-item, .grid-item');
            console.log('Found server rows:', rows.length); // Debug
            
            if (rows.length === 0) {
                // Fallback: try different selectors
                const tableRows = doc.querySelectorAll('tbody tr, .data-row');
                console.log('Found table rows:', tableRows.length); // Debug
                
                tableRows.forEach((row, index) => {
                    const cells = row.querySelectorAll('td, .cell');
                    if (cells.length >= 3) {
                        const name = cells[1]?.textContent?.trim() || '';
                        const status = cells[2]?.textContent?.trim() || 'Hoạt động';
                        const date = cells[3]?.textContent?.trim() || new Date().toLocaleDateString('vi-VN');
                        
                        if (name && name !== 'Tên danh mục') { // Skip header
                            const rowData = [
                                index,
                                name,
                                '', // Description not available in table
                                status,
                                date
                            ];
                            data.push(rowData);
                        }
                    }
                });
            } else {
                rows.forEach((row, index) => {
                    const name = row.querySelector('.category-name, h3, .title')?.textContent?.trim() || '';
                    const desc = row.querySelector('.category-desc, p, .description')?.textContent?.trim() || '';
                    const status = row.querySelector('.status-badge, .status')?.textContent?.trim() || 'Hoạt động';
                    
                    if (name) {
                        const rowData = [
                            index + 1,
                            name,
                            desc,
                            status,
                            new Date().toLocaleDateString('vi-VN')
                        ];
                        data.push(rowData);
                    }
                });
            }
            
            console.log('Total data rows:', data.length); // Debug
            
            if (data.length <= 1) {
                showCategoryNotification('Không có dữ liệu để xuất!', 'error');
                return;
            }
            
            // Create CSV content
            const csvContent = data.map(row => 
                row.map(cell => `"${cell.toString().replace(/"/g, '""')}"`).join(',')
            ).join('\n');
            
            // Download file
            const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            const url = URL.createObjectURL(blob);
            link.setAttribute('href', url);
            link.setAttribute('download', `categories_export_${new Date().toISOString().split('T')[0]}.csv`);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            showCategoryNotification(`Đã xuất ${data.length - 1} danh mục thành công!`, 'success');
            
        } catch (error) {
            console.error('Export error:', error);
            showCategoryNotification('Lỗi khi xuất dữ liệu: ' + error.message, 'error');
        }
    }

// Notification function with inline CSS for Categories page
function showCategoryNotification(message, type = 'info') {
    // Tạo toast notification với inline CSS
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show`;
    
    // Inline CSS để đảm bảo hiển thị đúng
    const bgColor = type === 'success' ? '#d1e7dd' : '#f8d7da';
    const borderColor = type === 'success' ? '#badbcc' : '#f5c2c7';
    const textColor = type === 'success' ? '#0f5132' : '#842029';
    
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        min-width: 300px;
        padding: 12px 16px;
        margin-bottom: 1rem;
        border: 1px solid ${borderColor};
        border-radius: 8px;
        background-color: ${bgColor};
        color: ${textColor};
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        font-size: 14px;
        line-height: 1.5;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        display: flex;
        align-items: center;
        gap: 8px;
        animation: slideIn 0.3s ease-out;
    `;
    
    // Thêm keyframe animation
    if (!document.getElementById('category-toast-animations')) {
        const style = document.createElement('style');
        style.id = 'category-toast-animations';
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOut {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }
    
    toast.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}" 
           style="color: ${type === 'success' ? '#0f5132' : '#842029'}; margin-right: 8px;"></i>
        <span style="flex: 1;">${message}</span>
        <button type="button" 
                style="background: none; border: none; font-size: 18px; color: ${textColor}; cursor: pointer; padding: 0; margin-left: 8px;"
                onclick="this.parentElement.remove()">&times;</button>
    `;
    
    document.body.appendChild(toast);
    
    // Tự động xóa sau 3 giây với animation
    setTimeout(() => {
        if (toast.parentNode) {
            toast.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }
    }, 3000);
}