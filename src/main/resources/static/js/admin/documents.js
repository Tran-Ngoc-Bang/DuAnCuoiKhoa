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

    // Modal and form handling methods
    initModalEventListeners() {
        // Modal event listeners will be added here
    }

    initializeModalTabs() {
        // Tab initialization will be added here
    }

    openDocumentModal() {
        // Open modal logic
    }

    handleSaveDocument(e) {
        // Save document logic
    }

    // Placeholder methods for compatibility
    loadDocuments() {
        console.log('Loading documents - placeholder method');
    }

    renderDocuments() {
        console.log('Rendering documents - placeholder method');
    }

    updatePagination() {
        console.log('Updating pagination - placeholder method');
    }

    updateStatistics() {
        console.log('Updating statistics - placeholder method');
    }

    applyFilters() {
        console.log('Applying filters - placeholder method');
    }

    collectFilters() {
        console.log('Collecting filters - placeholder method');
    }

    applyFiltersAndReload() {
        console.log('Applying filters and reload - placeholder method');
    }

    clearAllFilters() {
        console.log('Clearing all filters - placeholder method');
    }

    handleSort(field) {
        console.log('Handling sort for field:', field);
    }

    handleSelectAll(checked) {
        console.log('Handle select all:', checked);
    }

    clearSelectedDocuments() {
        console.log('Clearing selected documents');
    }

    updateUrlParams() {
        console.log('Updating URL params');
    }

    refreshData() {
        console.log('Refreshing data');
    }

    viewDocument(id) {
        window.open(`/admin/documents/${id}`, '_blank');
    }

    editDocument(id) {
        window.location.href = `/admin/documents/${id}/edit`;
    }

    deleteDocument(id) {
        if (confirm('Bạn có chắc chắn muốn xóa tài liệu này?')) {
            console.log('Delete document:', id);
        }
    }
}

// Tag Selector functionality for admin document forms
class AdminTagSelector {
    constructor() {
        this.selectedTags = new Set();
        this.maxTags = 20;
        this.searchTimeout = null;
        this.isLoading = false;
        
        if (this.initializeElements()) {
            this.attachEventListeners();
            this.loadExistingTags();
        }
    }

    initializeElements() {
        this.tagSearchInput = document.getElementById('tagSearchInput');
        this.tagDropdown = document.getElementById('tagDropdown');
        this.tagResults = document.getElementById('tagResults');
        this.selectedTagsContainer = document.getElementById('selectedTags');
        this.tagCountElement = document.getElementById('tagCount');
        this.tagNamesInput = document.getElementById('tagNames');
        this.popularTagsList = document.querySelector('.popular-tags-list');
        
        if (!this.tagSearchInput || !this.tagDropdown || !this.tagResults || 
            !this.selectedTagsContainer || !this.tagCountElement || !this.tagNamesInput) {
            console.warn('Some tag selector elements not found');
            return false;
        }
        
        return true;
    }

