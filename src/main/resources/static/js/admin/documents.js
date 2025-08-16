/**
 * Documents Management JavaScript
 * Quản lý tài liệu trong Admin Panel
 */
class DocumentsManager {
    constructor() {
        this.documents = [];
        this.filteredDocuments = [];
        this.selectedDocuments = new Set();
        this.currentPage = 1;
        this.pageSize = 25;
        this.filters = {
            search: '',
            status: '',
            category: '',
            type: '',
            price: '',
            author: '',
            tags: '',
            dateFrom: '',
            dateTo: '',
            size: '',
            views: '',
            rating: '',
            violation: ''
        };
        this.sortField = 'createdAt';
        this.sortOrder = 'desc';
        this.tabsInitialized = false;
        this._saveHandlerAttached = false;
        this.initializeElements();
    }

    init() {
        const urlParams = new URLSearchParams(window.location.search);
        this.currentPage = parseInt(urlParams.get('page')) + 1 || 1;
        this.pageSize = parseInt(urlParams.get('size')) || parseInt(this.itemsPerPageSelect?.value) || 25;

        console.log('Initializing with page:', this.currentPage, 'size:', this.pageSize);

        this.initializeElements();
        if (!this.documentsTableBody || !this.paginationContainer || !this.firstPageBtn || !this.prevPageBtn || !this.nextPageBtn || !this.lastPageBtn) {
            console.error('Required elements missing. Cannot initialize DocumentsManager.');
            return;
        }

        // Đồng bộ giá trị itemsPerPageSelect với pageSize
        if (this.itemsPerPageSelect) {
            this.itemsPerPageSelect.value = this.pageSize.toString();
            console.log('Set itemsPerPageSelect to:', this.pageSize);
        }

        this.attachEventListeners();
        this.loadDocuments();
        this.updateStatistics();
        this.initModalEventListeners();
        this.initializeModalTabs();
    }

    initializeElements() {
        // Search elements
        this.searchInput = document.getElementById('searchInput');
        this.searchBtn = document.getElementById('searchBtn');

        // Filter elements
        this.advancedFiltersToggle = document.getElementById('advancedFiltersToggle');
        this.advancedFilters = document.getElementById('advancedFilters');
        this.closeFilters = document.getElementById('closeFilters');

        // Filter inputs
        this.statusFilter = document.getElementById('statusFilter');
        this.categoryFilter = document.getElementById('categoryFilter');
        this.typeFilter = document.getElementById('typeFilter');
        this.priceFilter = document.getElementById('priceFilter');
        this.authorFilter = document.getElementById('authorFilter');
        this.tagsFilter = document.getElementById('tagsFilter');
        this.dateFromFilter = document.getElementById('dateFromFilter');
        this.dateToFilter = document.getElementById('dateToFilter');
        this.sizeFilter = document.getElementById('sizeFilter');
        this.viewsFilter = document.getElementById('viewsFilter');
        this.ratingFilter = document.getElementById('ratingFilter');
        this.violationFilter = document.getElementById('violationFilter');

        // Filter actions
        this.applyFiltersBtn = document.getElementById('applyFilters');
        this.clearFiltersBtn = document.getElementById('clearFilters');

        // Table elements
        this.documentsTable = document.getElementById('documentsTable');
        this.documentsTableBody = document.getElementById('documentsTableBody');
        this.selectAll = document.getElementById('selectAll');

        // Bulk actions
        this.bulkActions = document.getElementById('bulkActions');
        this.selectedCount = document.getElementById('selectedCount');
        this.clearSelection = document.getElementById('clearSelection');

        // Pagination
        this.itemsPerPageSelect = document.getElementById('itemsPerPage');
        this.showingFrom = document.getElementById('showingFrom');
        this.showingTo = document.getElementById('showingTo');
        this.totalRecords = document.getElementById('totalRecords');
        this.paginationContainer = document.getElementById('paginationNumbers');
        this.firstPageBtn = document.getElementById('firstPageBtn');
        this.prevPageBtn = document.getElementById('prevPageBtn');
        this.nextPageBtn = document.getElementById('nextPageBtn');
        this.lastPageBtn = document.getElementById('lastPageBtn');

        // Action buttons
        this.addDocumentBtn = document.getElementById('addDocumentBtn');
        this.refreshBtn = document.getElementById('refreshBtn');

        // States
        this.loadingState = document.getElementById('loadingState');
        this.emptyState = document.getElementById('emptyState');
        this.errorState = document.getElementById('errorState');

        // Statistics
        this.totalDocuments = document.getElementById('totalDocuments');
        this.totalViews = document.getElementById('totalViews');
        this.totalDownloads = document.getElementById('totalDownloads');
        this.pendingDocuments = document.getElementById('pendingDocuments');

        // Kiểm tra các phần tử quan trọng
       
    }

