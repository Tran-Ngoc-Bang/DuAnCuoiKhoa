/**
 * Admin Users Management JavaScript
 * Quản lý người dùng - Admin Panel
 * 
 * Chức năng:
 * - Tìm kiếm và lọc người dùng
 * - Thêm/sửa/xóa người dùng
 * - Bulk actions (kích hoạt/khóa/xóa nhiều user)
 * - Export dữ liệu
 * - Quản lý vai trò và trạng thái
 * - Ghi chú admin
 * - Thống kê và báo cáo
 */

document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    // ================ GLOBAL VARIABLES ================
    let currentPage = 1;
    let itemsPerPage = 20;
    let totalUsers = 0;
    let selectedUsers = new Set();
    let currentFilters = {};
    let sortColumn = 'createdAt';
    let sortDirection = 'desc';
    let isBulkOperationInProgress = false;

    // ================ DOM ELEMENTS ================
    const elements = {
        // Tables
        usersTable: document.querySelector('.users-table'),
        tableBody: document.querySelector('.users-table tbody'),
        
        // Search & Filters
        searchInput: document.querySelector('.search-input'),
        roleFilter: document.querySelector('.filter-select'),
        statusFilter: document.querySelectorAll('.filter-select')[1],
        
        // Advanced Filters
        advancedFilterToggle: document.getElementById('advancedFilterToggle'),
        advancedFiltersPanel: document.getElementById('advancedFiltersPanel'),
        applyAdvancedFilters: document.getElementById('applyAdvancedFilters'),
        resetAdvancedFilters: document.getElementById('resetAdvancedFilters'),
        clearAllFilters: document.getElementById('clearAllFilters'),
        
        // Bulk Actions
        selectAllCheckbox: document.getElementById('selectAll'),
        bulkActionsToolbar: document.getElementById('bulkActionsToolbar'),
        selectedCount: document.querySelector('.selected-count'),
        bulkActivateBtn: document.getElementById('bulkActivateBtn'),
        bulkDeactivateBtn: document.getElementById('bulkDeactivateBtn'),
        bulkRoleBtn: document.getElementById('bulkRoleBtn'),
        bulkRoleDropdown: document.getElementById('bulkRoleDropdown'),
        bulkEmailBtn: document.getElementById('bulkEmailBtn'),
        bulkDeleteBtn: document.getElementById('bulkDeleteBtn'),
        bulkClearBtn: document.getElementById('bulkClearBtn'),
        
        // Progress
        bulkProgress: document.getElementById('bulkProgress'),
        progressFill: document.getElementById('progressFill'),
        progressText: document.getElementById('progressText'),
        
        // Action Buttons
        addNewBtn: document.querySelector('.add-new-btn'),
        exportBtn: document.querySelector('.export-btn'),
        
        // Modals
        addUserModal: document.getElementById('addUserModal'),
        editUserModal: document.getElementById('editUserModal'),
        userDetailModal: document.getElementById('userDetailModal'),
        confirmActionModal: document.getElementById('confirmActionModal'),
        
        // Forms
        addUserForm: document.getElementById('addUserForm'),
        editUserForm: document.getElementById('editUserForm'),
        
        // Multi-select
        roleMultiSelect: document.getElementById('roleMultiSelect'),
        roleMultiDropdown: document.getElementById('roleMultiDropdown'),
        statusMultiSelect: document.getElementById('statusMultiSelect'),
        statusMultiDropdown: document.getElementById('statusMultiDropdown')
    };

    // ================ INITIALIZATION ================
    function init() {
        console.log('Initializing Admin Users Management...');
        
        // Initialize event listeners
        initializeEventListeners();
        
        // Initialize advanced filters
        // initializeAdvancedFilters();
        
        // Initialize multi-select dropdowns
        initializeMultiSelect();
        
        // Load initial data
        loadUsers();
        
        // Show existing note indicators
        showNoteIndicators();
        
        console.log('Admin Users Management initialized successfully');
    }

    // ================ EVENT LISTENERS ================
    function initializeEventListeners() {
        // Search functionality
        if (elements.searchInput) {
            elements.searchInput.addEventListener('input', debounce(handleSearch, 300));
        }

        // Filter changes
        if (elements.roleFilter) {
            elements.roleFilter.addEventListener('change', handleFilterChange);
        }
        if (elements.statusFilter) {
            elements.statusFilter.addEventListener('change', handleFilterChange);
        }

        // Advanced filters toggle
        if (elements.advancedFilterToggle) {
            elements.advancedFilterToggle.addEventListener('click', toggleAdvancedFilters);
        }

        // Apply advanced filters
        if (elements.applyAdvancedFilters) {
            elements.applyAdvancedFilters.addEventListener('click', applyAdvancedFilters);
        }

        // Reset filters
        if (elements.resetAdvancedFilters) {
            elements.resetAdvancedFilters.addEventListener('click', resetAdvancedFilters);
        }

        // Clear all filters
        if (elements.clearAllFilters) {
            elements.clearAllFilters.addEventListener('click', clearAllFilters);
        }

        // Select all checkbox
        if (elements.selectAllCheckbox) {
            elements.selectAllCheckbox.addEventListener('change', handleSelectAll);
        }

        // Bulk action buttons
        if (elements.bulkActivateBtn) {
            elements.bulkActivateBtn.addEventListener('click', () => performBulkAction('activate'));
        }
        if (elements.bulkDeactivateBtn) {
            elements.bulkDeactivateBtn.addEventListener('click', () => performBulkAction('deactivate'));
        }
        if (elements.bulkDeleteBtn) {
            elements.bulkDeleteBtn.addEventListener('click', () => performBulkAction('delete'));
        }
        if (elements.bulkEmailBtn) {
            elements.bulkEmailBtn.addEventListener('click', performBulkEmail);
        }
        if (elements.bulkClearBtn) {
            elements.bulkClearBtn.addEventListener('click', clearSelection);
        }

        // Role dropdown
        if (elements.bulkRoleBtn) {
            elements.bulkRoleBtn.addEventListener('click', toggleRoleDropdown);
        }

        // Action buttons
        if (elements.addNewBtn) {
            elements.addNewBtn.addEventListener('click', openAddUserModal);
        }
        if (elements.exportBtn) {
            elements.exportBtn.addEventListener('click', exportUsers);
        }

        // Table sorting
        if (elements.usersTable) {
            elements.usersTable.addEventListener('click', handleTableClick);
        }

        // Modal events
        initializeModalEvents();

        // Form submissions
        if (elements.addUserForm) {
            elements.addUserForm.addEventListener('submit', handleAddUser);
        }
        if (elements.editUserForm) {
            elements.editUserForm.addEventListener('submit', handleEditUser);
        }

        // Confirm action modal
        const confirmYes = document.getElementById('confirmActionYes');
        const confirmNo = document.getElementById('confirmActionNo');
        if (confirmYes) confirmYes.addEventListener('click', confirmAction);
        if (confirmNo) confirmNo.addEventListener('click', closeConfirmModal);
    }

    // ================ SEARCH & FILTERS ================
    function handleSearch() {
        const searchTerm = elements.searchInput.value.trim();
        currentFilters.search = searchTerm;
        currentPage = 1;
        loadUsers();
    }

    function handleFilterChange() {
        currentFilters.role = elements.roleFilter.value;
        currentFilters.status = elements.statusFilter.value;
        currentPage = 1;
        loadUsers();
    }

    function toggleAdvancedFilters() {
        const panel = elements.advancedFiltersPanel;
        const toggle = elements.advancedFilterToggle;
        const icon = toggle.querySelector('.toggle-icon');
        
        if (panel.style.display === 'none' || !panel.style.display) {
            panel.style.display = 'block';
            icon.style.transform = 'rotate(180deg)';
            toggle.classList.add('active');
        } else {
            panel.style.display = 'none';
            icon.style.transform = 'rotate(0deg)';
            toggle.classList.remove('active');
        }
    }

    function applyAdvancedFilters() {
        // Collect all advanced filter values
        const filters = {
            regDateFrom: document.getElementById('regDateFrom')?.value,
            regDateTo: document.getElementById('regDateTo')?.value,
            lastLoginFrom: document.getElementById('lastLoginFrom')?.value,
            lastLoginTo: document.getElementById('lastLoginTo')?.value,
            minDocuments: document.getElementById('minDocuments')?.value,
            minDownloads: document.getElementById('minDownloads')?.value,
            minRating: document.getElementById('minRating')?.value,
            activityStatus: document.getElementById('activityStatus')?.value,
            emailSearch: document.getElementById('emailSearch')?.value,
            fullNameSearch: document.getElementById('fullNameSearch')?.value,
            phoneSearch: document.getElementById('phoneSearch')?.value,
            userIdSearch: document.getElementById('userIdSearch')?.value,
            selectedRoles: getSelectedMultiSelectValues('role'),
            selectedStatuses: getSelectedMultiSelectValues('status')
        };

        // Remove empty filters
        Object.keys(filters).forEach(key => {
            if (!filters[key] || filters[key] === '') {
                delete filters[key];
            }
        });

        currentFilters.advanced = filters;
        currentPage = 1;
        loadUsers();
        
        // Update results count
        updateFilterResultsCount();
    }

    function resetAdvancedFilters() {
        // Reset all advanced filter inputs
        const inputs = elements.advancedFiltersPanel.querySelectorAll('input, select');
        inputs.forEach(input => {
            if (input.type === 'checkbox') {
                input.checked = false;
            } else {
                input.value = '';
            }
        });

        // Clear multi-select displays
        clearMultiSelectDisplay('role');
        clearMultiSelectDisplay('status');

        // Remove advanced filters
        delete currentFilters.advanced;
        currentPage = 1;
        loadUsers();
    }

    function clearAllFilters() {
        // Clear basic filters
        if (elements.searchInput) elements.searchInput.value = '';
        if (elements.roleFilter) elements.roleFilter.value = 'all';
        if (elements.statusFilter) elements.statusFilter.value = 'all';

        // Reset advanced filters
        resetAdvancedFilters();

        // Clear current filters
        currentFilters = {};
        currentPage = 1;
        loadUsers();
    }

    // ================ MULTI-SELECT DROPDOWNS ================
    function initializeMultiSelect() {
        // Role multi-select
        if (elements.roleMultiSelect) {
            elements.roleMultiSelect.addEventListener('click', () => toggleMultiSelect('role'));
        }

        // Status multi-select
        if (elements.statusMultiSelect) {
            elements.statusMultiSelect.addEventListener('click', () => toggleMultiSelect('status'));
        }

        // Close dropdowns when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.multi-select-container')) {
                closeAllMultiSelects();
            }
        });
    }

    function toggleMultiSelect(type) {
        const dropdown = document.getElementById(type + 'MultiDropdown');
        const isOpen = dropdown.style.display === 'block';
        
        closeAllMultiSelects();
        
        if (!isOpen) {
            dropdown.style.display = 'block';
        }
    }

    function closeAllMultiSelects() {
        const dropdowns = document.querySelectorAll('.multi-select-dropdown');
        dropdowns.forEach(dropdown => {
            dropdown.style.display = 'none';
        });
    }

    function getSelectedMultiSelectValues(type) {
        const checkboxes = document.querySelectorAll(`#${type}MultiDropdown input[type="checkbox"]:checked`);
        return Array.from(checkboxes).map(cb => cb.value);
    }

    function clearMultiSelectDisplay(type) {
        const display = document.getElementById(type + 'MultiSelect');
        const placeholder = display.querySelector('.placeholder');
        if (placeholder) {
            placeholder.textContent = `Chọn ${type === 'role' ? 'vai trò' : 'trạng thái'}...`;
        }
    }

    // ================ TABLE OPERATIONS ================
    function handleTableClick(e) {
        const sortableHeader = e.target.closest('.sortable');
        if (sortableHeader) {
            const column = sortableHeader.dataset.sort;
            handleSort(column);
        }

        const actionBtn = e.target.closest('.action-btn');
        if (actionBtn) {
            const row = actionBtn.closest('tr');
            const action = actionBtn.title || actionBtn.getAttribute('data-action');
            
            switch (action) {
                case 'Xem chi tiết':
                    openUserDetailModal(row);
                    break;
                case 'Chỉnh sửa':
                    openEditUserModal(row);
                    break;
                case 'Khóa tài khoản':
                    toggleUserStatus(row);
                    break;
                case 'Xóa':
                    deleteUser(row);
                    break;
            }
        }
    }

    function handleSort(column) {
        if (sortColumn === column) {
            sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            sortColumn = column;
            sortDirection = 'asc';
        }

        // Update sort indicators
        updateSortIndicators(column);
        
        // Reload data with new sort
        loadUsers();
    }

    function updateSortIndicators(activeColumn) {
        const headers = document.querySelectorAll('.sortable');
        headers.forEach(header => {
            const icon = header.querySelector('i');
            if (header.dataset.sort === activeColumn) {
                icon.className = `fas fa-sort-${sortDirection === 'asc' ? 'up' : 'down'}`;
            } else {
                icon.className = 'fas fa-sort';
            }
        });
    }

    // ================ SELECTION & BULK ACTIONS ================
    function handleSelectAll() {
        const checkboxes = document.querySelectorAll('.users-table tbody input[type="checkbox"]');
        const isChecked = elements.selectAllCheckbox.checked;
        
        checkboxes.forEach(checkbox => {
            checkbox.checked = isChecked;
            const userId = checkbox.id;
            
            if (isChecked) {
                selectedUsers.add(userId);
            } else {
                selectedUsers.delete(userId);
            }
        });
        
        updateBulkActionsVisibility();
    }

    function handleUserSelection(checkbox) {
        const userId = checkbox.id;
        
        if (checkbox.checked) {
            selectedUsers.add(userId);
        } else {
            selectedUsers.delete(userId);
        }
        
        // Update select all checkbox
        updateSelectAllCheckbox();
        updateBulkActionsVisibility();
    }

    function updateSelectAllCheckbox() {
        const checkboxes = document.querySelectorAll('.users-table tbody input[type="checkbox"]');
        const checkedCount = document.querySelectorAll('.users-table tbody input[type="checkbox"]:checked').length;
        
        if (checkedCount === 0) {
            elements.selectAllCheckbox.checked = false;
            elements.selectAllCheckbox.indeterminate = false;
        } else if (checkedCount === checkboxes.length) {
            elements.selectAllCheckbox.checked = true;
            elements.selectAllCheckbox.indeterminate = false;
        } else {
            elements.selectAllCheckbox.checked = false;
            elements.selectAllCheckbox.indeterminate = true;
        }
    }

    function updateBulkActionsVisibility() {
        const hasSelection = selectedUsers.size > 0;
        
        if (elements.bulkActionsToolbar) {
            elements.bulkActionsToolbar.style.display = hasSelection ? 'block' : 'none';
        }
        
        if (elements.selectedCount) {
            elements.selectedCount.textContent = selectedUsers.size;
        }
    }

    function clearSelection() {
        selectedUsers.clear();
        const checkboxes = document.querySelectorAll('.users-table tbody input[type="checkbox"]');
        checkboxes.forEach(checkbox => checkbox.checked = false);
        
        if (elements.selectAllCheckbox) {
            elements.selectAllCheckbox.checked = false;
            elements.selectAllCheckbox.indeterminate = false;
        }
        
        updateBulkActionsVisibility();
    }

    function toggleRoleDropdown() {
        const dropdown = elements.bulkRoleDropdown;
        dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
    }

    // ================ BULK OPERATIONS ================
    async function performBulkAction(action) {
        if (selectedUsers.size === 0) {
            showToast('Vui lòng chọn ít nhất một người dùng', 'warning');
            return;
        }

        const actionMessages = {
            activate: 'Kích hoạt',
            deactivate: 'Khóa',
            delete: 'Xóa'
        };

        const message = `Bạn có chắc chắn muốn ${actionMessages[action]} ${selectedUsers.size} người dùng đã chọn?`;
        
        if (!await showConfirmDialog(message)) {
            return;
        }

        try {
            showBulkProgress();
            
            const userIds = Array.from(selectedUsers);
            const total = userIds.length;
            let completed = 0;

            for (const userId of userIds) {
                try {
                    await performSingleBulkAction(userId, action);
                    completed++;
                    updateBulkProgress(completed, total);
                } catch (error) {
                    console.error(`Error processing user ${userId}:`, error);
                }
            }

            hideBulkProgress();
            showToast(`${actionMessages[action]} thành công ${completed}/${total} người dùng`, 'success');
            
            // Reload data
            loadUsers();
            clearSelection();
            
        } catch (error) {
            hideBulkProgress();
            showToast('Có lỗi xảy ra khi thực hiện thao tác hàng loạt', 'error');
            console.error('Bulk action error:', error);
        }
    }

    async function performSingleBulkAction(userId, action) {
        const endpoints = {
            activate: `/api/admin/users/${userId}/activate`,
            deactivate: `/api/admin/users/${userId}/deactivate`,
            delete: `/api/admin/users/${userId}`
        };

        const method = action === 'delete' ? 'DELETE' : 'PUT';
        const url = endpoints[action];

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken()
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return response.json();
    }

    function showBulkProgress() {
        if (elements.bulkProgress) {
            elements.bulkProgress.style.display = 'block';
        }
        isBulkOperationInProgress = true;
    }

    function hideBulkProgress() {
        if (elements.bulkProgress) {
            elements.bulkProgress.style.display = 'none';
        }
        isBulkOperationInProgress = false;
    }

    function updateBulkProgress(completed, total) {
        const percentage = (completed / total) * 100;
        
        if (elements.progressFill) {
            elements.progressFill.style.width = `${percentage}%`;
        }
        
        if (elements.progressText) {
            elements.progressText.textContent = `Đang xử lý... ${completed}/${total}`;
        }
    }

    async function performBulkEmail() {
        if (selectedUsers.size === 0) {
            showToast('Vui lòng chọn ít nhất một người dùng', 'warning');
            return;
        }

        // Open email modal or redirect to bulk email page
        const userIds = Array.from(selectedUsers);
        const queryString = userIds.map(id => `users=${id}`).join('&');
        window.open(`/admin/bulk-email?${queryString}`, '_blank');
    }

    // ================ USER CRUD OPERATIONS ================
    async function loadUsers() {
        try {
            showLoading();
            
            const params = new URLSearchParams({
                page: currentPage - 1,
                size: itemsPerPage,
                sort: sortColumn,
                direction: sortDirection,
                ...currentFilters
            });

            const response = await fetch(`/api/admin/users?${params}`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            
            renderUsersTable(data.content);
            updatePagination(data);
            totalUsers = data.totalElements;
            
            // Update filter results count
            updateFilterResultsCount();
            
        } catch (error) {
            console.error('Error loading users:', error);
            showToast('Có lỗi xảy ra khi tải danh sách người dùng', 'error');
        } finally {
            hideLoading();
        }
    }

    function renderUsersTable(users) {
        if (!elements.tableBody) return;

        elements.tableBody.innerHTML = '';

        users.forEach(user => {
            const row = createUserRow(user);
            elements.tableBody.appendChild(row);
        });

        // Re-attach event listeners
        attachRowEventListeners();
    }

    function createUserRow(user) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <div class="checkbox-wrapper">
                    <input type="checkbox" id="user${user.id}" ${selectedUsers.has(`user${user.id}`) ? 'checked' : ''}>
                    <label for="user${user.id}"></label>
                </div>
            </td>
            <td>
                <div class="user-info">
                    <img src="${user.avatarUrl || '/assets/images/default-avatar.png'}" alt="${user.fullName}" class="user-avatar">
                    <div class="user-details">
                        <div class="user-name">${user.fullName}</div>
                        <div class="user-username">@${user.username} <i class="fas fa-sticky-note note-indicator" title="Có ghi chú" style="display:none;"></i></div>
                    </div>
                </div>
            </td>
            <td>${user.email}</td>
            <td><span class="role-badge ${user.role}">${getRoleDisplayName(user.role)}</span></td>
            <td>${formatDate(user.createdAt)}</td>
            <td>${user.documentCount || 0}</td>
            <td>${user.downloadCount || 0}</td>
            <td><span class="status-badge ${user.status}">${getStatusDisplayName(user.status)}</span></td>
            <td>
                <div class="action-buttons">
                    <button type="button" class="action-btn" title="Xem chi tiết" data-action="view">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button type="button" class="action-btn edit-user-btn" title="Chỉnh sửa" data-action="edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button type="button" class="action-btn" title="${user.status === 'active' ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'}" data-action="toggle-status">
                        <i class="fas fa-${user.status === 'active' ? 'lock' : 'unlock'}"></i>
                    </button>
                    <button type="button" class="action-btn danger" title="Xóa" data-action="delete">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;

        return row;
    }

    function attachRowEventListeners() {
        // Checkbox listeners
        const checkboxes = document.querySelectorAll('.users-table tbody input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', () => handleUserSelection(checkbox));
        });

        // Show note indicators
        showNoteIndicators();
    }

    // ================ MODAL OPERATIONS ================
    function initializeModalEvents() {
        // Add User Modal
        if (elements.addUserModal) {
            const closeBtn = elements.addUserModal.querySelector('.modal-close');
            const cancelBtn = elements.addUserModal.querySelector('.cancel-btn');
            
            if (closeBtn) closeBtn.addEventListener('click', closeAddUserModal);
            if (cancelBtn) cancelBtn.addEventListener('click', closeAddUserModal);
        }

        // Edit User Modal
        if (elements.editUserModal) {
            const closeBtn = elements.editUserModal.querySelector('.modal-close');
            const cancelBtn = elements.editUserModal.querySelector('#cancelEditUser');
            
            if (closeBtn) closeBtn.addEventListener('click', closeEditUserModal);
            if (cancelBtn) cancelBtn.addEventListener('click', closeEditUserModal);
        }

        // User Detail Modal
        if (elements.userDetailModal) {
            const closeBtn = elements.userDetailModal.querySelector('.modal-close');
            if (closeBtn) closeBtn.addEventListener('click', closeUserDetailModal);
        }

        // Close modals when clicking overlay
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-overlay')) {
                closeAllModals();
            }
        });

        // Close modals with Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeAllModals();
            }
        });
    }

    function openAddUserModal() {
        if (elements.addUserModal) {
            elements.addUserModal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            
            // Reset form
            if (elements.addUserForm) {
                elements.addUserForm.reset();
            }
        }
    }

    function closeAddUserModal() {
        if (elements.addUserModal) {
            elements.addUserModal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    function openEditUserModal(row) {
        if (!row || !elements.editUserModal) return;

        const userData = extractUserDataFromRow(row);
        fillEditUserForm(userData);
        
        elements.editUserModal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function closeEditUserModal() {
        if (elements.editUserModal) {
            elements.editUserModal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    function openUserDetailModal(row) {
        if (!row || !elements.userDetailModal) return;

        const userData = extractUserDataFromRow(row);
        fillUserDetailModal(userData);
        
        elements.userDetailModal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function closeUserDetailModal() {
        if (elements.userDetailModal) {
            elements.userDetailModal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    function closeAllModals() {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            modal.style.display = 'none';
        });
        document.body.style.overflow = 'auto';
    }

    // ================ FORM HANDLERS ================
    async function handleAddUser(e) {
        e.preventDefault();
        
        const formData = new FormData(elements.addUserForm);
        const userData = Object.fromEntries(formData.entries());
        
        try {
            const response = await fetch('/api/admin/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(userData)
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            showToast('Thêm người dùng thành công', 'success');
            closeAddUserModal();
            loadUsers();
            
        } catch (error) {
            console.error('Error adding user:', error);
            showToast('Có lỗi xảy ra khi thêm người dùng', 'error');
        }
    }

    async function handleEditUser(e) {
        e.preventDefault();
        
        const formData = new FormData(elements.editUserForm);
        const userData = Object.fromEntries(formData.entries());
        const userId = userData.id;
        
        try {
            const response = await fetch(`/api/admin/users/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify(userData)
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();
            showToast('Cập nhật người dùng thành công', 'success');
            closeEditUserModal();
            loadUsers();
            
        } catch (error) {
            console.error('Error updating user:', error);
            showToast('Có lỗi xảy ra khi cập nhật người dùng', 'error');
        }
    }

    // ================ USER ACTIONS ================
    async function toggleUserStatus(row) {
        const userId = extractUserIdFromRow(row);
        const currentStatus = row.querySelector('.status-badge').classList.contains('active') ? 'active' : 'inactive';
        const newStatus = currentStatus === 'active' ? 'inactive' : 'active';
        const actionText = newStatus === 'active' ? 'Kích hoạt' : 'Khóa';

        if (!await showConfirmDialog(`Bạn có chắc chắn muốn ${actionText.toLowerCase()} tài khoản này?`)) {
            return;
        }

        try {
            const response = await fetch(`/api/admin/users/${userId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify({ status: newStatus })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            showToast(`${actionText} tài khoản thành công`, 'success');
            loadUsers();
            
        } catch (error) {
            console.error('Error toggling user status:', error);
            showToast('Có lỗi xảy ra khi thay đổi trạng thái tài khoản', 'error');
        }
    }

    async function deleteUser(row) {
        const userId = extractUserIdFromRow(row);
        const userName = row.querySelector('.user-name').textContent;

        if (!await showConfirmDialog(`Bạn có chắc chắn muốn xóa người dùng "${userName}"? Hành động này không thể hoàn tác.`)) {
            return;
        }

        try {
            const response = await fetch(`/api/admin/users/${userId}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            showToast('Xóa người dùng thành công', 'success');
            loadUsers();
            
        } catch (error) {
            console.error('Error deleting user:', error);
            showToast('Có lỗi xảy ra khi xóa người dùng', 'error');
        }
    }

    // ================ UTILITY FUNCTIONS ================
    function extractUserDataFromRow(row) {
        const cells = row.querySelectorAll('td');
        return {
            id: extractUserIdFromRow(row),
            fullName: cells[1]?.querySelector('.user-name')?.textContent.trim() || '',
            username: cells[1]?.querySelector('.user-username')?.textContent.replace('@', '').trim() || '',
            email: cells[2]?.textContent.trim() || '',
            role: getRoleFromBadge(cells[3]?.querySelector('.role-badge')),
            status: getStatusFromBadge(cells[7]?.querySelector('.status-badge'))
        };
    }

    function extractUserIdFromRow(row) {
        const checkbox = row.querySelector('input[type="checkbox"]');
        return checkbox ? checkbox.id.replace('user', '') : null;
    }

    function getRoleFromBadge(badge) {
        if (!badge) return 'user';
        if (badge.classList.contains('admin')) return 'admin';
        if (badge.classList.contains('contributor')) return 'contributor';
        return 'user';
    }

    function getStatusFromBadge(badge) {
        if (!badge) return 'active';
        if (badge.classList.contains('inactive')) return 'inactive';
        if (badge.classList.contains('pending')) return 'pending';
        return 'active';
    }

    function getRoleDisplayName(role) {
        const roles = {
            admin: 'Quản trị viên',
            contributor: 'Cộng tác viên',
            user: 'Người dùng'
        };
        return roles[role] || 'Người dùng';
    }

    function getStatusDisplayName(status) {
        const statuses = {
            active: 'Hoạt động',
            inactive: 'Tạm khóa',
            pending: 'Chờ xác nhận'
        };
        return statuses[status] || 'Hoạt động';
    }

    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }

    function fillEditUserForm(userData) {
        if (!elements.editUserForm) return;
        
        const form = elements.editUserForm;
        form.querySelector('#editFullName').value = userData.fullName;
        form.querySelector('#editUsername').value = userData.username;
        form.querySelector('#editEmail').value = userData.email;
        form.querySelector('#editRole').value = userData.role;
        form.querySelector('#editStatus').value = userData.status;
        
        // Add hidden ID field if not exists
        let idField = form.querySelector('#editUserId');
        if (!idField) {
            idField = document.createElement('input');
            idField.type = 'hidden';
            idField.id = 'editUserId';
            idField.name = 'id';
            form.appendChild(idField);
        }
        idField.value = userData.id;
    }

    function fillUserDetailModal(userData) {
        // This would be implemented based on the detail modal structure
        // For now, we'll just log the data
        console.log('Filling user detail modal with:', userData);
    }

    // ================ ADMIN NOTES ================
    function showNoteIndicators() {
        document.querySelectorAll('.users-table tbody tr').forEach(row => {
            const usernameEl = row.querySelector('.user-username');
            if (!usernameEl) return;
            
            const username = usernameEl.textContent.trim().replace('@', '');
            const indicator = row.querySelector('.note-indicator');
            
            if (indicator) {
                const note = localStorage.getItem(`userNotes_${username}`);
                if (note && note.trim() !== '') {
                    indicator.style.display = 'inline-block';
                }
            }
        });
    }

    function saveAdminNote(username, note) {
        localStorage.setItem(`userNotes_${username}`, note);
        
        // Update indicator in table
        document.querySelectorAll('.users-table .user-username').forEach(el => {
            if (el.textContent.trim().replace('@', '') === username) {
                const icon = el.querySelector('.note-indicator');
                if (icon) {
                    icon.style.display = note ? 'inline-block' : 'none';
                }
            }
        });
    }

    // ================ EXPORT FUNCTIONALITY ================
    async function exportUsers() {
        try {
            const params = new URLSearchParams({
                format: 'excel',
                ...currentFilters
            });

            const response = await fetch(`/api/admin/users/export?${params}`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `users_export_${new Date().toISOString().split('T')[0]}.xlsx`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            
            showToast('Xuất dữ liệu thành công', 'success');
            
        } catch (error) {
            console.error('Error exporting users:', error);
            showToast('Có lỗi xảy ra khi xuất dữ liệu', 'error');
        }
    }

    // ================ UI HELPERS ================
    function showLoading() {
        // Add loading overlay or spinner
        const loadingEl = document.createElement('div');
        loadingEl.id = 'loading-overlay';
        loadingEl.innerHTML = '<div class="loading-spinner"></div>';
        loadingEl.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, 0.8);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        `;
        document.body.appendChild(loadingEl);
    }

    function hideLoading() {
        const loadingEl = document.getElementById('loading-overlay');
        if (loadingEl) {
            loadingEl.remove();
        }
    }

    function showToast(message, type = 'info') {
        // Create toast notification
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 12px 20px;
            border-radius: 4px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            animation: slideIn 0.3s ease;
        `;
        
        // Set background color based on type
        const colors = {
            success: '#10b981',
            error: '#ef4444',
            warning: '#f59e0b',
            info: '#3b82f6'
        };
        toast.style.backgroundColor = colors[type] || colors.info;
        
        document.body.appendChild(toast);
        
        // Remove after 3 seconds
        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    async function showConfirmDialog(message) {
        return new Promise((resolve) => {
            const modal = elements.confirmActionModal;
            const messageEl = document.getElementById('confirmActionMessage');
            
            if (messageEl) {
                messageEl.textContent = message;
            }
            
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            
            // Store resolve function
            window.confirmActionResolve = resolve;
        });
    }

    function confirmAction() {
        closeConfirmModal();
        if (window.confirmActionResolve) {
            window.confirmActionResolve(true);
            window.confirmActionResolve = null;
        }
    }

    function closeConfirmModal() {
        const modal = elements.confirmActionModal;
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        
        if (window.confirmActionResolve) {
            window.confirmActionResolve(false);
            window.confirmActionResolve = null;
        }
    }

    function updateFilterResultsCount() {
        const countEl = document.getElementById('filterResultsCount');
        if (countEl) {
            countEl.textContent = `Hiển thị ${totalUsers} người dùng`;
        }
    }

    function updatePagination(data) {
        // Implement pagination controls
        // This would update pagination buttons and info
        console.log('Pagination data:', data);
    }

    function getCsrfToken() {
        const token = document.querySelector('meta[name="_csrf"]');
        return token ? token.getAttribute('content') : '';
    }

    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // ================ INITIALIZE ================
    init();
});

// ================ GLOBAL FUNCTIONS ================
// These functions can be called from HTML or other scripts

// Open user detail modal from HTML
window.openUserDetailModal = function(row) {
    const detailModal = document.getElementById('userDetailModal');
    if (!detailModal || !row) return;
    
    // Get user data from row
    const userName = row.querySelector('.user-name')?.textContent.trim() || '';
    const userUsername = row.querySelector('.user-username')?.textContent.replace('@', '').trim() || '';
    const userEmail = row.cells[2]?.textContent.trim() || '';
    const userRole = row.querySelector('.role-badge')?.textContent.trim() || '';
    const userStatus = row.cells[7]?.textContent.trim() || '';
    
    // Fill modal with user data
    const detailUserName = document.getElementById('detailUserName');
    const detailUserUsername = document.getElementById('detailUserUsername');
    const detailUserEmail = document.getElementById('detailUserEmail');
    const detailUserRole = document.getElementById('detailUserRole');
    const detailUserStatus = document.getElementById('detailUserStatus');
    const adminNotesTextarea = document.getElementById('adminNotesTextarea');
    
    if (detailUserName) detailUserName.textContent = userName;
    if (detailUserUsername) detailUserUsername.textContent = userUsername;
    if (detailUserEmail) detailUserEmail.textContent = userEmail;
    if (detailUserRole) detailUserRole.textContent = userRole;
    if (detailUserStatus) detailUserStatus.textContent = userStatus;
    
    // Load admin notes from localStorage
    const savedNotes = localStorage.getItem('userNotes_' + userUsername) || '';
    if (adminNotesTextarea) {
        adminNotesTextarea.value = savedNotes;
    }
    
    // Open modal
    detailModal.classList.add('open');
    detailModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
};

// Save admin note
window.saveAdminNote = function() {
    const notesTextarea = document.getElementById('adminNotesTextarea');
    const usernameField = document.querySelector('#userDetailModal .user-username');
    
    if (!notesTextarea || !usernameField) return;
    
    const username = usernameField.textContent.trim().replace('@', '');
    const note = notesTextarea.value.trim();
    
    localStorage.setItem('userNotes_' + username, note);

    // Update indicator in list
    document.querySelectorAll('.users-table .user-username').forEach(el => {
        if (el.textContent.trim().replace('@', '') === username) {
            const icon = el.querySelector('.note-indicator');
            if (icon) {
                icon.style.display = note ? 'inline-block' : 'none';
            }
        }
    });
    
    // Show success message
    if (typeof showToast === 'function') {
        showToast('Ghi chú đã được lưu', 'success');
    }
};
