
// ===== Category Management Data & Functions =====
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
        alert('Bạn chưa chọn danh mục nào.');
        return;
    }
    if (!confirm(`Bạn có chắc muốn xóa ${selectedCategories.size} danh mục đã chọn?`)) return;
    categories = categories.filter(c => !selectedCategories.has(c.id));
    selectedCategories.clear();
    filterCategories();
    updateSelectAllState();
}
let currentCatPage = 1;
const categoriesPerPage = 6;
// ID của danh mục đang chỉnh sửa; null nếu ở chế độ thêm mới
let editingCategoryId = null;

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
        }
    });
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
    renderCategories();
    updateCatPagination();
}

// function renderCategories() {
//     const gridContainer = document.querySelector('.categories-grid');
//     const tableBody = document.querySelector('#tableView tbody');
//     if (!gridContainer || !tableBody) return;

//     const start = (currentCatPage - 1) * categoriesPerPage;
//     const end = start + categoriesPerPage;
//     const paginated = filteredCategories.slice(start, end);

//     gridContainer.innerHTML = paginated.map(cat => `
//         <div class="category-admin-card ${selectedCategories.has(cat.id) ? 'selected' : ''}" style="--cat-color: ${cat.color}">
//             <div class="category-admin-header">
//                 <div class="category-icon" style="background:${cat.color};">
//                     <i class="${cat.iconClass}"></i>
//                 </div>
//                 <div class="category-admin-actions">
//                     <button class="action-btn" title="Chỉnh sửa" onclick="editCategory(${cat.id})"><i class="fas fa-edit"></i></button>
//                     <button class="action-btn" title="Xóa" onclick="deleteCategory(${cat.id})"><i class="fas fa-trash"></i></button>
//                 </div>
//             </div>
//             <div class="category-admin-content">
//                 <h3 class="category-admin-title">${cat.name}</h3>
//                 <p class="category-admin-description">${cat.description}</p>
//                 <div class="category-stats">
//                     <div class="stat-item"><span class="stat-label">Tài liệu</span><span class="stat-value">${cat.documents.toLocaleString()}</span></div>
//                     <div class="stat-item"><span class="stat-label">Lượt xem</span><span class="stat-value">${cat.views.toLocaleString()}</span></div>
//                     <div class="stat-item"><span class="stat-label">Lượt tải</span><span class="stat-value">${cat.downloads.toLocaleString()}</span></div>
//                 </div>
//                 <div class="category-admin-status">
//                     ${getStatusBadge(cat.status)}
//                     <span class="category-date">Ngày tạo: ${formatDate(cat.created)}</span>
//                 </div>
//             </div>
//         </div>
//     `).join('');

//     tableBody.innerHTML = paginated.map(cat => `
//         <tr>
//             <td><div class="checkbox-wrapper"><input type="checkbox" class="category-checkbox" data-category-id="${cat.id}" ${selectedCategories.has(cat.id) ? 'checked' : ''}><label></label></div></td>
//             <td><div class="category-info"><div class="category-icon" style="background:${cat.color}; width:2.5rem; height:2.5rem; font-size:1rem;"><i class="${cat.iconClass}"></i></div><div class="category-name">${cat.name}</div></div></td>
//             <td>${cat.description.slice(0, 60)}...</td>
//             <td>${cat.documents.toLocaleString()}</td>
//             <td>${cat.views.toLocaleString()}</td>
//             <td>${cat.downloads.toLocaleString()}</td>
//             <td>${formatDate(cat.created)}</td>
//             <td>${getStatusBadge(cat.status)}</td>
//             <td><div class="action-buttons"><button class="action-btn" title="Chỉnh sửa" onclick="editCategory(${cat.id})"><i class="fas fa-edit"></i></button><button class="action-btn" title="Xóa" onclick="deleteCategory(${cat.id})"><i class="fas fa-trash"></i></button></div></td>
//         </tr>
//     `).join('');
// }