    attachEventListeners() {
        console.log('Attaching event listeners');

        // Search
        this.searchInput?.addEventListener('input', this.debounce(() => {
            this.filters.search = this.searchInput.value;
            this.applyFilters();
            this.currentPage = 1;
            this.updateUrlParams();
            this.renderDocuments();
            this.updatePagination();
        }, 300));

        this.searchBtn?.addEventListener('click', () => {
            this.filters.search = this.searchInput.value;
            this.applyFilters();
            this.currentPage = 1;
            this.updateUrlParams();
            this.renderDocuments();
            this.updatePagination();
        });

        // Status filter
        this.statusFilter?.addEventListener('change', () => {
            this.filters.status = this.statusFilter.value;
            this.applyFilters();
            this.currentPage = 1;
            this.updateUrlParams();
            this.renderDocuments();
            this.updatePagination();
        });

        // Items per page
        this.itemsPerPageSelect?.addEventListener('change', () => {
            this.pageSize = parseInt(this.itemsPerPageSelect.value) || 25;
            this.currentPage = 1;
            console.log('Changed pageSize to:', this.pageSize);
            this.updateUrlParams();
            this.renderDocuments();
            this.updatePagination();
        });

        // Advanced filters toggle
        this.advancedFiltersToggle?.addEventListener('click', () => {
            const isVisible = window.getComputedStyle(this.advancedFilters).display !== 'none';
            this.advancedFilters.style.display = isVisible ? 'none' : 'block';
        });

        this.closeFilters?.addEventListener('click', () => {
            this.advancedFilters.style.display = 'none';
        });

        // Filter actions
        this.applyFiltersBtn?.addEventListener('click', () => {
            this.collectFilters();
            this.applyFiltersAndReload();
        });

        this.clearFiltersBtn?.addEventListener('click', () => {
            this.clearAllFilters();
        });

        // Table sorting
        this.documentsTable?.addEventListener('click', (e) => {
            const sortable = e.target.closest('.sortable');
            if (sortable) {
                this.handleSort(sortable.dataset.sort);
            }
        });

        // Select all checkbox
        this.selectAll?.addEventListener('change', (e) => {
            this.handleSelectAll(e.target.checked);
        });

        // Bulk actions
        this.clearSelection?.addEventListener('click', () => {
            this.clearSelectedDocuments();
        });

        // Pagination
        this.firstPageBtn?.addEventListener('click', () => {
            console.log('First page button clicked');
            if (this.currentPage !== 1) {
                this.currentPage = 1;
                this.updateUrlParams();
                this.renderDocuments();
                this.updatePagination();
            }
        });

        this.prevPageBtn?.addEventListener('click', () => {
            console.log('Previous page button clicked');
            if (this.currentPage > 1) {
                this.currentPage--;
                this.updateUrlParams();
                this.renderDocuments();
                this.updatePagination();
            }
        });

        this.nextPageBtn?.addEventListener('click', () => {
            const totalPages = Math.ceil(this.filteredDocuments.length / this.pageSize);
            console.log('Next page button clicked, totalPages:', totalPages);
            if (this.currentPage < totalPages) {
                this.currentPage++;
                this.updateUrlParams();
                this.renderDocuments();
                this.updatePagination();
            }
        });

        this.lastPageBtn?.addEventListener('click', () => {
            const totalPages = Math.ceil(this.filteredDocuments.length / this.pageSize);
            console.log('Last page button clicked, totalPages:', totalPages);
            if (this.currentPage !== totalPages) {
                this.currentPage = totalPages;
                this.updateUrlParams();
                this.renderDocuments();
                this.updatePagination();
            }
        });

        // Actions
        this.addDocumentBtn?.addEventListener('click', () => {
            this.openDocumentModal();
        });

        this.refreshBtn?.addEventListener('click', () => {
            this.refreshData();
        });

        // Attach save handler once
        if (!this._saveHandlerAttached) {
            this.documentForm = document.getElementById('documentForm');
            if (this.documentForm) {
                this.documentForm.addEventListener('submit', (e) => this.handleSaveDocument(e));
                this._saveHandlerAttached = true;
                console.log('Attached submit handler to documentForm');
            }
        }
    }

