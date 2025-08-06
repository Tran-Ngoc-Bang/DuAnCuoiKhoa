/**
 * IP Bans Management
 * Handles all functionality related to the IP bans page
 */
document.addEventListener('DOMContentLoaded', function() {
    // DOM Element Selectors - Updated for new structure
    const selectors = {
        // Search and filters
        searchInput: document.getElementById('searchInput'),
        statusFilter: document.getElementById('statusFilter'),
        typeFilter: document.getElementById('typeFilter'),
        advancedFilterToggle: document.getElementById('advancedFilterToggle'),
        advancedFilterPanel: document.getElementById('advancedFilterPanel'),
        applyFiltersBtn: document.getElementById('applyFiltersBtn'),
        resetFiltersBtn: document.getElementById('resetFiltersBtn'),
        exportBtn: document.getElementById('exportBtn'),
        
        // Advanced filter elements
        advancedTypeFilter: document.getElementById('advancedTypeFilter'),
        createdFromFilter: document.getElementById('createdFromFilter'),
        createdToFilter: document.getElementById('createdToFilter'),
        expiresFromFilter: document.getElementById('expiresFromFilter'),
        expiresToFilter: document.getElementById('expiresToFilter'),
        createdByFilter: document.getElementById('createdByFilter'),
        
        // Data table and bulk actions
        ipBansTable: document.getElementById('ipBansTable'),
        ipBansTableBody: document.getElementById('ipBansTableBody'),
        selectAllCheckbox: document.getElementById('selectAllCheckbox'),
        bulkActionsToolbar: document.getElementById('bulkActionsToolbar'),
        selectedCount: document.getElementById('selectedCount'),
        bulkActivateBtn: document.getElementById('bulkActivateBtn'),
        bulkDeactivateBtn: document.getElementById('bulkDeactivateBtn'),
        bulkDeleteBtn: document.getElementById('bulkDeleteBtn'),
        
        // Pagination
        startItem: document.getElementById('startItem'),
        endItem: document.getElementById('endItem'),
        totalItems: document.getElementById('totalItems'),
        prevPageBtn: document.getElementById('prevPageBtn'),
        nextPageBtn: document.getElementById('nextPageBtn'),
        pageNumbers: document.getElementById('pageNumbers'),
        
        // Add/Edit Modal
        addIpBanBtn: document.getElementById('addIpBanBtn'),
        ipBanModal: document.getElementById('ipBanModal'),
        modalTitle: document.getElementById('modalTitle'),
        closeModalBtn: document.getElementById('closeModalBtn'),
        cancelBtn: document.getElementById('cancelBtn'),
        saveBtn: document.getElementById('saveBtn'),
        ipBanForm: document.getElementById('ipBanForm'),
        
        // Form fields
        ipAddress: document.getElementById('ipAddress'),
        reason: document.getElementById('reason'),
        banType: document.getElementById('banType'),
        expiresAt: document.getElementById('expiresAt'),
        status: document.getElementById('status'),
        
        // Form errors
        ipAddressError: document.getElementById('ipAddressError'),
        reasonError: document.getElementById('reasonError'),
        banTypeError: document.getElementById('banTypeError'),
        expiresAtError: document.getElementById('expiresAtError'),
        
        // Delete Modal
        deleteModal: document.getElementById('deleteModal'),
        closeDeleteModalBtn: document.getElementById('closeDeleteModalBtn'),
        cancelDeleteBtn: document.getElementById('cancelDeleteBtn'),
        confirmDeleteBtn: document.getElementById('confirmDeleteBtn'),
        deleteIpAddress: document.getElementById('deleteIpAddress'),
        
        // Toast container
        toastContainer: document.getElementById('toastContainer')
    };

    // Application state
    const state = {
        ipBans: [], // Will hold all IP ban data
        currentPage: 1,
        itemsPerPage: 10,
        totalPages: 1,
        selectedIpBans: new Set(),
        filters: {
            search: '',
            status: '',
            type: '',
            createdBy: '',
            startDate: '',
            endDate: ''
        },
        isEditing: false,
        currentIpBanId: null
    };

    // Initialize date inputs - set default values if needed
    const today = new Date().toISOString().split('T')[0];
    // Note: We don't set default dates for filters as users may want to see all data initially
    
    // Set up event listeners and initialize
    initEventListeners();
    fetchAndRenderIpBans();

    /**
     * Initialize all event listeners
     */
    function initEventListeners() {
        // Filter toggle
        selectors.advancedFilterToggle.addEventListener('click', toggleFilterPanel);
        
        // Modal controls
        selectors.addIpBanBtn.addEventListener('click', openAddModal);
        selectors.closeModalBtn.addEventListener('click', closeModal);
        selectors.cancelBtn.addEventListener('click', closeModal);
        selectors.saveBtn.addEventListener('click', saveIpBan);
        
        // Delete modal controls
        selectors.closeDeleteModalBtn.addEventListener('click', closeDeleteModal);
        selectors.cancelDeleteBtn.addEventListener('click', closeDeleteModal);
        selectors.confirmDeleteBtn.addEventListener('click', executeDelete);
        
        // Ban type change (show/hide end date based on permanent/temporary)
        selectors.banType.addEventListener('change', handleBanTypeChange);
        
        // Edit and delete buttons (delegate to table)
        selectors.ipBansTable.addEventListener('click', handleTableActions);
        
        // Filter actions
        selectors.applyFiltersBtn.addEventListener('click', applyFilters);
        selectors.resetFiltersBtn.addEventListener('click', resetFilters);
        selectors.exportBtn.addEventListener('click', exportData);
        
        // Search
        selectors.searchInput.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') handleSearch();
        });
    }
    
    /**
     * Toggle filter panel visibility
     */
    function toggleFilterPanel() {
        if (selectors.advancedFilterPanel.style.display === 'none') {
            selectors.advancedFilterPanel.style.display = 'block';
            selectors.advancedFilterToggle.classList.add('active');
        } else {
            selectors.advancedFilterPanel.style.display = 'none';
            selectors.advancedFilterToggle.classList.remove('active');
        }
    }
    
    /**
     * Open modal in add mode
     */
    function openAddModal() {
        state.isEditing = false;
        state.currentIpBanId = null;
        selectors.modalTitle.textContent = 'Thêm IP Ban';
        selectors.ipBanForm.reset();
        
        // Set default selections
        selectors.banType.value = '';
        selectors.status.value = 'active';
        
        // Clear any previous errors
        clearFormErrors();
        
        // Show the modal
        selectors.ipBanModal.classList.add('show');
    }
    
    /**
     * Open modal in edit mode
     * @param {number} id - ID of the IP ban to edit
     */
    function openEditModal(id) {
        state.currentIpBanId = id;
        state.isEditing = true;
        
        // Set modal title
        selectors.modalTitle.textContent = 'Chỉnh sửa IP Ban';
        
        // Get IP ban data (in real app, this would be an API call)
        const ipBan = mockData.find(item => item.id === id);
        
        if (!ipBan) {
            showToast('Không tìm thấy dữ liệu IP ban', 'error');
            return;
        }
        
        // Populate form fields
        selectors.ipAddress.value = ipBan.ipAddress;
        selectors.reason.value = ipBan.reason;
        selectors.banType.value = ipBan.type;
        selectors.status.value = ipBan.status;
        
        // Handle expires_at field
        if (ipBan.expiresAt && ipBan.expiresAt !== 'Không có') {
            // Convert from display format to datetime-local format
            const date = new Date(ipBan.expiresAt);
            const formattedDate = date.toISOString().slice(0, 16);
            selectors.expiresAt.value = formattedDate;
        } else {
            selectors.expiresAt.value = '';
        }
        
        // Show the modal
        selectors.ipBanModal.classList.add('show');
        
        // Clear any previous errors
        clearFormErrors();
    }
    
    /**
     * Close the IP ban modal
     */
    function closeModal() {
        selectors.ipBanModal.classList.remove('show');
        selectors.ipBanForm.reset();
    }
    
    /**
     * Open delete confirmation modal
     * @param {number} id - ID of the IP ban to delete
     */
    function openDeleteModal(id) {
        state.currentIpBanId = id;
        selectors.deleteModal.classList.add('show');
    }
    
    /**
     * Close delete confirmation modal
     */
    function closeDeleteModal() {
        selectors.deleteModal.classList.remove('show');
    }
    
    /**
     * Handle ban type change to show/hide end date
     */
    function handleBanTypeChange() {
        const isPermanent = selectors.banType.value === 'permanent';
        
        // If permanent, disable and hide end date
        // If temporary, enable and show end date
        if (isPermanent) {
            selectors.expiresAt.value = '';
            selectors.expiresAt.disabled = true;
            selectors.expiresAt.parentElement.classList.add('disabled');
        } else {
            selectors.expiresAt.disabled = false;
            selectors.expiresAt.parentElement.classList.remove('disabled');
            
            // If no end date is set and we're switching to temporary, set default end date to 30 days from start
            if (!selectors.expiresAt.value) {
                const startDate = new Date(selectors.startDate.value);
                if (!isNaN(startDate.getTime())) {
                    const endDate = new Date(startDate);
                    endDate.setDate(startDate.getDate() + 30);
                    selectors.expiresAt.value = endDate.toISOString().split('T')[0];
                }
            }
        }
    }
    
    /**
     * Handle table row actions (edit, delete buttons)
     * @param {Event} e - Click event
     */
    function handleTableActions(e) {
        // Check if clicked element is an edit button or its parent
        const editBtn = e.target.closest('.btn-edit');
        if (editBtn) {
            const id = parseInt(editBtn.dataset.id);
            openEditModal(id);
            return;
        }
        
        // Check if clicked element is a delete button or its parent
        const deleteBtn = e.target.closest('.btn-delete');
        if (deleteBtn) {
            const id = parseInt(deleteBtn.dataset.id);
            openDeleteModal(id);
            return;
        }
    }
    
    /**
     * Apply filters and refresh data
     */
    function applyFilters() {
        state.filters.status = selectors.statusFilter.value;
        state.filters.type = selectors.typeFilter.value;
        state.filters.createdBy = selectors.createdByFilter.value;
        state.filters.startDate = selectors.createdFromFilter.value;
        state.filters.endDate = selectors.createdToFilter.value;
        
        state.currentPage = 1; // Reset to first page when filtering
        fetchAndRenderIpBans();
        
        // Close filter panel
        toggleFilterPanel();
    }
    
    /**
     * Reset all filters to default values
     */
    function resetFilters() {
        selectors.statusFilter.value = '';
        selectors.typeFilter.value = '';
        selectors.createdByFilter.value = '';
        selectors.createdFromFilter.value = '';
        selectors.createdToFilter.value = '';
        
        // Reset filter state
        state.filters.status = '';
        state.filters.type = '';
        state.filters.createdBy = '';
        state.filters.startDate = '';
        state.filters.endDate = '';
    }
    
    /**
     * Refresh data without changing filters
     */
    function refreshData() {
        fetchAndRenderIpBans();
    }
    
    /**
     * Handle search button click
     */
    function handleSearch() {
        state.filters.search = selectors.searchInput.value.trim();
        state.currentPage = 1; // Reset to first page when searching
        fetchAndRenderIpBans();
    }
    
    // Placeholder for these functions - will be implemented in next parts
    function saveIpBan() {
        const formData = new FormData(selectors.ipBanForm);
        
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        const ipBanData = {
            ipAddress: formData.get('ipAddress'),
            reason: formData.get('reason'),
            banType: formData.get('banType'),
            expiresAt: formData.get('expiresAt'),
            status: formData.get('status')
        };
        
        if (state.isEditing) {
            // Update existing IP ban
            showToast('Cập nhật IP ban thành công', 'success');
        } else {
            // Create new IP ban
            showToast('Thêm IP ban thành công', 'success');
        }
        
        closeModal();
        fetchAndRenderIpBans();
    }

    function executeDelete() {
        if (!state.currentIpBanId) return;
        
        // In real app, this would be an API call
        showToast('Xóa IP ban thành công', 'success');
        closeDeleteModal();
        fetchAndRenderIpBans();
    }

    /**
     * Validate form data
     * @returns {boolean} - True if form is valid
     */
    function validateForm() {
        let isValid = true;
        clearFormErrors();
        
        // Validate IP address
        const ipAddress = selectors.ipAddress.value.trim();
        if (!ipAddress) {
            showFormError('ipAddress', 'IP Address là bắt buộc');
            isValid = false;
        } else if (!isValidIpAddress(ipAddress)) {
            showFormError('ipAddress', 'IP Address không hợp lệ');
            isValid = false;
        }
        
        // Validate reason
        const reason = selectors.reason.value.trim();
        if (!reason) {
            showFormError('reason', 'Lý do cấm là bắt buộc');
            isValid = false;
        } else if (reason.length < 10) {
            showFormError('reason', 'Lý do cấm phải có ít nhất 10 ký tự');
            isValid = false;
        }
        
        // Validate ban type
        const banType = selectors.banType.value;
        if (!banType) {
            showFormError('banType', 'Loại cấm là bắt buộc');
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Validate IP address format
     * @param {string} ip - IP address to validate
     * @returns {boolean} - True if valid
     */
    function isValidIpAddress(ip) {
        // Support both single IP and CIDR notation
        const ipRegex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(?:\/(?:[0-9]|[1-2][0-9]|3[0-2]))?$/;
        return ipRegex.test(ip);
    }

    /**
     * Fetch and render IP bans data
     * For now using mock data, would be replaced with API call in production
     */
    function fetchAndRenderIpBans() {
        // Mock data based on IP_Bans schema
        const mockData = [
            {
                id: 1,
                ipAddress: '192.168.1.100',
                reason: 'Spam liên tục và gửi nội dung không phù hợp',
                type: 'spam',
                status: 'active',
                createdAt: '2024-01-15T08:30:00',
                expiresAt: '2024-07-15T08:30:00',
                createdBy: 'Nguyễn Văn A'
            },
            {
                id: 2,
                ipAddress: '10.0.0.25',
                reason: 'Tấn công brute force và cố gắng truy cập trái phép',
                type: 'security',
                status: 'active',
                createdAt: '2024-02-10T14:22:00',
                expiresAt: null, // Permanent ban
                createdBy: 'Trần Thị B'
            },
            {
                id: 3,
                ipAddress: '203.0.113.42',
                reason: 'Đăng tải nội dung vi phạm bản quyền',
                type: 'violation',
                status: 'inactive',
                createdAt: '2024-01-08T10:15:00',
                expiresAt: '2024-02-08T10:15:00',
                createdBy: 'Lê Văn C'
            },
            {
                id: 4,
                ipAddress: '172.16.0.0/24',
                reason: 'Subnet được báo cáo có hoạt động lạm dụng',
                type: 'abuse',
                status: 'active',
                createdAt: '2024-03-01T09:00:00',
                expiresAt: '2024-09-01T09:00:00',
                createdBy: 'Phạm Thị D'
            },
            {
                id: 5,
                ipAddress: '198.51.100.15',
                reason: 'Bot tự động download hàng loạt',
                type: 'abuse',
                status: 'active',
                createdAt: '2024-02-20T16:45:00',
                expiresAt: '2024-05-20T16:45:00',
                createdBy: 'Hoàng Văn E'
            }
        ];
        
        // Store original data
        state.originalIpBans = mockData;
        
        // Apply filters and search
        let filteredData = applyFiltersToData(mockData);
        
        // Apply search
        const searchTerm = selectors.searchInput.value.toLowerCase().trim();
        if (searchTerm) {
            filteredData = filteredData.filter(item => 
                item.ipAddress.toLowerCase().includes(searchTerm) ||
                item.reason.toLowerCase().includes(searchTerm) ||
                item.createdBy.toLowerCase().includes(searchTerm)
            );
        }
        
        // Store filtered data
        state.filteredIpBans = filteredData;
        
        // Update pagination info
        updatePaginationInfo();
        
        // Get paginated data for current page
        const paginatedData = paginateData(filteredData);
        
        // Render the table
        renderIpBansTable(paginatedData);
        
        // Render pagination controls
        renderPagination();
        
        // Restore checkbox selections
        restoreCheckboxSelections();
    }
    
    /**
     * Apply filters to the data
     * @param {Array} data - The data to filter
     * @returns {Array} - Filtered data
     */
    function applyFiltersToData(data) {
        return data.filter(ipBan => {
            // Search filter
            if (state.filters.search && 
                !(ipBan.ipAddress.toLowerCase().includes(state.filters.search.toLowerCase()) || 
                  ipBan.reason.toLowerCase().includes(state.filters.search.toLowerCase()))) {
                return false;
            }
            
            // Status filter
            if (state.filters.status && ipBan.status !== state.filters.status) {
                return false;
            }
            
            // Type filter
            if (state.filters.type && ipBan.type !== state.filters.type) {
                return false;
            }
            
            // Creator filter
            if (state.filters.createdBy && ipBan.createdBy !== state.filters.createdBy) {
                return false;
            }
            
            // Date range filters
            if (state.filters.startDate) {
                const filterStartDate = new Date(state.filters.startDate);
                const ipBanStartDate = new Date(ipBan.createdAt);
                
                if (ipBanStartDate < filterStartDate) {
                    return false;
                }
            }
            
            if (state.filters.endDate && ipBan.expiresAt) {
                const filterEndDate = new Date(state.filters.endDate);
                const ipBanEndDate = new Date(ipBan.expiresAt);
                
                if (ipBanEndDate > filterEndDate) {
                    return false;
                }
            }
            
            return true;
        });
    }
    
    /**
     * Update pagination information based on current data and state
     */
    function updatePaginationInfo() {
        state.totalPages = Math.max(1, Math.ceil(state.filteredIpBans.length / state.itemsPerPage));
        
        // Ensure current page is within valid range
        if (state.currentPage > state.totalPages) {
            state.currentPage = state.totalPages;
        }
        
        // Update pagination display info
        const startItem = (state.currentPage - 1) * state.itemsPerPage + 1;
        const endItem = Math.min(startItem + state.itemsPerPage - 1, state.filteredIpBans.length);
        
        selectors.startItem.textContent = state.filteredIpBans.length > 0 ? startItem : 0;
        selectors.endItem.textContent = endItem;
        selectors.totalItems.textContent = state.filteredIpBans.length;
    }
    
    /**
     * Paginate data based on current page and items per page
     * @param {Array} data - The data to paginate
     * @returns {Array} - Paginated data
     */
    function paginateData(data) {
        const startIndex = (state.currentPage - 1) * state.itemsPerPage;
        const endIndex = startIndex + state.itemsPerPage;
        
        return data.slice(startIndex, endIndex);
    }
    
    /**
     * Render the IP bans table with provided data
     * @param {Array} ipBans - Array of IP ban objects to render
     */
    function renderIpBansTable(ipBans) {
        const tbody = selectors.ipBansTableBody;
        tbody.innerHTML = '';
        
        if (ipBans.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="no-data">
                        <div class="no-data-content">
                            <i class="fas fa-ban"></i>
                            <p>Không có dữ liệu IP ban</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }
        
        ipBans.forEach(ipBan => {
            const row = document.createElement('tr');
            
            // Map ban types to display labels and classes
            const typeMapping = {
                'spam': { label: 'Spam', class: 'type-spam' },
                'security': { label: 'Bảo mật', class: 'type-security' },
                'violation': { label: 'Vi phạm', class: 'type-violation' },
                'abuse': { label: 'Lạm dụng', class: 'type-abuse' }
            };
            
            // Map status to display labels and classes
            const statusMapping = {
                'active': { label: 'Hoạt động', class: 'status-active' },
                'inactive': { label: 'Không hoạt động', class: 'status-inactive' },
                'expired': { label: 'Hết hạn', class: 'status-expired' }
            };
            
            const typeInfo = typeMapping[ipBan.type] || { label: ipBan.type, class: 'type-default' };
            const statusInfo = statusMapping[ipBan.status] || { label: ipBan.status, class: 'status-default' };
            
            // Format dates
            const createdDate = new Date(ipBan.createdAt).toLocaleString('vi-VN');
            const expiresDate = ipBan.expiresAt ? new Date(ipBan.expiresAt).toLocaleString('vi-VN') : 'Vĩnh viễn';
            
            // Truncate long reasons for display
            const displayReason = ipBan.reason.length > 50 
                ? ipBan.reason.substring(0, 50) + '...' 
                : ipBan.reason;
            
            row.innerHTML = `
                <td>
                    <input type="checkbox" class="row-checkbox" data-id="${ipBan.id}" 
                           ${state.selectedIpBans.has(ipBan.id) ? 'checked' : ''}>
                </td>
                <td class="ip-address">${ipBan.ipAddress}</td>
                <td class="reason" title="${ipBan.reason}">${displayReason}</td>
                <td><span class="badge ${typeInfo.class}">${typeInfo.label}</span></td>
                <td><span class="status-badge ${statusInfo.class}">${statusInfo.label}</span></td>
                <td class="created-date">${createdDate}</td>
                <td class="expires-date">${expiresDate}</td>
                <td class="created-by">${ipBan.createdBy}</td>
                <td class="actions">
                    <div class="action-buttons">
                        <button class="btn-icon btn-edit" data-id="${ipBan.id}" title="Chỉnh sửa">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-icon btn-delete" data-id="${ipBan.id}" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            `;
            
            tbody.appendChild(row);
        });
        
        // Attach event listeners to action buttons
        attachTableEventListeners();
    }
    
    /**
     * Render pagination controls
     */
    function renderPagination() {
        // Enable/disable prev/next buttons
        selectors.prevPageBtn.disabled = state.currentPage <= 1;
        selectors.nextPageBtn.disabled = state.currentPage >= state.totalPages;
        
        // Generate page number buttons
        const pageNumbersContainer = selectors.pageNumbers;
        pageNumbersContainer.innerHTML = '';
        
        // For simplicity, show up to 5 page numbers
        let startPage = Math.max(1, state.currentPage - 2);
        let endPage = Math.min(state.totalPages, startPage + 4);
        
        // Adjust startPage if we're near the end
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }
        
        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = document.createElement('button');
            pageBtn.textContent = i;
            pageBtn.className = 'page-number';
            if (i === state.currentPage) {
                pageBtn.classList.add('active');
            }
            pageBtn.addEventListener('click', () => changePage(i));
            pageNumbersContainer.appendChild(pageBtn);
        }
        
        // Set up event listeners for pagination buttons if they don't exist yet
        if (!selectors.prevPageBtn.hasEventListener) {
            selectors.prevPageBtn.addEventListener('click', () => changePage(state.currentPage - 1));
            selectors.prevPageBtn.hasEventListener = true;
        }
        
        if (!selectors.nextPageBtn.hasEventListener) {
            selectors.nextPageBtn.addEventListener('click', () => changePage(state.currentPage + 1));
            selectors.nextPageBtn.hasEventListener = true;
        }
        
        if (!selectors.itemsPerPageSelect.hasEventListener) {
            selectors.itemsPerPageSelect.addEventListener('change', changeItemsPerPage);
            selectors.itemsPerPageSelect.hasEventListener = true;
        }
    }
    
    /**
     * Change the current page
     * @param {number} page - The page number to change to
     */
    function changePage(page) {
        if (page < 1 || page > state.totalPages) return;
        
        state.currentPage = page;
        
        // Re-render with the new page
        updatePaginationInfo();
        const paginatedData = paginateData(state.filteredIpBans);
        renderIpBansTable(paginatedData);
        renderPagination();
    }
    
    /**
     * Change the number of items displayed per page
     */
    function changeItemsPerPage() {
        state.itemsPerPage = parseInt(selectors.itemsPerPageSelect.value);
        state.currentPage = 1; // Reset to first page
        
        // Re-render with the new items per page setting
        updatePaginationInfo();
        const paginatedData = paginateData(state.filteredIpBans);
        renderIpBansTable(paginatedData);
        renderPagination();
    }
    
    /**
     * Clear all selected items
     */
    function clearSelection() {
        state.selectedIpBans.clear();
        selectors.selectAllCheckbox.checked = false;
        
        // Hide the bulk actions toolbar
        selectors.bulkActionsToolbar.style.display = 'none';
        
        // Update count display
        selectors.selectedCount.textContent = '0';
        
        // Uncheck all checkboxes
        const checkboxes = document.querySelectorAll('.row-checkbox');
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
        });
    }
    
    /**
     * Restore checkbox selections based on state.selectedIpBans
     */
    function restoreCheckboxSelections() {
        if (state.selectedIpBans.size === 0) return;
        
        const checkboxes = document.querySelectorAll('.row-checkbox');
        checkboxes.forEach(checkbox => {
            const id = parseInt(checkbox.dataset.id);
            checkbox.checked = state.selectedIpBans.has(id);
        });
        
        // Show the bulk actions toolbar if there are selections
        if (state.selectedIpBans.size > 0) {
            selectors.bulkActionsToolbar.style.display = 'flex';
            selectors.selectedCount.textContent = state.selectedIpBans.size;
        }
        
        // Check if all visible items are selected
        const visibleIds = Array.from(checkboxes).map(cb => parseInt(cb.dataset.id));
        const allSelected = visibleIds.every(id => state.selectedIpBans.has(id));
        selectors.selectAllCheckbox.checked = allSelected && visibleIds.length > 0;
    }

    /**
     * Attach event listeners to action buttons in the table
     */
    function attachTableEventListeners() {
        // Edit buttons
        document.querySelectorAll('.btn-edit').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = parseInt(button.getAttribute('data-id'));
                openEditModal(id);
            });
        });
        
        // Delete buttons
        document.querySelectorAll('.btn-delete').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = parseInt(button.getAttribute('data-id'));
                openDeleteModal(id);
            });
        });
        
        // Row checkboxes
        document.querySelectorAll('.row-checkbox').forEach(checkbox => {
            checkbox.addEventListener('change', handleRowCheckboxChange);
        });
    }

    /**
     * Restore checkbox selections after table re-render
     */
    function restoreCheckboxSelections() {
        document.querySelectorAll('.row-checkbox').forEach(checkbox => {
            const id = parseInt(checkbox.getAttribute('data-id'));
            checkbox.checked = state.selectedIpBans.has(id);
        });
        
        updateSelectAllCheckbox();
    }

    /**
     * Handle individual row checkbox change
     * @param {Event} e - Change event
     */
    function handleRowCheckboxChange(e) {
        const checkbox = e.target;
        const id = parseInt(checkbox.getAttribute('data-id'));
        
        if (checkbox.checked) {
            state.selectedIpBans.add(id);
        } else {
            state.selectedIpBans.delete(id);
        }
        
        updateSelectAllCheckbox();
        updateBulkActionButtons();
    }

    /**
     * Update bulk action buttons based on selection
     */
    function updateBulkActionButtons() {
        const hasSelection = state.selectedIpBans.size > 0;
        const bulkDeleteBtn = document.querySelector('.btn-bulk-delete');
        const bulkActivateBtn = document.querySelector('.btn-bulk-activate');
        const bulkDeactivateBtn = document.querySelector('.btn-bulk-deactivate');
        
        if (bulkDeleteBtn) bulkDeleteBtn.disabled = !hasSelection;
        if (bulkActivateBtn) bulkActivateBtn.disabled = !hasSelection;
        if (bulkDeactivateBtn) bulkDeactivateBtn.disabled = !hasSelection;
    }

    /**
     * Clear all selections
     */
    function clearSelection() {
        state.selectedIpBans.clear();
        document.querySelectorAll('.row-checkbox').forEach(checkbox => {
            checkbox.checked = false;
        });
        selectors.selectAllCheckbox.checked = false;
        updateBulkActionButtons();
    }

    /**
     * Open delete confirmation modal
     * @param {number} id - ID of the IP ban to delete
     */
    function openDeleteModal(id) {
        state.currentIpBanId = id;
        
        // Find the IP ban to get its info
        const ipBan = mockData.find(item => item.id === id);
        if (!ipBan) {
            showToast('Không tìm thấy dữ liệu IP ban', 'error');
            return;
        }
        
        // Update modal content with IP ban info
        const modalBody = selectors.deleteModal.querySelector('.modal-body');
        modalBody.innerHTML = `
            <p>Bạn có chắc chắn muốn xóa IP ban cho địa chỉ <strong>${ipBan.ipAddress}</strong>?</p>
            <p class="text-muted">Hành động này không thể hoàn tác.</p>
        `;
        
        // Show the delete modal
        selectors.deleteModal.classList.add('show');
    }

    /**
     * Close delete confirmation modal
     */
    function closeDeleteModal() {
        selectors.deleteModal.classList.remove('show');
        state.currentIpBanId = null;
    }

    /**
     * Clear all form validation errors
     */
    function clearFormErrors() {
        const errorElements = [
            selectors.ipAddressError,
            selectors.reasonError,
            selectors.banTypeError,
            selectors.expiresAtError
        ];
        
        errorElements.forEach(element => {
            if (element) {
                element.textContent = '';
                element.style.display = 'none';
            }
        });
        
        // Remove error styling from form inputs
        const formInputs = selectors.ipBanForm.querySelectorAll('.form-input, .form-textarea, .form-select');
        formInputs.forEach(input => {
            input.classList.remove('error');
        });
    }

    /**
     * Show form validation error
     * @param {string} fieldName - Name of the field with error
     * @param {string} message - Error message to display
     */
    function showFormError(fieldName, message) {
        const errorElement = selectors[fieldName + 'Error'];
        const inputElement = selectors[fieldName];
        
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
        
        if (inputElement) {
            inputElement.classList.add('error');
        }
    }

    /**
     * Export data functionality
     */
    function exportData() {
        showToast('Tính năng xuất dữ liệu đang được phát triển', 'info');
    }
});