function getStatusBadge(status) {
    return status === 'active' ? '<span class="status-badge approved">Hoạt động</span>' : '<span class="status-badge rejected">Vô hiệu hóa</span>';
}
function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('vi-VN');
}
function updateCatPagination() {
    const totalPages = Math.ceil(filteredCategories.length / categoriesPerPage);
    const pagesContainer = document.querySelector('.pagination-pages');
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');
    if (!pagesContainer) return;

    let pagesHTML = '';
    for (let i = 1; i <= totalPages; i++) {
        pagesHTML += `<button class="pagination-btn page-btn ${i === currentCatPage ? 'active' : ''}">${i}</button>`;
    }
    pagesContainer.innerHTML = pagesHTML;

    prevBtn.disabled = currentCatPage === 1;
    nextBtn.disabled = currentCatPage === totalPages || totalPages === 0;
}
function goToCatPage(page) {
    currentCatPage = page;
    renderCategories();
    updateCatPagination();
}
function previousCatPage() {
    if (currentCatPage > 1) {
        currentCatPage--;
        renderCategories();
        updateCatPagination();
    }
}
function nextCatPage() {
    const totalPages = Math.ceil(filteredCategories.length / categoriesPerPage);
    if (currentCatPage < totalPages) {
        currentCatPage++;
        renderCategories();
        updateCatPagination();
    }
}
function toggleCategorySelection(id, isChecked) {
    if (Number.isNaN(id)) return;
    if (isChecked) selectedCategories.add(id); else selectedCategories.delete(id);
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
    // Icon: reset selection to first
    const iconItems = document.querySelectorAll('.icon-item');
    iconItems.forEach(i => i.classList.remove('selected'));
    if (iconItems[0]) iconItems[0].classList.add('selected');
    const selectedIcon = document.querySelector('.selected-icon i');
    if (selectedIcon) selectedIcon.className = iconItems[0]?.querySelector('i').className || 'fas fa-folder';

    // Color reset
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
    // icon
    const iconItems = document.querySelectorAll('.icon-item');
    iconItems.forEach(i => i.classList.remove('selected'));
    iconItems.forEach(i => {
        if (i.querySelector('i').className === cat.iconClass) i.classList.add('selected');
    });
    const selectedIcon = document.querySelector('.selected-icon i');
    if (selectedIcon) selectedIcon.className = cat.iconClass;
    // color
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
// Backward compatibility for existing code using closeModal
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
        alert('Vui lòng nhập tên danh mục');
        return;
    }

    let catObj;
    if (editingCategoryId) {
        // update existing
        catObj = categories.find(c => c.id === editingCategoryId);
        if (!catObj) return;
        catObj.name = name;
        catObj.description = descInput.value.trim();
        catObj.iconClass = selectedIconEl.className || 'fas fa-folder';
        catObj.color = colorInput.value;
        // giữ statistics & created
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

    // reset trạng thái
    editingCategoryId = null;
    clearCategoryForm();

    filterCategories();
    closeAddCategoryModal();
    alert('Đã thêm danh mục mới!');
}

function deleteCategory(id) {
    if (confirm('Bạn có chắc muốn xóa danh mục này?')) {
        categories = categories.filter(c => c.id !== id);
        filterCategories();
    }
}

// Bulk delete button
const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
if (deleteSelectedBtn) {
    deleteSelectedBtn.addEventListener('click', deleteSelectedCategories);
}

// ===== End Category Management =====

document.addEventListener('DOMContentLoaded', function () {
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
    const checkboxes = document.querySelectorAll('.category-checkbox:not(#selectAll)');

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

    // delegate change on individual checkboxes (already handled in body listener) -> just updateSelectAllState
    document.body.addEventListener('change', (e) => {
        if (e.target.matches('.category-checkbox') && e.target.id !== 'selectAll') {
            const cb = e.target;
            const idVal = Number(cb.dataset.categoryId);
            if (!Number.isNaN(idVal)) {
                toggleCategorySelection(idVal, cb.checked);
            }
            updateSelectAllState();
        }
    });

    // Modal functionality
    const addCategoryBtn = document.getElementById('addCategoryBtn');
    const addCategoryModal = document.getElementById('addCategoryModal');
    const modalOverlay = document.querySelector('.modal-overlay');
    const modalCloseBtn = document.querySelector('.modal-close');
    const cancelBtn = document.querySelector('.cancel-btn');

    function openModal() {
        if (addCategoryModal) {
            addCategoryModal.classList.add('open');
            document.body.style.overflow = 'hidden';
        }
    }

    function closeModal() {
        if (addCategoryModal) {
            addCategoryModal.classList.remove('open');
            document.body.style.overflow = '';
        }
    }

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

            if (this.textContent === '5') {
                nextButton.disabled = true;
            } else {
                nextButton.disabled = false;
            }
        });
    });

    // ===== Khởi tạo quản lý danh mục =====
    setupCategoryEventListeners();
    filterCategories();
});