    debounce(func, wait) {
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

    loadDocuments() {
        console.log('Loading documents');
        this.showLoading();
        
        this.documents = [];
        const rows = this.documentsTableBody ? this.documentsTableBody.querySelectorAll('tr') : [];
        console.log('Rows found:', rows.length);
        if (rows.length === 0) {
            console.warn('No rows found in documentsTableBody');
            this.showEmptyState();
            this.hideLoading();
            this.updatePagination(); // Vẫn cập nhật phân trang để hiển thị trạng thái rỗng
            return;
        }

        rows.forEach(row => {
            console.log('Processing row:', row.outerHTML);
            const checkbox = row.querySelector('.document-checkbox');
            if (!checkbox) {
                console.warn('Skipping row with no checkbox:', row.outerHTML);
                return;
            }

            const title = row.querySelector('.doc-title');
            const desc = row.querySelector('.doc-desc');
            const fileType = row.querySelector('.doc-type');
            const fileSize = row.querySelector('.doc-size');
            const authorName = row.querySelector('.author-name');
            const authorAvatar = row.querySelector('.author-avatar');
            const categoryName = row.querySelector('.category-name');
            const status = row.querySelector('.status-badge');
            const price = row.querySelector('.price-display');
            const views = row.querySelector('.views-count span');
            const downloads = row.querySelector('.downloads-count span');
            const createdAt = row.querySelector('.created-date');
            const thumbnail = row.querySelector('.doc-thumb');

            if (!title || !desc || !fileType || !fileSize || !authorName || !authorAvatar ||
                !categoryName || !status || !price || !views || !downloads || !createdAt || !thumbnail) {
                console.warn('Missing elements in row:', row.outerHTML);
                return;
            }

            try {
                const doc = {
                    id: checkbox.value,
                    title: title.textContent || '',
                    description: desc.textContent || '',
                    fileType: fileType.textContent || '',
                    fileSize: parseFloat(fileSize.textContent.replace(' KB', '')) / 1024 / 1024 || 0, // Convert KB to MB
                    author: {
                        name: authorName.textContent || '',
                        avatar: authorAvatar.src || '/images/default-avatar.jpg'
                    },
                    category: {
                        name: categoryName.textContent || ''
                    },
                    status: status.textContent || '',
                    price: price.textContent.includes('Miễn phí') ? 0 : parseFloat(price.textContent.replace(' coin', '')) || 0,
                    views: parseInt(views.textContent) || 0,
                    downloads: parseInt(downloads.textContent) || 0,
                    createdAt: createdAt.textContent ? new Date(createdAt.textContent) : new Date(),
                    thumbnail: thumbnail.src || '/images/default-thumbnail.jpg',
                    tags: []
                };
                this.documents.push(doc);
            } catch (e) {
                console.error('Error processing row:', row.outerHTML, e);
            }
        });

        console.log('Loaded documents:', this.documents.length, this.documents);
        this.filteredDocuments = [...this.documents];
        this.applyFilters();
        this.renderDocuments();
        this.updatePagination();
        this.hideLoading();
    }

    renderDocuments() {
        if (!this.documentsTableBody) {
            console.error('documentsTableBody is null');
            this.showEmptyState();
            return;
        }

        console.log('Rendering documents for page:', this.currentPage, 'pageSize:', this.pageSize);
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = Math.min(startIndex + this.pageSize, this.filteredDocuments.length);
        const documentsToShow = this.filteredDocuments.slice(startIndex, endIndex);
        console.log(`Rendering: startIndex=${startIndex}, endIndex=${endIndex}, documentsToShow=${documentsToShow.length}`);

        // Ẩn tất cả các hàng
        const rows = this.documentsTableBody.querySelectorAll('tr');
        rows.forEach(row => {
            row.style.display = 'none';
        });

        // Hiển thị các hàng thuộc trang hiện tại
        documentsToShow.forEach(doc => {
            const row = Array.from(rows).find(r => r.querySelector('.document-checkbox')?.value == doc.id);
            if (row) {
                row.style.display = '';
                const checkbox = row.querySelector('.document-checkbox');
                if (checkbox) {
                    checkbox.checked = this.selectedDocuments.has(doc.id);
                    checkbox.addEventListener('change', (e) => {
                        if (e.target.checked) {
                            this.selectedDocuments.add(doc.id);
                        } else {
                            this.selectedDocuments.delete(doc.id);
                        }
                        this.updateSelectionUI();
                    });
                }
                const status = row.querySelector('.status-badge');
                if (status) {
                    status.textContent = this.getStatusText(doc.status);
                    status.className = `status-badge ${this.getStatusClass(doc.status)}`;
                }
                // Gắn sự kiện cho các nút hành động
                const viewBtn = row.querySelector('.action-btn.view');
                const editBtn = row.querySelector('.action-btn.edit');
                const deleteBtn = row.querySelector('.action-btn.delete');
                if (viewBtn) viewBtn.addEventListener('click', () => this.viewDocument(doc.id));
                if (editBtn) editBtn.addEventListener('click', () => this.editDocument(doc.id));
                if (deleteBtn) deleteBtn.addEventListener('click', () => this.deleteDocument(doc.id));
            }
        });

        if (documentsToShow.length === 0) {
            console.log('No documents to show, displaying empty state');
            this.showEmptyState();
        }

        this.updateSelectionUI();
    }

    updatePagination() {
        if (!this.paginationContainer) {
            console.error('paginationContainer is null');
            return;
        }

        console.log('Updating pagination');
        this.paginationContainer.innerHTML = ''; // Xóa các nút phân trang cũ
        const totalDocuments = this.filteredDocuments.length;
        const totalPages = Math.max(1, Math.ceil(totalDocuments / this.pageSize)); // Đảm bảo ít nhất 1 trang

        // Cập nhật thông tin hiển thị
        if (this.showingFrom && this.showingTo && this.totalRecords) {
            const startIndex = totalDocuments > 0 ? (this.currentPage - 1) * this.pageSize + 1 : 0;
            const endIndex = Math.min(this.currentPage * this.pageSize, totalDocuments);
            this.showingFrom.textContent = startIndex;
            this.showingTo.textContent = endIndex;
            this.totalRecords.textContent = totalDocuments;
            console.log(`Pagination info: showing ${startIndex}-${endIndex} of ${totalDocuments} documents, totalPages: ${totalPages}`);
        } else {
            console.warn('One or more of showingFrom, showingTo, totalRecords elements not found');
        }

        // Cập nhật trạng thái các nút tĩnh
        if (this.firstPageBtn && this.prevPageBtn && this.nextPageBtn && this.lastPageBtn) {
            this.firstPageBtn.disabled = this.currentPage === 1;
            this.prevPageBtn.disabled = this.currentPage === 1;
            this.nextPageBtn.disabled = this.currentPage >= totalPages;
            this.lastPageBtn.disabled = this.currentPage >= totalPages;
            console.log('Button states:', {
                first: this.firstPageBtn.disabled,
                prev: this.prevPageBtn.disabled,
                next: this.nextPageBtn.disabled,
                last: this.lastPageBtn.disabled
            });
        } else {
            console.warn('One or more pagination buttons (firstPageBtn, prevPageBtn, nextPageBtn, lastPageBtn) not found');
        }

        // Tạo các nút số trang
        for (let i = 1; i <= totalPages; i++) {
            const pageButton = document.createElement('button');
            pageButton.textContent = i;
            pageButton.className = i === this.currentPage ? 'pagination-btn active' : 'pagination-btn';
            pageButton.addEventListener('click', () => {
                console.log('Page button clicked:', i);
                this.currentPage = i;
                this.updateUrlParams();
                this.renderDocuments();
                this.updatePagination();
            });
            this.paginationContainer.appendChild(pageButton);
        }

        if (totalPages === 1 && totalDocuments === 0) {
            const noPageMessage = document.createElement('span');
            noPageMessage.textContent = 'Không có trang nào';
            this.paginationContainer.appendChild(noPageMessage);
        }
    }

    updateUrlParams() {
        const url = new URL(window.location);
        url.searchParams.set('page', this.currentPage - 1); // Server dùng page bắt đầu từ 0
        url.searchParams.set('size', this.pageSize);
        console.log('Updating URL to:', url.toString());
        window.history.pushState({}, '', url.toString());
    }

    goToPage(page) {
        const totalPages = Math.ceil(this.filteredDocuments.length / this.pageSize);
        if (page < 1 || page > totalPages) return;

        console.log('Going to page:', page);
        this.currentPage = page;
        this.updateUrlParams();
        this.renderDocuments();
        this.updatePagination();
    }

    getStatusClass(status) {
        const statusClasses = {
            'PUBLISHED': 'status-published',
            'PENDING': 'status-pending',
            'REJECTED': 'status-rejected',
            'DRAFT': 'status-draft',
            'ARCHIVED': 'status-archived',
            'SUSPENDED': 'status-suspended'
        };
        return statusClasses[status] || 'status-unknown';
    }

    getStatusText(status) {
        const statusTexts = {
            'PUBLISHED': 'Đã xuất bản',
            'PENDING': 'Chờ duyệt',
            'REJECTED': 'Bị từ chối',
            'DRAFT': 'Bản nháp',
            'ARCHIVED': 'Đã lưu trữ',
            'SUSPENDED': 'Bị đình chỉ'
        };
        return statusTexts[status] || 'Không xác định';
    }

    getStatusBadgeClass(status) {
        const statusClasses = {
            'PUBLISHED': 'bg-success',
            'DRAFT': 'bg-secondary',
            'PENDING': 'bg-warning',
            'REJECTED': 'bg-danger',
            'ARCHIVED': 'bg-dark',
            'SUSPENDED': 'bg-info'
        };
        return statusClasses[status] || 'bg-secondary';
    }

    showEmptyState() {
        if (this.documentsTableBody) {
            this.documentsTableBody.innerHTML = `
                <tr>
                    <td colspan="14" class="empty-state">
                        <div class="empty-content">
                            <i class="fas fa-file-alt"></i>
                            <h3>Không tìm thấy tài liệu</h3>
                            <p>Thử thay đổi bộ lọc hoặc thêm tài liệu mới</p>
                        </div>
                    </td>
                </tr>
            `;
        }
        console.log('Showing empty state');
    }

    showLoading() {
        if (this.loadingState) this.loadingState.style.display = 'block';
        if (this.documentsTableBody) this.documentsTableBody.style.display = 'none';
        if (this.emptyState) this.emptyState.style.display = 'none';
        if (this.errorState) this.errorState.style.display = 'none';
        console.log('Showing loading state');
    }

    hideLoading() {
        if (this.loadingState) this.loadingState.style.display = 'none';
        if (this.documentsTableBody) this.documentsTableBody.style.display = '';
        console.log('Hiding loading state');
    }

    updateStatistics() {
        if (this.totalDocuments) {
            this.totalDocuments.textContent = this.formatNumber(this.filteredDocuments.length);
        }
        if (this.totalViews) {
            const totalViews = this.filteredDocuments.reduce((sum, doc) => sum + (doc.views || 0), 0);
            this.totalViews.textContent = this.formatNumber(totalViews);
        }
        if (this.totalDownloads) {
            const totalDownloads = this.filteredDocuments.reduce((sum, doc) => sum + (doc.downloads || 0), 0);
            this.totalDownloads.textContent = this.formatNumber(totalDownloads);
        }
        if (this.pendingDocuments) {
            const pendingDocs = this.filteredDocuments.filter(doc => doc.status === 'PENDING').length;
            this.pendingDocuments.textContent = this.formatNumber(pendingDocs);
        }
        console.log('Updated statistics');
    }

    formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    collectFilters() {
        this.filters.status = this.statusFilter?.value || '';
        this.filters.category = this.categoryFilter?.value || '';
        this.filters.type = this.typeFilter?.value || '';
        this.filters.price = this.priceFilter?.value || '';
        this.filters.author = this.authorFilter?.value || '';
        this.filters.tags = this.tagsFilter?.value || '';
        this.filters.dateFrom = this.dateFromFilter?.value || '';
        this.filters.dateTo = this.dateToFilter?.value || '';
        this.filters.size = this.sizeFilter?.value || '';
        this.filters.views = this.viewsFilter?.value || '';
        this.filters.rating = this.ratingFilter?.value || '';
        this.filters.violation = this.violationFilter?.checked ? 'true' : '';
        console.log('Collected filters:', this.filters);
    }

    applyFilters() {
        console.log('Applying filters');
        this.filteredDocuments = this.documents.filter(doc => {
            if (this.filters.search) {
                const searchTerm = this.filters.search.toLowerCase();
                if (!doc.title.toLowerCase().includes(searchTerm) &&
                    !doc.description.toLowerCase().includes(searchTerm) &&
                    !doc.author.name.toLowerCase().includes(searchTerm)) {
                    return false;
                }
            }
            if (this.filters.status && doc.status !== this.filters.status) {
                return false;
            }
            if (this.filters.category && doc.category.name !== this.filters.category) {
                return false;
            }
            if (this.filters.type && doc.fileType !== this.filters.type) {
                return false;
            }
            if (this.filters.price) {
                if (this.filters.price === 'free' && doc.price > 0) return false;
                if (this.filters.price === 'paid' && doc.price === 0) return false;
            }
            if (this.filters.author && !doc.author.name.toLowerCase().includes(this.filters.author.toLowerCase())) {
                return false;
            }
            if (this.filters.tags && !doc.tags.some(tag => tag.toLowerCase().includes(this.filters.tags.toLowerCase()))) {
                return false;
            }
            if (this.filters.dateFrom) {
                const docDate = new Date(doc.createdAt);
                const fromDate = new Date(this.filters.dateFrom);
                if (docDate < fromDate) return false;
            }
            if (this.filters.dateTo) {
                const docDate = new Date(doc.createdAt);
                const toDate = new Date(this.filters.dateTo);
                if (docDate > toDate) return false;
            }
            if (this.filters.size) {
                const sizeInMB = doc.fileSize;
                switch (this.filters.size) {
                    case 'small': if (sizeInMB >= 10) return false; break;
                    case 'medium': if (sizeInMB < 10 || sizeInMB >= 50) return false; break;
                    case 'large': if (sizeInMB < 50) return false; break;
                }
            }
            if (this.filters.views) {
                switch (this.filters.views) {
                    case 'low': if (doc.views >= 1000) return false; break;
                    case 'medium': if (doc.views < 1000 || doc.views >= 10000) return false; break;
                    case 'high': if (doc.views < 10000) return false; break;
                }
            }
            if (this.filters.rating) {
                const minRating = parseFloat(this.filters.rating);
                if (doc.rating < minRating) return false;
            }
            if (this.filters.violation === 'true' && !doc.hasReports) return false;
            if (this.filters.violation === 'false' && doc.hasReports) return false;
            return true;
        });

        this.currentPage = 1;
        console.log('Filtered documents:', this.filteredDocuments.length);
    }

    applyFiltersAndReload() {
        this.collectFilters();
        this.applyFilters();
        this.currentPage = 1;
        this.updateUrlParams();
        this.renderDocuments();
        this.updatePagination();
    }

    clearAllFilters() {
        this.filters = {
            search: '',
            status: '',
            category: '',
            type: '',
            price: '',
            author: '',
            tags: '',
            dateFrom: '',
            dateTo: '',
            size: '',
            views: '',
            rating: '',
            violation: ''
        };

        if (this.searchInput) this.searchInput.value = '';
        if (this.statusFilter) this.statusFilter.value = '';
        if (this.categoryFilter) this.categoryFilter.value = '';
        if (this.typeFilter) this.typeFilter.value = '';
        if (this.priceFilter) this.priceFilter.value = '';
        if (this.authorFilter) this.authorFilter.value = '';
        if (this.tagsFilter) this.tagsFilter.value = '';
        if (this.dateFromFilter) this.dateFromFilter.value = '';
        if (this.dateToFilter) this.dateToFilter.value = '';
        if (this.sizeFilter) this.sizeFilter.value = '';
        if (this.viewsFilter) this.viewsFilter.value = '';
        if (this.ratingFilter) this.ratingFilter.value = '';
        if (this.violationFilter) this.violationFilter.checked = false;

        this.advancedFilters.style.display = 'none';
        this.applyFilters();
        this.renderDocuments();
        this.updatePagination();
        console.log('Cleared all filters');
    }

    handleSelectAll(checked) {
        this.selectedDocuments.clear();

        if (checked) {
            const startIndex = (this.currentPage - 1) * this.pageSize;
            const endIndex = Math.min(startIndex + this.pageSize, this.filteredDocuments.length);
            for (let i = startIndex; i < endIndex; i++) {
                this.selectedDocuments.add(this.filteredDocuments[i].id);
            }
        }

        const checkboxes = document.querySelectorAll('.document-checkbox');
        checkboxes.forEach(checkbox => {
            checkbox.checked = checked;
        });

        this.updateSelectionUI();
    }

    clearSelectedDocuments() {
        this.selectedDocuments.clear();
        const checkboxes = document.querySelectorAll('.document-checkbox');
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
        });