    attachEventListeners() {
        if (!this.tagSearchInput) return;

        // Search input events
        this.tagSearchInput.addEventListener('input', (e) => {
            this.handleSearch(e.target.value);
        });

        this.tagSearchInput.addEventListener('focus', () => {
            this.showDropdown();
            if (!this.tagSearchInput.value.trim()) {
                this.loadAllTags();
            }
        });

        this.tagSearchInput.addEventListener('blur', (e) => {
            // Delay hiding to allow clicking on dropdown items
            setTimeout(() => {
                if (!this.tagDropdown.matches(':hover')) {
                    this.hideDropdown();
                }
            }, 200);
        });

        // Click outside to close dropdown
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.tag-search-container')) {
                this.hideDropdown();
            }
        });

        // Popular tags click events
        if (this.popularTagsList) {
            this.popularTagsList.addEventListener('click', (e) => {
                const tagBadge = e.target.closest('.popular-tag-badge');
                if (tagBadge) {
                    const tagName = tagBadge.getAttribute('data-tag');
                    if (tagName) {
                        this.addTag(tagName);
                    }
                }
            });
        }
    }

    loadExistingTags() {
        if (!this.tagNamesInput) return;
        
        const existingTags = this.tagNamesInput.value;
        console.log('Loading existing tags:', existingTags);
        
        if (existingTags && existingTags.trim()) {
            const tags = existingTags.split(',').map(tag => tag.trim()).filter(tag => tag);
            console.log('Parsed tags:', tags);
            
            // Clear placeholder first if we have tags
            if (tags.length > 0 && this.selectedTagsContainer) {
                const placeholder = this.selectedTagsContainer.querySelector('div[style*="color: #6c757d"]');
                if (placeholder) {
                    placeholder.remove();
                }
            }
            
            tags.forEach(tag => {
                if (tag && !this.selectedTags.has(tag)) {
                    this.selectedTags.add(tag);
                    this.renderSelectedTag(tag);
                }
            });
            this.updateTagCount();
            this.updatePopularTags();
        } else {
            // Show placeholder if no existing tags
            this.updateTagCount();
        }
    }

    handleSearch(query) {
        clearTimeout(this.searchTimeout);
        
        if (!query.trim()) {
            this.loadAllTags();
            return;
        }

        this.searchTimeout = setTimeout(() => {
            this.searchTags(query);
        }, 300);
    }

    async searchTags(query) {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading();

        try {
            const response = await fetch(`/admin/documents/tags/search?q=${encodeURIComponent(query)}`);
            if (response.ok) {
                const tags = await response.json();
                this.renderTagResults(tags, query);
            } else {
                this.showError('Lỗi khi tìm kiếm từ khóa');
            }
        } catch (error) {
            console.error('Error searching tags:', error);
            this.showError('Lỗi kết nối');
        } finally {
            this.isLoading = false;
        }
    }

    async loadAllTags() {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading();

        try {
            const response = await fetch('/admin/documents/tags/all');
            if (response.ok) {
                const tags = await response.json();
                this.renderTagResults(tags);
            } else {
                this.showError('Lỗi khi tải danh sách từ khóa');
            }
        } catch (error) {
            console.error('Error loading all tags:', error);
            this.showError('Lỗi kết nối');
        } finally {
            this.isLoading = false;
        }
    }

    renderTagResults(tags, searchQuery = '') {
        if (!this.tagResults) return;

        this.tagResults.innerHTML = '';

        // Add header
        const header = document.createElement('div');
        header.className = 'tag-results-header';
        header.textContent = searchQuery ? `Kết quả tìm kiếm "${searchQuery}"` : 'Tất cả từ khóa';
        this.tagResults.appendChild(header);

        // Add tag items
        if (tags && tags.length > 0) {
            tags.forEach(tag => {
                const tagItem = document.createElement('div');
                tagItem.className = 'tag-result-item';
                if (this.selectedTags.has(tag.name)) {
                    tagItem.classList.add('selected');
                }

                tagItem.innerHTML = `
                    <span class="tag-result-name">${this.highlightMatch(tag.name, searchQuery)}</span>
                    <span class="tag-result-count">${tag.documentCount || 0}</span>
                `;

                tagItem.addEventListener('click', () => {
                    if (!this.selectedTags.has(tag.name)) {
                        this.addTag(tag.name);
                    }
                    this.hideDropdown();
                    this.tagSearchInput.value = '';
                });

                this.tagResults.appendChild(tagItem);
            });
        }

        // Always show create new tag option when searching
        if (searchQuery && searchQuery.trim()) {
            const existingTag = tags && tags.some(tag => tag.name.toLowerCase() === searchQuery.toLowerCase());
            if (!existingTag && !this.selectedTags.has(searchQuery)) {
                const createItem = document.createElement('div');
                createItem.className = 'tag-result-item create-new-tag';
                createItem.innerHTML = `
                    <span><i class="fas fa-plus"></i> Tạo từ khóa mới: "${searchQuery}"</span>
                `;

                createItem.addEventListener('click', () => {
                    this.addTag(searchQuery);
                    this.hideDropdown();
                    this.tagSearchInput.value = '';
                });

                this.tagResults.appendChild(createItem);
            }
        }

        // Show no results message only if no tags and no create option
        if ((!tags || tags.length === 0) && (!searchQuery || !searchQuery.trim())) {
            this.tagResults.innerHTML = `
                <div class="no-results">
                    <i class="fas fa-search"></i>
                    <p>Không tìm thấy từ khóa nào</p>
                </div>
            `;
        }
    }

    highlightMatch(text, query) {
        if (!query) return text;
        
        const regex = new RegExp(`(${query})`, 'gi');
        return text.replace(regex, '<strong>$1</strong>');
    }

    addTag(tagName) {
        if (!tagName || this.selectedTags.has(tagName) || this.selectedTags.size >= this.maxTags) {
            return;
        }

        // Clear placeholder if this is the first tag
        if (this.selectedTags.size === 0 && this.selectedTagsContainer) {
            const placeholder = this.selectedTagsContainer.querySelector('div[style*="color: #6c757d"]');
            if (placeholder) {
                placeholder.remove();
            }
        }

        this.selectedTags.add(tagName);
        this.renderSelectedTag(tagName);
        this.updateTagCount();
        this.updateHiddenInput();
        this.updatePopularTags();
    }

    removeTag(tagName) {
        if (this.selectedTags.has(tagName)) {
            this.selectedTags.delete(tagName);
            this.removeSelectedTagElement(tagName);
            this.updateTagCount();
            this.updateHiddenInput();
            this.updatePopularTags();
        }
    }

    renderSelectedTag(tagName) {
        if (!this.selectedTagsContainer) return;

        // Clear placeholder text if this is the first tag
        if (this.selectedTags.size === 1) {
            const placeholder = this.selectedTagsContainer.querySelector('div[style*="color: #6c757d"]');
            if (placeholder) {
                placeholder.remove();
            }
        }

        const tagElement = document.createElement('span');
        tagElement.className = 'selected-tag new-tag';
        tagElement.setAttribute('data-tag', tagName);
        tagElement.innerHTML = `
            <span>${tagName}</span>
            <span class="remove-tag" onclick="adminTagSelector.removeTag('${tagName}')">&times;</span>
        `;

        this.selectedTagsContainer.appendChild(tagElement);

        // Remove animation class after animation completes
        setTimeout(() => {
            tagElement.classList.remove('new-tag');
        }, 300);
    }

    removeSelectedTagElement(tagName) {
        if (!this.selectedTagsContainer) return;

        const tagElement = this.selectedTagsContainer.querySelector(`[data-tag="${tagName}"]`);
        if (tagElement) {
            tagElement.remove();
        }
    }

    updateTagCount() {
        if (this.tagCountElement) {
            this.tagCountElement.textContent = this.selectedTags.size;
        }
        
        // Update placeholder text
        if (this.selectedTagsContainer) {
            if (this.selectedTags.size === 0) {
                this.selectedTagsContainer.innerHTML = '<div style="color: #6c757d; font-style: italic; padding: 15px;">Chưa có từ khóa nào được chọn</div>';
            }
        }
    }

    updateHiddenInput() {
        if (this.tagNamesInput) {
            this.tagNamesInput.value = Array.from(this.selectedTags).join(', ');
        }
    }

    updatePopularTags() {
        if (!this.popularTagsList) return;

        const popularTags = this.popularTagsList.querySelectorAll('.popular-tag-badge');
        popularTags.forEach(badge => {
            const tagName = badge.getAttribute('data-tag');
            if (this.selectedTags.has(tagName)) {
                badge.style.opacity = '0.5';
                badge.style.pointerEvents = 'none';
            } else {
                badge.style.opacity = '1';
                badge.style.pointerEvents = 'auto';
            }
        });
    }

    showDropdown() {
        if (this.tagDropdown) {
            this.tagDropdown.classList.add('show');
        }
    }

    hideDropdown() {
        if (this.tagDropdown) {
            this.tagDropdown.classList.remove('show');
        }
    }

    showLoading() {
        if (this.tagResults) {
            this.tagResults.innerHTML = `
                <div class="loading-state">
                    <i class="fas fa-spinner fa-spin"></i>
                    <p>Đang tải...</p>
                </div>
            `;
        }
    }

    showError(message) {
        if (this.tagResults) {
            this.tagResults.innerHTML = `
                <div class="error-state">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>${message}</p>
                </div>
            `;
        }
    }
}

// Global function for popular tag clicks (called from HTML)
function addTagFromPopular(tagName) {
    if (window.adminTagSelector) {
        window.adminTagSelector.addTag(tagName);
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Documents Manager if on documents list page
    if (document.getElementById('documentsTable')) {
        window.documentsManager = new DocumentsManager();
        window.documentsManager.init();
    }
    
    // Initialize Tag Selector if on document create/edit pages
    if (document.getElementById('tagSearchInput')) {
        window.adminTagSelector = new AdminTagSelector();
    }
});