        if (this.selectAll) {
            this.selectAll.checked = false;
            this.selectAll.indeterminate = false;
        }

        this.updateSelectionUI();
    }

    getTotalPages() {
        return Math.max(1, Math.ceil(this.filteredDocuments.length / this.pageSize));
    }

	openDocumentModal(doc = null, mode = 'create') {
	    const modal = document.getElementById('documentModal');
	    const modalTitle = modal.querySelector('#documentModalTitle');
	    const form = modal.querySelector('#documentForm');

	    if (!modal || !form) {
	        console.error('Modal hoặc form không tìm thấy');
	        return;
	    }

	    if (modalTitle) {
	        modalTitle.textContent = doc ? 'Chỉnh sửa tài liệu' : 'Thêm tài liệu mới';
	    }

	    // Reset form
	    form.reset();
	    form.querySelector('#documentId').value = '';
	    form.querySelector('#documentStatus').value = 'DRAFT';

	    // Load danh mục (giả định có API lấy danh mục)
	    this.loadCategories(form.querySelector('#documentCategory'));

	    if (doc) {
	        form.querySelector('#documentTitle').value = doc.title || '';
	        form.querySelector('#documentDescription').value = doc.description || '';
	        form.querySelector('#documentCategory').value = doc.category?.id || '';
	        form.querySelector('#documentTags').value = doc.tags ? doc.tags.join(', ') : '';
	        form.querySelector('#documentPrice').value = doc.price || 0;
	        form.querySelector('#documentStatus').value = doc.status || 'DRAFT';
	        form.querySelector('#documentId').value = doc.id || '';
	        // Hiển thị thông tin file và thumbnail nếu có
	        this.showFileInfo({ name: doc.fileType, size: doc.fileSize * 1024 * 1024 }, document.getElementById('fileInfo'));
	        this.showThumbnailPreview({ type: 'image/*', src: doc.thumbnail }, document.getElementById('thumbnailPreview'));
	    }

	    modal.classList.add('active');
	    form.classList.remove('was-validated');
	    this.currentEditingDocument = doc;

	    const firstInput = form.querySelector('#documentTitle');
	    if (firstInput) {
	        setTimeout(() => firstInput.focus(), 100);
	    }
	}
	
	async loadCategories(selectElement) {
	    try {
	        const response = await fetch('/api/categories'); // Giả định có API lấy danh mục
	        const categories = await response.json();
	        selectElement.innerHTML = '<option value="">Chọn danh mục</option>';
	        categories.forEach(category => {
	            const option = document.createElement('option');
	            option.value = category.id;
	            option.textContent = category.name;
	            selectElement.appendChild(option);
	        });
	    } catch (error) {
	        console.error('Lỗi khi tải danh mục:', error);
	        this.showToast('Không thể tải danh sách danh mục', 'error');
	    }
	}

    closeDocumentModal() {
        const modal = document.getElementById('documentModal');
        if (modal) {
            modal.classList.remove('active');
            this.currentEditingDocument = null;
        }
    }

    openDocumentDetailsModal(doc) {
        const modal = document.getElementById('documentDetailsModal');
        if (!modal) {
            console.error('Document details modal not found');
            return;
        }

        this.currentViewingDocument = doc;
        this.populateDocumentDetails(doc);
        this.switchTab(modal, 'general');
        modal.classList.add('active');

        if (!this.tabsInitialized) {
            this.initializeModalTabs();
            this.tabsInitialized = true;
        }

        const editBtn = modal.querySelector('#editDocumentFromDetails');
        if (editBtn && !editBtn.hasAttribute('data-listener-added')) {
            editBtn.addEventListener('click', () => {
                this.closeDocumentDetailsModal();
                this.openDocumentModal(doc);
            });
            editBtn.setAttribute('data-listener-added', 'true');
        }
    }

    closeDocumentDetailsModal() {
        const modal = document.getElementById('documentDetailsModal');
        if (modal) {
            modal.classList.remove('active');
        }
    }

    initModalEventListeners() {
        const documentModal = document.getElementById('documentModal');
        if (documentModal) {
            documentModal.addEventListener('click', (e) => {
                if (e.target === documentModal) {
                    this.closeDocumentModal();
                }
            });

            const closeButtons = documentModal.querySelectorAll('.modal-close, [data-modal-close]');
            closeButtons.forEach(btn => {
                btn.addEventListener('click', () => {
                    this.closeDocumentModal();
                });
            });
        }

        const detailsModal = document.getElementById('documentDetailsModal');
        if (detailsModal) {
            detailsModal.addEventListener('click', (e) => {
                if (e.target === detailsModal) {
                    this.closeDocumentDetailsModal();
                }
            });

            const closeButtons = detailsModal.querySelectorAll('.modal-close, [data-modal-close]');
            closeButtons.forEach(btn => {
                btn.addEventListener('click', () => {
                    this.closeDocumentDetailsModal();
                });
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeDocumentModal();
                this.closeDocumentDetailsModal();
            }
        });
    }

    copyShareUrl() {
        const shareUrlInput = document.querySelector('#shareUrl');
        if (shareUrlInput) {
            shareUrlInput.select();
            shareUrlInput.setSelectionRange(0, 99999);

            try {
                document.execCommand('copy');
                this.showToast('Đã sao chép URL vào clipboard!', 'success');
            } catch (err) {
                console.error('Failed to copy URL:', err);
                this.showToast('Không thể sao chép URL', 'error');
            }
        }
    }

    shareToFacebook(url) {
        const facebookUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
        window.open(facebookUrl, '_blank', 'width=600,height=400');
    }

    shareToTwitter(url, title) {
        const twitterUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(title)}`;
        window.open(twitterUrl, '_blank', 'width=600,height=400');
    }

    shareToWhatsApp(url, title) {
        const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(title + ' ' + url)}`;
        window.open(whatsappUrl, '_blank');
    }

    getCategoryColor(categoryId) {
        const colors = [
            '#4e73df', '#1cc88a', '#F59E0B', '#EF4444',
            '#8B5CF6', '#06B6D4', '#84CC16', '#F97316'
        ];
        return colors[(categoryId - 1) % colors.length];
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    formatDate(date) {
        return new Date(date).toLocaleDateString('vi-VN');
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

	

    refreshData() {
        this.currentPage = 1;
        this.loadDocuments();
        this.updateStatistics();
        this.showToast('Dữ liệu đã được làm mới', 'success');
    }

    handleSort(field) {
        if (this.sortField === field) {
            this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortField = field;
            this.sortOrder = 'asc';
        }

        this.sortDocuments();
        this.renderDocuments();
        this.updateSortIndicators();
    }

    sortDocuments() {
        this.filteredDocuments.sort((a, b) => {
            let valueA, valueB;

            switch (this.sortField) {
                case 'title':
                    valueA = a.title.toLowerCase();
                    valueB = b.title.toLowerCase();
                    break;
                case 'author':
                    valueA = a.author.name.toLowerCase();
                    valueB = b.author.name.toLowerCase();
                    break;
                case 'category':
                    valueA = a.category.name.toLowerCase();
                    valueB = b.category.name.toLowerCase();
                    break;
                case 'status':
                    valueA = a.status;
                    valueB = b.status;
                    break;
                case 'price':
                    valueA = a.price;
                    valueB = b.price;
                    break;
                case 'views':
                    valueA = a.views;
                    valueB = b.views;
                    break;
                case 'downloads':
                    valueA = a.downloads;
                    valueB = b.downloads;
                    break;
                case 'rating':
                    valueA = a.rating;
                    valueB = b.rating;
                    break;
                case 'createdAt':
                    valueA = a.createdAt;
                    valueB = b.createdAt;
                    break;
                default:
                    valueA = a.createdAt;
                    valueB = b.createdAt;
            }

            if (valueA < valueB) return this.sortOrder === 'asc' ? -1 : 1;
            if (valueA > valueB) return this.sortOrder === 'asc' ? 1 : -1;
            return 0;
        });
    }

    updateSortIndicators() {
        document.querySelectorAll('.sortable .sort-indicator').forEach(indicator => {
            indicator.textContent = '↕️';
        });

        const currentSortHeader = document.querySelector(`[data-sort="${this.sortField}"] .sort-indicator`);
        if (currentSortHeader) {
            currentSortHeader.textContent = this.sortOrder === 'asc' ? '↑' : '↓';
        }
    }

    viewDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            this.openDocumentDetailsModal(doc);
        }
    }

    editDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            this.openDocumentModal(doc, 'edit');
        }
    }

    deleteDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            if (confirm(`Bạn có chắc chắn muốn xóa tài liệu "${doc.title}"? Hành động này không thể hoàn tác.`)) {
                this.documents = this.documents.filter(d => d.id !== id);
                this.applyFilters();
                this.updateStatistics();
                this.showToast('Đã xóa tài liệu thành công', 'success');
            }
        }
    }

    duplicateDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            const newDoc = {
                ...doc,
                id: Math.max(...this.documents.map(d => d.id)) + 1,
                title: `${doc.title} (Bản sao)`,
                status: 'DRAFT',
                createdAt: new Date(),
                updatedAt: new Date()
            };
            this.documents.push(newDoc);
            this.applyFilters();
            this.updateStatistics();
            this.showToast('Đã nhân bản tài liệu thành công', 'success');
        }
    }

    shareDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            this.openShareModal(doc);
        }
    }

    archiveDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            if (confirm(`Bạn có chắc chắn muốn lưu trữ tài liệu "${doc.title}"?`)) {
                doc.status = 'ARCHIVED';
                doc.updatedAt = new Date();
                this.applyFilters();
                this.updateStatistics();
                this.showToast('Đã lưu trữ tài liệu thành công', 'success');
            }
        }
    }

    downloadDocument(id) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) {
            this.showToast(`Đang tải xuống "${doc.title}"...`, 'info');
            setTimeout(() => {
                this.showToast('Tải xuống hoàn tất', 'success');
            }, 1500);
        }
    }

    bulkPublish() {
        if (this.selectedDocuments.size === 0) return;

        if (confirm(`Bạn có chắc chắn muốn xuất bản ${this.selectedDocuments.size} tài liệu đã chọn?`)) {
            this.selectedDocuments.forEach(id => {
                const doc = this.documents.find(d => d.id === id);
                if (doc) {
                    doc.status = 'PUBLISHED';
                    doc.updatedAt = new Date();
                }
            });

            this.clearSelectedDocuments();
            this.applyFilters();
            this.updateStatistics();
            this.showToast('Các tài liệu đã được xuất bản', 'success');
        }
    }

    bulkArchive() {
        if (this.selectedDocuments.size === 0) return;

        if (confirm(`Bạn có chắc chắn muốn lưu trữ ${this.selectedDocuments.size} tài liệu đã chọn?`)) {
            this.selectedDocuments.forEach(id => {
                const doc = this.documents.find(d => d.id === id);
                if (doc) {
                    doc.status = 'ARCHIVED';
                    doc.updatedAt = new Date();
                }
            });

            this.clearSelectedDocuments();
            this.applyFilters();
            this.updateStatistics();
            this.showToast('Các tài liệu đã được lưu trữ', 'success');
        }
    }

    bulkDelete() {
        if (this.selectedDocuments.size === 0) return;

        if (confirm(`Bạn có chắc chắn muốn xóa ${this.selectedDocuments.size} tài liệu đã chọn? Hành động này không thể hoàn tác.`)) {
            this.documents = this.documents.filter(doc => !this.selectedDocuments.has(doc.id));
            this.clearSelectedDocuments();
            this.applyFilters();
            this.updateStatistics();
            this.showToast('Các tài liệu đã được xóa', 'success');
        }
    }

    openShareModal(doc) {
        const modal = document.getElementById('shareModal');
        if (!modal) {
            console.error('Share modal not found');
            return;
        }

        const shareUrl = `${window.location.origin}/documents/${doc.id}`;
        modal.querySelector('#shareUrl').value = shareUrl;
        modal.querySelector('#shareTitle').textContent = doc.title;

        modal.classList.add('active');

        if (!modal.dataset.listenersAttached) {
            modal.querySelector('#copyShareUrlBtn').addEventListener('click', () => {
                this.copyShareUrl();
            });

            modal.querySelector('#shareToFacebookBtn').addEventListener('click', () => {
                this.shareToFacebook(shareUrl);
            });

            modal.querySelector('#shareToTwitterBtn').addEventListener('click', () => {
                this.shareToTwitter(shareUrl, doc.title);
            });

            modal.querySelector('#shareToWhatsAppBtn').addEventListener('click', () => {
                this.shareToWhatsApp(shareUrl, doc.title);
            });

            modal.dataset.listenersAttached = 'true';
        }
    }

    showToast(message, type = 'info') {
        let toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
            toastContainer.style.zIndex = '9999';
            document.body.appendChild(toastContainer);
        }

        const toastId = 'toast_' + Date.now();
        const iconClass = {
            'success': 'fas fa-check-circle text-success',
            'error': 'fas fa-exclamation-circle text-danger',
            'warning': 'fas fa-exclamation-triangle text-warning',
            'info': 'fas fa-info-circle text-info'
        }[type] || 'fas fa-info-circle text-info';

        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.id = toastId;
        toast.innerHTML = `
            <div class="toast-header">
                <i class="${iconClass} me-2"></i>
                <strong class="me-auto">Thông báo</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">${message}</div>
        `;

        toastContainer.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();

        toast.addEventListener('hidden.bs.toast', () => {
            toast.remove();
        });
    }

    updateSelectionUI() {
        if (this.selectedCount) {
            this.selectedCount.textContent = this.selectedDocuments.size;
        }
        if (this.bulkActions) {
            this.bulkActions.style.display = this.selectedDocuments.size > 0 ? 'flex' : 'none';
        }
    }

    initializeModalTabs() {
        document.addEventListener('click', (e) => {
            if (e.target.matches('.tab-btn') || e.target.closest('.tab-btn')) {
                const tabBtn = e.target.matches('.tab-btn') ? e.target : e.target.closest('.tab-btn');
                const tabName = tabBtn.dataset.tab;
                const modal = tabBtn.closest('.modal');
                this.switchTab(modal, tabName);
            }
        });

        this.initConditionalFields();
    }

    switchTab(modal, tabName) {
        const tabBtns = modal.querySelectorAll('.tab-btn');
        const tabPanels = modal.querySelectorAll('.tab-panel');

        tabBtns.forEach(btn => btn.classList.remove('active'));
        tabPanels.forEach(panel => panel.classList.remove('active'));

        const activeBtn = modal.querySelector(`[data-tab="${tabName}"]`);
        const activePanel = modal.querySelector(`#tab${tabName.charAt(0).toUpperCase() + tabName.slice(1)}`);

        if (activeBtn) activeBtn.classList.add('active');
        if (activePanel) activePanel.classList.add('active');
    }

    loadTabContent(tabName) {
        const currentDoc = this.currentViewingDocument;
        if (!currentDoc) return;

        switch (tabName) {
            case 'versions':
                this.loadVersionHistory(currentDoc.id);
                break;
            case 'analytics':
                this.loadAnalytics(currentDoc.id);
                break;
            case 'reviews':
                this.loadReviews(currentDoc.id);
                break;
        }
    }

    loadVersionHistory(documentId) {
        console.log('Loading version history for document:', documentId);
    }

    loadAnalytics(documentId) {
        console.log('Loading analytics for document:', documentId);
    }

    loadReviews(documentId) {
        console.log('Loading reviews for document:', documentId);
    }

    populateDocumentDetails(doc) {
        if (!doc) {
            console.error('No document data provided to populateDocumentDetails');
            return;
        }

        console.log('Populating document details for:', doc);

        const thumbnail = document.getElementById('detailThumbnail');
        if (thumbnail) {
            thumbnail.src = doc.thumbnail || 'https://via.placeholder.com/300x200?text=No+Image';
            thumbnail.alt = doc.title || 'Document thumbnail';
        } else {
            console.warn('detailThumbnail element not found');
        }

        this.setElementText('detailTitle', doc.title || 'Không có tiêu đề');
        this.setElementText('detailDescription', doc.description || 'Không có mô tả');
        this.setElementText('detailAuthor', doc.author?.name || 'Không có tác giả');
        this.setElementText('detailCategory', doc.category?.name || 'Không có danh mục');
        this.setElementText('detailFileType', doc.fileType || 'Không xác định');
        this.setElementText('detailPrice', doc.price ? `${doc.price.toLocaleString()} VNĐ` : 'Miễn phí');
        this.setElementText('detailViews', doc.views?.toLocaleString() || '0');
        this.setElementText('detailDownloads', doc.downloads?.toLocaleString() || '0');
        this.setElementText('detailRating', doc.rating ? `${doc.rating}/5 ⭐ (${doc.reviewCount || 0} đánh giá)` : 'Chưa có đánh giá');
        this.setElementText('detailFileSize', doc.fileSize ? `${doc.fileSize} MB` : 'Không xác định');
        this.setElementText('detailCreatedAt', doc.createdAt ? new Date(doc.createdAt).toLocaleDateString('vi-VN') : 'Không xác định');
        this.setElementText('detailUpdatedAt', doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString('vi-VN') : 'Không xác định');

        this.populateTags(doc.tags);
        this.populateStatus(doc.status);
    }

    setElementText(elementId, text) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = text;
        } else {
            console.warn(`Element with id '${elementId}' not found`);
        }
    }

    populateTags(tags) {
        const tagsContainer = document.getElementById('detailTags');
        if (!tagsContainer) {
            console.warn('detailTags element not found');
            return;
        }

        tagsContainer.innerHTML = '';
        if (tags && tags.length > 0) {
            tags.forEach(tag => {
                const tagSpan = document.createElement('span');
                tagSpan.className = 'badge bg-secondary me-1 mb-1';
                tagSpan.textContent = tag;
                tagsContainer.appendChild(tagSpan);
            });
        } else {
            tagsContainer.innerHTML = '<span class="text-muted">Chưa có tags</span>';
        }
    }

    populateStatus(status) {
        const statusElement = document.getElementById('detailStatus');
        if (!statusElement) {
            console.warn('detailStatus element not found');
            return;
        }

        statusElement.textContent = this.getStatusText(status);
        statusElement.className = `badge ${this.getStatusBadgeClass(status)}`;
    }

    initConditionalFields() {
        const scheduleCheckbox = document.getElementById('schedulePublish');
        const scheduleFields = document.getElementById('scheduleFields');
        if (scheduleCheckbox && scheduleFields) {
            scheduleCheckbox.addEventListener('change', () => {
                scheduleFields.style.display = scheduleCheckbox.checked ? 'flex' : 'none';
            });
        }

        const approvalCheckbox = document.getElementById('requireApproval');
        const approvalFields = document.getElementById('approvalFields');
        if (approvalCheckbox && approvalFields) {
            approvalCheckbox.addEventListener('change', () => {
                approvalFields.style.display = approvalCheckbox.checked ? 'flex' : 'none';
            });
        }

        const metaDescription = document.getElementById('metaDescription');
        const metaCharCount = document.getElementById('metaCharCount');
        if (metaDescription && metaCharCount) {
            metaDescription.addEventListener('input', () => {
                const remaining = 160 - metaDescription.value.length;
                metaCharCount.textContent = Math.max(0, remaining);
                metaCharCount.parentElement.classList.toggle('text-warning', remaining < 20);
                metaCharCount.parentElement.classList.toggle('text-danger', remaining < 0);
            });
        }

        this.initFileUploadHandlers();

        const saveDraftBtn = document.getElementById('saveDraftBtn');
        if (saveDraftBtn) {
            saveDraftBtn.addEventListener('click', () => {
                this.saveDraft();
            });
        }
    }

    initFileUploadHandlers() {
        const documentFile = document.getElementById('documentFile');
        const fileInfo = document.getElementById('fileInfo');
        if (documentFile && fileInfo) {
            documentFile.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    this.showFileInfo(file, fileInfo);
                } else {
                    fileInfo.style.display = 'none';
                }
            });

            const removeBtn = fileInfo.querySelector('.btn-remove');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    documentFile.value = '';
                    fileInfo.style.display = 'none';
                });
            }
        }

        const thumbnailFile = document.getElementById('documentThumbnail');
        const thumbnailPreview = document.getElementById('thumbnailPreview');
        if (thumbnailFile && thumbnailPreview) {
            thumbnailFile.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file && file.type.startsWith('image/')) {
                    this.showThumbnailPreview(file, thumbnailPreview);
                } else {
                    thumbnailPreview.style.display = 'none';
                }
            });

            const removeBtn = thumbnailPreview.querySelector('.btn-remove');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    thumbnailFile.value = '';
                    thumbnailPreview.style.display = 'none';
                });
            }
        }
    }

    showFileInfo(file, container) {
        const fileName = container.querySelector('.file-name');
        const fileSize = container.querySelector('.file-size');
        if (fileName) fileName.textContent = file.name;
        if (fileSize) fileSize.textContent = this.formatFileSize(file.size);
        container.style.display = 'flex';
    }

    showThumbnailPreview(file, container) {
        const img = container.querySelector('img');
        if (img) {
            const reader = new FileReader();
            reader.onload = (e) => {
                img.src = e.target.result;
                container.style.display = 'block';
            };
            reader.readAsDataURL(file);
        }
    }

    saveDraft() {
        const form = this.documentForm;
        const statusField = form.querySelector('#documentStatus');
        if (statusField) {
            statusField.value = 'DRAFT';
        }

        const title = form.querySelector('#documentTitle');
        const category = form.querySelector('#documentCategory');
        if (!title.value.trim()) {
            this.showToast('Vui lòng nhập tiêu đề tài liệu', 'error');
            this.switchTab(form.closest('.modal'), 'basic');
            title.focus();
            return;
        }

        if (!category.value) {
            this.showToast('Vui lòng chọn danh mục', 'error');
            this.switchTab(form.closest('.modal'), 'basic');
            category.focus();
            return;
        }

        this.handleSaveDocument({ preventDefault: () => {} });
        this.showToast('Lưu bản nháp thành công', 'success');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('documentsTableBody')) {
        console.log('Initializing DocumentsManager');
        window.documentsManager = new DocumentsManager();
    } else {
        console.error('documentsTableBody not found');
    }
});