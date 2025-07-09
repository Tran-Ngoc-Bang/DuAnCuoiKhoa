/**
 * Documents Management JavaScript
 * Quản lý tài liệu trong Admin Panel
 */

class DocumentsManager {
    constructor() {
        this.documents = [];
        this.filteredDocuments = [];
        this.currentPage = 1;
        this.pageSize = 25; 
        this.sortField = 'created_at';
        this.sortDirection = 'desc';
        this.selectedDocuments = new Set();
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

        this.init();
    }

    init() {
        this.generateMockData();
        this.initializeElements();
        this.attachEventListeners();
        this.loadDocuments();
        this.updateStatistics();
        this.initModalEventListeners();
        this.initializeModalTabs();
    }

    /**
     * Tạo dữ liệu mẫu cho tài liệu
     */
    generateMockData() {
        const statuses = ['PUBLISHED', 'PENDING', 'REJECTED', 'DRAFT', 'ARCHIVED', 'SUSPENDED'];
        const categories = [
            { id: 1, name: 'Công nghệ thông tin' },
            { id: 2, name: 'Kinh tế' },
            { id: 3, name: 'Y học' },
            { id: 4, name: 'Luật' },
            { id: 5, name: 'Giáo dục' }
        ];
        const authors = [
            { id: 1, name: 'Nguyễn Văn A', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ffd8?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
            { id: 2, name: 'Trần Thị B', avatar: 'https://images.unsplash.com/photo-1494790108755-2616b9e86b8c?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
            { id: 3, name: 'Lê Văn C', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
            { id: 4, name: 'Phạm Thị D', avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' },
            { id: 5, name: 'Hoàng Văn E', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80' }
        ];
        const fileTypes = ['PDF', 'DOC', 'PPT', 'XLS', 'TXT'];
        const documentTitles = [
            'Lập trình JavaScript cơ bản',
            'Phân tích kinh tế vĩ mô',
            'Cẩm nang điều trị nội khoa',
            'Luật dân sự Việt Nam',
            'Phương pháp giảng dạy hiện đại',
            'Thuật toán và cấu trúc dữ liệu',
            'Marketing số trong kỷ nguyên 4.0',
            'Chẩn đoán hình ảnh y học',
            'Quy định pháp luật mới nhất',
            'Tâm lý học giáo dục',
            'Phát triển ứng dụng mobile',
            'Quản trị rủi ro tài chính',
            'Y học cổ truyền Việt Nam',
            'Thủ tục hành chính công',
            'Đổi mới giáo dục đại học',
            'Blockchain và cryptocurrency',
            'Phân tích báo cáo tài chính',
            'Điều dưỡng chăm sóc người bệnh',
            'Luật lao động và bảo hiểm xã hội',
            'Công nghệ thông tin trong giáo dục'
        ];

        // Tạo 100 tài liệu mẫu
        for (let i = 1; i <= 100; i++) {
            const category = categories[Math.floor(Math.random() * categories.length)];
            const author = authors[Math.floor(Math.random() * authors.length)];
            const status = statuses[Math.floor(Math.random() * statuses.length)];
            const fileType = fileTypes[Math.floor(Math.random() * fileTypes.length)];
            const title = documentTitles[Math.floor(Math.random() * documentTitles.length)] + ` ${i}`;
            
            const baseDate = new Date('2024-01-01');
            const randomDays = Math.floor(Math.random() * 365);
            const createdAt = new Date(baseDate.getTime() + randomDays * 24 * 60 * 60 * 1000);

            this.documents.push({
                id: i,
                title: title,
                description: `Mô tả chi tiết cho tài liệu ${title}. Đây là nội dung mô tả được tạo tự động để demo.`,
                author: author,
                category: category,
                status: status,
                fileType: fileType,
                price: Math.random() > 0.3 ? Math.floor(Math.random() * 100) + 1 : 0,
                views: Math.floor(Math.random() * 10000),
                downloads: Math.floor(Math.random() * 1000),
                rating: parseFloat((Math.random() * 4 + 1).toFixed(1)),
                reviewCount: Math.floor(Math.random() * 100),
                fileSize: parseFloat((Math.random() * 50 + 0.5).toFixed(1)), // MB
                tags: this.generateRandomTags(),
                thumbnail: this.generateThumbnailUrl(fileType),
                hasReports: Math.random() > 0.8,
                reportCount: Math.random() > 0.8 ? Math.floor(Math.random() * 5) + 1 : 0,
                createdAt: createdAt,
                updatedAt: new Date(createdAt.getTime() + Math.floor(Math.random() * 30) * 24 * 60 * 60 * 1000)
            });
        }

        this.filteredDocuments = [...this.documents];
    }

    /**
     * Tạo tags ngẫu nhiên
     */
    generateRandomTags() {
        const allTags = ['javascript', 'html', 'css', 'react', 'nodejs', 'python', 'java', 'php', 'mysql', 'mongodb', 
                        'kinh tế', 'tài chính', 'marketing', 'quản trị', 'kế toán', 'đầu tư',
                        'y học', 'điều dưỡng', 'dược học', 'chẩn đoán', 'điều trị',
                        'luật', 'pháp lý', 'hợp đồng', 'tố tụng', 'hành chính',
                        'giáo dục', 'sư phạm', 'tâm lý', 'phương pháp', 'đổi mới'];
        
        const numTags = Math.floor(Math.random() * 4) + 1;
        const selectedTags = [];
        
        for (let i = 0; i < numTags; i++) {
            const tag = allTags[Math.floor(Math.random() * allTags.length)];
            if (!selectedTags.includes(tag)) {
                selectedTags.push(tag);
            }
        }
        
        return selectedTags;
    }

    /**
     * Tạo URL thumbnail dựa vào loại file
     */
    generateThumbnailUrl(fileType) {
        const thumbnails = {
            'PDF': 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
            'DOC': 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
            'PPT': 'https://images.unsplash.com/photo-1560472354-b33ff0c44a43?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
            'XLS': 'https://images.unsplash.com/photo-1551288049-bebda4e86b8c?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
            'TXT': 'https://images.unsplash.com/photo-1515378791036-0648a814c963?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
        };
        
        return thumbnails[fileType] || thumbnails['PDF'];
    }

    /**
     * Khởi tạo các element DOM
     */
    initializeElements() {
        // Search elements
        this.searchInput = document.getElementById('searchInput');
        this.searchBtn = document.getElementById('searchBtn');
        
        // Filter elements
        // Sử dụng đúng ID như trong documents.html
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
        this.itemsPerPageElement = document.getElementById('itemsPerPage'); 
        this.showingFrom = document.getElementById('showingFrom');
        this.showingTo = document.getElementById('showingTo');
        this.totalRecords = document.getElementById('totalRecords');
        this.paginationNumbers = document.getElementById('paginationNumbers');
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

    /**
     * Gắn event listeners
     */
    attachEventListeners() {
        // Search
        this.searchInput?.addEventListener('input', this.debounce(() => {
            this.filters.search = this.searchInput.value;
            this.applyFilters();
        }, 300));
        
        this.searchBtn?.addEventListener('click', () => {
            this.filters.search = this.searchInput.value;
            this.applyFilters();
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
        this.itemsPerPageElement?.addEventListener('change', (e) => {
            this.pageSize = parseInt(e.target.value); 
            this.currentPage = 1;
            this.renderDocuments();
            this.updatePagination();
        });
        
        this.firstPageBtn?.addEventListener('click', () => this.goToPage(1));
        this.prevPageBtn?.addEventListener('click', () => this.goToPage(this.currentPage - 1));
        this.nextPageBtn?.addEventListener('click', () => this.goToPage(this.currentPage + 1));
        this.lastPageBtn?.addEventListener('click', () => this.goToPage(this.getTotalPages()));
        
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
            }
        }
    }

    /**
     * Debounce function
     */
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

    /**
     * Load documents and render
     */
    loadDocuments() {
        this.showLoading();
        
        // Simulate API call delay
        setTimeout(() => {
            this.renderDocuments();
            this.updatePagination();
            this.hideLoading();
        }, 500);
    }

    /**
     * Render documents to table
     */
    renderDocuments() {
        if (!this.documentsTableBody) return;

        // Initialize filteredDocuments if empty
        if (this.filteredDocuments.length === 0) {
            this.filteredDocuments = [...this.documents];
        }

        // Calculate pagination
        const startIndex = (this.currentPage - 1) * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        const documentsToShow = this.filteredDocuments.slice(startIndex, endIndex);

        // Clear table body
        this.documentsTableBody.innerHTML = '';

        // Check if no documents
        if (documentsToShow.length === 0) {
            this.showEmptyState();
            return;
        }

        // Get the row template
        const template = document.getElementById('documentRowTemplate');
        if (!template) {
            console.error('Document row template not found!');
            return;
        }

        // Generate table rows using the template
        documentsToShow.forEach(doc => {
            // Clone the template content
            const clone = template.content.cloneNode(true);
            
            // Set document ID as data attribute on the row
            const row = clone.querySelector('tr');
            if (row) row.dataset.id = doc.id;
            
            // Set checkbox value and checked state
            const checkbox = clone.querySelector('.document-checkbox');
            if (checkbox) {
                checkbox.value = doc.id;
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
            
            // Set document thumbnail
            const thumbnail = clone.querySelector('.doc-thumb');
            if (thumbnail) {
                thumbnail.src = doc.thumbnailUrl || this.generateThumbnailUrl(doc.fileType);
                thumbnail.alt = doc.title;
            }
            
            // Set document title and description
            const title = clone.querySelector('.doc-title');
            if (title) title.textContent = doc.title;
            
            const desc = clone.querySelector('.doc-desc');
            if (desc) desc.textContent = doc.description;
            
            // Set document metadata (type and size)
            const docType = clone.querySelector('.doc-type');
            if (docType) docType.textContent = doc.fileType.toUpperCase();
            
            const docSize = clone.querySelector('.doc-size');
            if (docSize) docSize.textContent = this.formatFileSize(doc.fileSize);
            
            // Set author info
            const authorAvatar = clone.querySelector('.author-avatar');
            if (authorAvatar) {
                authorAvatar.src = doc.author.avatarUrl;
                authorAvatar.alt = doc.author.name;
            }
            
            const authorName = clone.querySelector('.author-name');
            if (authorName) authorName.textContent = doc.author.name;
            
            // Set category
            const category = clone.querySelector('.category-name');
            if (category) {
                category.textContent = doc.category.name;
                category.style.color = this.getCategoryColor(doc.category.id);
            }
            
            // Set status
            const status = clone.querySelector('.status-badge');
            if (status) {
                status.textContent = this.getStatusText(doc.status);
                status.className = `status-badge ${this.getStatusClass(doc.status)}`;
            }
            
            // Set price
            const price = clone.querySelector('.price-display');
            if (price) price.textContent = doc.price > 0 ? `${doc.price} VND` : 'Miễn phí';
            
            // Set stats (views and downloads)
            const views = clone.querySelector('.views-count span');
            if (views) views.textContent = this.formatNumber(doc.views);
            
            const downloads = clone.querySelector('.downloads-count span');
            if (downloads) downloads.textContent = this.formatNumber(doc.downloads);
            
            // Set date
            const date = clone.querySelector('.created-date');
            if (date) date.textContent = this.formatDate(doc.createdAt);
            
            // Set actions
            const viewBtn = clone.querySelector('.action-btn.view');
            if (viewBtn) viewBtn.addEventListener('click', () => this.viewDocument(doc.id));
            
            const editBtn = clone.querySelector('.action-btn.edit');
            if (editBtn) editBtn.addEventListener('click', () => this.editDocument(doc.id));
            
            const deleteBtn = clone.querySelector('.action-btn.delete');
            if (deleteBtn) deleteBtn.addEventListener('click', () => this.deleteDocument(doc.id));
            
            // Add the populated row to the table
            this.documentsTableBody.appendChild(clone);
        });

        // Update selection UI
        this.updateSelectionUI();
    }
    // ---------------- Pagination helpers ----------------
    getTotalPages() {
        return Math.max(1, Math.ceil(this.filteredDocuments.length / this.pageSize));
    }

    updatePagination() {
        if (!this.paginationNumbers) return;

        // Cập nhật thông tin
        const total = this.filteredDocuments.length;
        const start = (this.currentPage - 1) * this.pageSize + 1;
        const end   = Math.min(start + this.pageSize - 1, total);
        this.showingFrom.textContent = total ? start : 0;
        this.showingTo.textContent   = end;
        this.totalRecords.textContent = total.toLocaleString();

        // Sinh lại số trang
        this.paginationNumbers.innerHTML = '';
        const totalPages = this.getTotalPages();
        for (let i = 1; i <= totalPages; i++) {
            const btn = document.createElement('button');
            btn.type  = 'button';
            btn.className = 'pagination-number' + (i === this.currentPage ? ' active' : '');
            btn.textContent = i;
            btn.addEventListener('click', () => this.goToPage(i));
            this.paginationNumbers.appendChild(btn);
        }

        // Trạng thái nút điều hướng
        this.firstPageBtn.disabled = this.prevPageBtn.disabled = this.currentPage === 1;
        this.nextPageBtn.disabled  = this.lastPageBtn.disabled = this.currentPage === totalPages;
    }

    goToPage(page) {
        const totalPages = this.getTotalPages();
        if (page < 1 || page > totalPages) return;
        this.currentPage = page;
        this.renderDocuments();
        this.updatePagination();
    }

    /**
     * Helper methods for table rendering
     */

    /**
     * Helper methods for table rendering
     */
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

    // getStatusText(status) {
    //     const statusTexts = {
    //         'PUBLISHED': 'Đã xuất bản',
    //         'PENDING': 'Chờ duyệt',
    //         'REJECTED': 'Từ chối',
    //         'DRAFT': 'Bản nháp',
    //         'ARCHIVED': 'Lưu trữ',
    //         'SUSPENDED': 'Tạm ngưng'
    //     };
    //     return statusTexts[status] || status;
    // }


    // getCategoryColor(categoryId) {
    //     const colors = [
    //         '#3B82F6', '#1cc88a', '#F59E0B', '#EF4444', 
    //         '#8B5CF6', '#06B6D4', '#84CC16', '#F97316'
    //     ];
    //     return colors[categoryId % colors.length];
    // }

    // formatFileSize(bytes) {
    //     const sizes = ['B', 'KB', 'MB', 'GB'];
    //     if (bytes === 0) return '0 B';
    //     const i = Math.floor(Math.log(bytes) / Math.log(1024));
    //     return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
    // }

    formatDate(date) {
        return new Date(date).toLocaleDateString('vi-VN');
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showEmptyState() {
        this.documentsTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">
                    <div class="empty-content">
                        <i class="fas fa-file-alt"></i>
                        <h3>Không tìm thấy tài liệu</h3>
                        <p>Thử thay đổi bộ lọc hoặc thêm tài liệu mới</p>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Show loading state
     */
    showLoading() {
        this.loadingState.style.display = 'block';
        this.documentsTableBody.style.display = 'none';
        this.emptyState.style.display = 'none';
        this.errorState.style.display = 'none';
    }

    /**
     * Hide loading state
     */
    hideLoading() {
        this.loadingState.style.display = 'none';
        this.documentsTableBody.style.display = '';
    }

    /**
     * Update statistics
     */
    updateStatistics() {
        const totalDocs = this.documents.length;
        const totalViews = this.documents.reduce((sum, doc) => sum + doc.views, 0);
        const totalDownloads = this.documents.reduce((sum, doc) => sum + doc.downloads, 0);
        const pendingDocs = this.documents.filter(doc => doc.status === 'PENDING').length;

        this.totalDocuments.textContent = this.formatNumber(totalDocs);
        this.totalViews.textContent = this.formatNumber(totalViews);
        this.totalDownloads.textContent = this.formatNumber(totalDownloads);
        this.pendingDocuments.textContent = this.formatNumber(pendingDocs);
    }

    /**
     * Format number for display
     */
    formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    /**
     * Collect filter values from inputs
     */
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
        this.filters.violation = this.violationFilter?.checked || false;
    }

    /**
     * Apply filters and reload documents
     */
    applyFiltersAndReload() {
        this.currentPage = 1;
        this.applyFilters();
    }

    /**
     * Apply current filters to documents
     */
    applyFilters() {
        this.filteredDocuments = this.documents.filter(doc => {
            // Search filter
            if (this.filters.search) {
                const searchTerm = this.filters.search.toLowerCase();
                if (!doc.title.toLowerCase().includes(searchTerm) && 
                    !doc.description.toLowerCase().includes(searchTerm) &&
                    !doc.author.name.toLowerCase().includes(searchTerm)) {
                    return false;
                }
            }

            // Status filter
            if (this.filters.status && doc.status !== this.filters.status) {
                return false;
            }

            // Category filter
            if (this.filters.category && doc.category.id.toString() !== this.filters.category) {
                return false;
            }

            // File type filter
            if (this.filters.type && doc.fileType !== this.filters.type) {
                return false;
            }

            // Price filter
            if (this.filters.price) {
                if (this.filters.price === 'free' && doc.price > 0) return false;
                if (this.filters.price === 'paid' && doc.price === 0) return false;
            }

            // Author filter
            if (this.filters.author) {
                const authorTerm = this.filters.author.toLowerCase();
                if (!doc.author.name.toLowerCase().includes(authorTerm)) {
                    return false;
                }
            }

            // Tags filter
            if (this.filters.tags) {
                const tagTerm = this.filters.tags.toLowerCase();
                if (!doc.tags.some(tag => tag.toLowerCase().includes(tagTerm))) {
                    return false;
                }
            }

            // Date range filter
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

            // File size filter
            if (this.filters.size) {
                const sizeInMB = doc.fileSize / (1024 * 1024);
                switch (this.filters.size) {
                    case 'small': if (sizeInMB >= 10) return false; break;
                    case 'medium': if (sizeInMB < 10 || sizeInMB >= 50) return false; break;
                    case 'large': if (sizeInMB < 50) return false; break;
                }
            }

            // Views filter
            if (this.filters.views) {
                switch (this.filters.views) {
                    case 'low': if (doc.views >= 1000) return false; break;
                    case 'medium': if (doc.views < 1000 || doc.views >= 10000) return false; break;
                    case 'high': if (doc.views < 10000) return false; break;
                }
            }

            // Rating filter
            if (this.filters.rating) {
                const minRating = parseFloat(this.filters.rating);
                if (doc.rating < minRating) return false;
            }

            // Violation filter
            if (this.filters.violation === 'true' && !doc.hasReports) return false;
            if (this.filters.violation === 'false' && doc.hasReports) return false;

            return true;
        });
        
        // Reset to first page when filters change
        this.currentPage = 1;
    }

    /**
     * Clear all filters
     */
    clearAllFilters() {
        // Reset filter object
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
            violation: false
        };

        // Reset form inputs
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

        // Apply filters
        this.applyFilters();
    }

    /**
     * Apply current filters to documents
     */
    // applyFilters() {
    //     this.filteredDocuments = this.documents.filter(doc => {
    //         // Search filter
    //         if (this.filters.search) {
    //             const searchTerm = this.filters.search.toLowerCase();
    //             const searchableText = `${doc.title} ${doc.description} ${doc.author} ${doc.tags.join(' ')}`.toLowerCase();
    //             if (!searchableText.includes(searchTerm)) {
    //                 return false;
    //             }
    //         }
            
    //         // Status filter
    //         if (this.filters.status && this.filters.status !== 'all') {
    //             if (doc.status !== this.filters.status) {
    //                 return false;
    //             }
    //         }
            
    //         // Category filter
    //         if (this.filters.category && this.filters.category !== 'all') {
    //             if (doc.category !== this.filters.category) {
    //                 return false;
    //             }
    //         }
            
    //         // File type filter
    //         if (this.filters.fileType && this.filters.fileType !== 'all') {
    //             if (doc.fileType !== this.filters.fileType) {
    //                 return false;
    //             }
    //         }
            
    //         // Date range filter
    //         if (this.filters.dateFrom) {
    //             const fromDate = new Date(this.filters.dateFrom);
    //             if (doc.createdAt < fromDate) {
    //                 return false;
    //             }
    //         }
            
    //         if (this.filters.dateTo) {
    //             const toDate = new Date(this.filters.dateTo);
    //             toDate.setHours(23, 59, 59, 999); // End of day
    //             if (doc.createdAt > toDate) {
    //                 return false;
    //             }
    //         }
            
    //         return true;
    //     });
        
    //     // Reset to first page and render
    //     this.currentPage = 1;
    //     this.renderDocuments();
    //     this.updatePagination();
    // }

    /**
     * Collect filter values from form inputs
     */
    // collectFilters() {
    //     this.filters.search = this.searchInput?.value || '';
    //     this.filters.status = this.statusFilter?.value || '';
    //     this.filters.category = this.categoryFilter?.value || '';
    //     this.filters.type = this.typeFilter?.value || '';
    //     this.filters.price = this.priceFilter?.value || '';
    //     this.filters.author = this.authorFilter?.value || '';
    //     this.filters.tags = this.tagsFilter?.value || '';
    //     this.filters.dateFrom = this.dateFromFilter?.value || '';
    //     this.filters.dateTo = this.dateToFilter?.value || '';
    //     this.filters.size = this.sizeFilter?.value || '';
    //     this.filters.views = this.viewsFilter?.value || '';
    //     this.filters.rating = this.ratingFilter?.value || '';
    //     this.filters.violation = this.violationFilter?.value || '';
    // }

    /**
     * Apply filters and reload data
     */
    applyFiltersAndReload() {
        this.collectFilters();
        this.applyFilters();
    }

    /**
     * Clear all filters and reset view
     */
    clearAllFilters() {
        // Reset filter object
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
        
        // Reset form inputs
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
        if (this.violationFilter) this.violationFilter.value = '';
        
        // Đóng panel bộ lọc
        this.advancedFilters.style.display = 'none';
        
        // Apply filters to reset view
        this.applyFilters();
    }

    /**
     * Handle select all checkbox
     */
    handleSelectAll(checked) {
        this.selectedDocuments.clear();
        
        if (checked) {
            // Select all visible documents on current page
            const startIndex = (this.currentPage - 1) * this.pageSize;
            const endIndex = Math.min(startIndex + this.pageSize, this.filteredDocuments.length);
            
            for (let i = startIndex; i < endIndex; i++) {
                this.selectedDocuments.add(this.filteredDocuments[i].id);
            }
        }
        
        // Update all checkboxes on current page
        const checkboxes = document.querySelectorAll('.document-checkbox');
        checkboxes.forEach(checkbox => {
            checkbox.checked = checked;
        });
        
        this.updateSelectionUI();
    }

    /**
     * Clear all selected documents
     */
    clearSelectedDocuments() {
        this.selectedDocuments.clear();
        
        // Uncheck all checkboxes
        const checkboxes = document.querySelectorAll('.document-checkbox');
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
        });
        
        // Uncheck select all checkbox
        if (this.selectAll) {
            this.selectAll.checked = false;
            this.selectAll.indeterminate = false;
        }
        
        this.updateSelectionUI();
    }

    /**
     * Navigate to specific page
     */
    goToPage(page) {
        const totalPages = this.getTotalPages();
        
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        this.currentPage = page;
        this.renderDocuments();
        this.updatePagination();
    }

    /**
     * Get total number of pages
     */
    getTotalPages() {
        return Math.ceil(this.filteredDocuments.length / this.pageSize);
    }

    /**
     * Open document modal for adding/editing
     */
    openDocumentModal(doc = null, mode = 'create') {
        const modal = document.getElementById('documentModal');
        const modalTitle = modal.querySelector('#documentModalTitle'); // Sửa selector đúng với HTML
        const form = modal.querySelector('#documentForm');
        
        if (!modal) {
            console.error('Modal không tìm thấy');
            return;
        }

        // Đặt tiêu đề modal
        if (modalTitle) {
            modalTitle.textContent = doc ? 'Chỉnh sửa tài liệu' : 'Thêm tài liệu mới';
        }
        
        // Nếu là chỉnh sửa, điền thông tin vào form
        if (doc && form) {
            form.querySelector('#documentTitle').value = doc.title || '';
            form.querySelector('#documentDescription').value = doc.description || '';
            form.querySelector('#documentCategory').value = doc.category?.id || '';
            form.querySelector('#documentTags').value = doc.tags ? doc.tags.join(', ') : '';
            form.querySelector('#documentPrice').value = doc.price || 0;
            form.querySelector('#documentStatus').value = doc.status || 'DRAFT';
            form.querySelector('#documentId').value = doc.id || '';
        } else if (form) {
            // Reset form cho trường hợp thêm mới
            form.reset();
            form.querySelector('#documentId').value = '';
        }
        
        // Lưu thông tin tài liệu đang chỉnh sửa
        this.currentEditingDocument = doc;
        
        // Hiển thị modal bằng cách thêm class 'active'
        modal.classList.add('active');
        
        // Thêm event listener cho form submit (chỉ gán 1 lần)
        if (form && !form.hasAttribute('data-listener-added')) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleSaveDocument(e);
            });
            form.setAttribute('data-listener-added', 'true');
        }
        
        // Focus vào trường đầu tiên
        const firstInput = modal.querySelector('#documentTitle');
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    }

    /**
     * Đóng modal tài liệu
     */
    closeDocumentModal() {
        const modal = document.getElementById('documentModal');
        if (modal) {
            modal.classList.remove('active');
            this.currentEditingDocument = null;
        }
    }

    /**
     * Mở modal chi tiết tài liệu
     */
    openDocumentDetailsModal(doc) {
        const modal = document.getElementById('documentDetailsModal');
        const modalBody = modal.querySelector('.modal-body');
        
        if (!modal) {
            console.error('Modal chi tiết không tìm thấy');
            return;
        }

        // Điền thông tin vào các phần tử HTML tĩnh
        const thumbnail = modal.querySelector('#detailThumbnail');
        const title = modal.querySelector('#detailTitle');
        const description = modal.querySelector('#detailDescription');
        const author = modal.querySelector('#detailAuthor');
        const category = modal.querySelector('#detailCategory');
        const fileType = modal.querySelector('#detailFileType');
        const price = modal.querySelector('#detailPrice');
        const views = modal.querySelector('#detailViews');
        const downloads = modal.querySelector('#detailDownloads');
        const rating = modal.querySelector('#detailRating');
        const fileSize = modal.querySelector('#detailFileSize');
        const tags = modal.querySelector('#detailTags');
        const status = modal.querySelector('#detailStatus');
        const createdAt = modal.querySelector('#detailCreatedAt');
        const updatedAt = modal.querySelector('#detailUpdatedAt');

        // Cập nhật nội dung
        if (thumbnail) {
            thumbnail.src = doc.thumbnailUrl || this.generateThumbnailUrl(doc.fileType);
            thumbnail.alt = doc.title;
        }
        if (title) title.textContent = doc.title;
        if (description) description.textContent = doc.description || 'Không có mô tả';
        if (author) author.textContent = doc.author?.name || 'Không xác định';
        if (category) category.textContent = doc.category?.name || 'Không xác định';
        if (fileType) fileType.textContent = doc.fileType || 'Không xác định';
        if (price) price.textContent = doc.price === 0 ? 'Miễn phí' : this.formatPrice(doc.price);
        if (views) views.textContent = this.formatNumber(doc.views || 0);
        if (downloads) downloads.textContent = this.formatNumber(doc.downloads || 0);
        if (rating) rating.textContent = `${doc.rating || 0} ⭐ (${doc.reviewCount || 0} đánh giá)`;
        if (fileSize) fileSize.textContent = `${doc.fileSize || 0} MB`;
        
        // Cập nhật tags
        if (tags) {
            if (doc.tags && doc.tags.length > 0) {
                tags.innerHTML = doc.tags.map(tag => 
                    `<span class="badge bg-secondary me-1">${this.escapeHtml(tag)}</span>`
                ).join('');
            } else {
                tags.innerHTML = '<span class="text-muted">Không có tags</span>';
            }
        }
        
        // Cập nhật trạng thái
        if (status) {
            status.textContent = this.getStatusText(doc.status);
            status.className = `badge ${this.getStatusBadgeClass(doc.status)}`;
        }
        
        // Cập nhật ngày tháng
        if (createdAt) createdAt.textContent = this.formatDate(doc.createdAt);
        if (updatedAt) updatedAt.textContent = this.formatDate(doc.updatedAt);

        // Lưu document hiện tại cho nút chỉnh sửa
        this.currentViewingDocument = doc;
        
        // Hiển thị modal
        modal.classList.add('active');
        
        // Gán event cho nút chỉnh sửa
        const editBtn = modal.querySelector('#editDocumentFromDetails');
        if (editBtn && !editBtn.hasAttribute('data-listener-added')) {
            editBtn.addEventListener('click', () => {
                this.closeDocumentDetailsModal();
                this.openDocumentModal(doc);
            });
            editBtn.setAttribute('data-listener-added', 'true');
        }
    }

    /**
     * Đóng modal chi tiết tài liệu
     */
    closeDocumentDetailsModal() {
        const modal = document.getElementById('documentDetailsModal');
        if (modal) {
            modal.classList.remove('active');
        }
    }

    /**
     * Khởi tạo event listeners cho modal
     */
    initModalEventListeners() {
        // Event listeners cho nút đóng modal document
        const documentModal = document.getElementById('documentModal');
        if (documentModal) {
            // Đóng modal khi click vào overlay
            documentModal.addEventListener('click', (e) => {
                if (e.target === documentModal) {
                    this.closeDocumentModal();
                }
            });
            
            // Đóng modal khi click vào nút đóng
            const closeButtons = documentModal.querySelectorAll('.modal-close, [data-modal-close]');
            closeButtons.forEach(btn => {
                btn.addEventListener('click', () => {
                    this.closeDocumentModal();
                });
            });
        }

        // Event listeners cho modal chi tiết
        const detailsModal = document.getElementById('documentDetailsModal');
        if (detailsModal) {
            // Đóng modal khi click vào overlay
            detailsModal.addEventListener('click', (e) => {
                if (e.target === detailsModal) {
                    this.closeDocumentDetailsModal();
                }
            });
            
            // Đóng modal khi click vào nút đóng
            const closeButtons = detailsModal.querySelectorAll('.modal-close, [data-modal-close]');
            closeButtons.forEach(btn => {
                btn.addEventListener('click', () => {
                    this.closeDocumentDetailsModal();
                });
            });
        }

        // Event listener cho phím ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeDocumentModal();
                this.closeDocumentDetailsModal();
            }
        });
    }

    /**
     * Copy share URL to clipboard
     */
    copyShareUrl() {
        const shareUrlInput = document.querySelector('#shareUrl');
        if (shareUrlInput) {
            shareUrlInput.select();
            shareUrlInput.setSelectionRange(0, 99999); // For mobile devices
            
            try {
                document.execCommand('copy');
                this.showToast('Đã sao chép URL vào clipboard!', 'success');
            } catch (err) {
                console.error('Failed to copy URL:', err);
                this.showToast('Không thể sao chép URL', 'error');
            }
        }
    }

    /**
     * Share to Facebook
     */
    shareToFacebook(url) {
        const facebookUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
        window.open(facebookUrl, '_blank', 'width=600,height=400');
    }

    /**
     * Share to Twitter
     */
    shareToTwitter(url, title) {
        const twitterUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(title)}`;
        window.open(twitterUrl, '_blank', 'width=600,height=400');
    }

    /**
     * Share to WhatsApp
     */
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

    formatFileSize(sizeMb) {
        if (sizeMb >= 1024) {
            return (sizeMb / 1024).toFixed(1) + ' GB';
        }
        return sizeMb.toFixed(1) + ' MB';
    }

    formatDate(date) {
        return new Date(date).toLocaleDateString('vi-VN');
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // getStatusBadgeClass(status) {
    //     const classes = {
    //         'PUBLISHED': 'bg-success',
    //         'PENDING': 'bg-warning',
    //         'REJECTED': 'bg-danger',
    //         'DRAFT': 'bg-secondary',
    //         'ARCHIVED': 'bg-info',
    //         'SUSPENDED': 'bg-dark'
    //     };
    //     return classes[status] || 'bg-secondary';
    // }

    getStatusText(status) {
        const texts = {
            'PUBLISHED': 'Đã xuất bản',
            'PENDING': 'Chờ duyệt',
            'REJECTED': 'Bị từ chối',
            'DRAFT': 'Bản nháp',
            'ARCHIVED': 'Đã lưu trữ',
            'SUSPENDED': 'Bị đình chỉ'
        };
        return texts[status] || 'Không xác định';
    }

    /**
     * Open share modal
     */
    // openShareModal(doc) {
    //     const modal = document.getElementById('shareModal');
    //     if (!modal) {
    //         console.error('Share modal not found');
    //         return;
    //     }

    //     // Cập nhật nội dung modal
    //     const shareUrl = `${window.location.origin}/documents/${doc.id}`;
    //     modal.querySelector('#shareUrl').value = shareUrl;
    //     modal.querySelector('#shareTitle').textContent = doc.title;

    //     // Hiển thị modal
    //     modal.classList.add('active');

    //     // Gắn event listeners cho các nút chia sẻ (chỉ gắn một lần)
    //     if (!modal.dataset.listenersAttached) {
    //         // Nút sao chép URL
    //         modal.querySelector('#copyShareUrlBtn').addEventListener('click', () => {
    //             this.copyShareUrl();
    //         });

    //         // Nút chia sẻ Facebook
    //         modal.querySelector('#shareToFacebookBtn').addEventListener('click', () => {
    //             this.shareToFacebook(shareUrl);
    //         });

    //         // Nút chia sẻ Twitter
    //         modal.querySelector('#shareToTwitterBtn').addEventListener('click', () => {
    //             this.shareToTwitter(shareUrl, doc.title);
    //         });

    //         // Nút chia sẻ WhatsApp
    //         modal.querySelector('#shareToWhatsAppBtn').addEventListener('click', () => {
    //             this.shareToWhatsApp(shareUrl, doc.title);
    //         });

    //         modal.dataset.listenersAttached = 'true';
    //     }
    // }

    /**
     * Save (add or update) document – attached to form submit
     */
    handleSaveDocument(e) {
        e.preventDefault();
        const form = this.documentForm;
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }
        // Gather data
        const id = form.querySelector('#documentId').value;
        const data = {
            id: id || 'doc_' + Date.now(),
            title: form.querySelector('#documentTitle').value.trim(),
            description: form.querySelector('#documentDescription').value.trim(),
            category: { id: form.querySelector('#documentCategory').value, name: form.querySelector('#documentCategory').selectedOptions[0].textContent },
            tags: form.querySelector('#documentTags').value.split(',').map(t => t.trim()).filter(Boolean),
            price: parseInt(form.querySelector('#documentPrice').value || '0'),
            status: form.querySelector('#documentStatus').value,
            views: 0,
            downloads: 0,
            rating: 0,
            fileSize: '—',
            fileType: 'PDF',
            author: { name: 'Bạn', avatarUrl: '' },
            thumbnailUrl: '',
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        if (id) {
            // update existing
            const idx = this.documents.findIndex(d => d.id === id);
            if (idx !== -1) {
                this.documents[idx] = { ...this.documents[idx], ...data, updated_at: new Date().toISOString() };
            }
            this.showToast('Cập nhật tài liệu thành công', 'success');
        } else {
            // add new
            this.documents.unshift(data);
            this.showToast('Thêm tài liệu thành công', 'success');
        }

        // refresh list & UI
        this.filteredDocuments = [...this.documents];
        this.currentPage = 1;
        this.renderDocuments();
        this.updatePagination();
        this.closeDocumentModal();
    }

    refreshData() {
        // Reset to initial state and regenerate mock data
        this.currentPage = 1;
        this.generateMockData();
        this.renderDocuments();
        this.updatePagination();
        this.updateStatistics();
        
        this.showToast('Dữ liệu đã được làm mới', 'success');
    }

    /**
     * Handle table sorting
     */
    handleSort(field) {
        if (this.sortField === field) {
            this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.sortField = field;
            this.sortDirection = 'asc';
        }

        this.sortDocuments();
        this.renderDocuments();
        this.updateSortIndicators();
    }

    /**
     * Sort documents by current sort field and direction
     */
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
                case 'created_at':
                    valueA = a.createdAt;
                    valueB = b.createdAt;
                    break;
                default:
                    valueA = a.createdAt;
                    valueB = b.createdAt;
            }

            if (valueA < valueB) return this.sortDirection === 'asc' ? -1 : 1;
            if (valueA > valueB) return this.sortDirection === 'asc' ? 1 : -1;
            return 0;
        });
    }

    /**
     * Update sort indicators in table headers
     */
    updateSortIndicators() {
        // Remove all existing sort indicators
        document.querySelectorAll('.sortable .sort-indicator').forEach(indicator => {
            indicator.textContent = '↕️';
        });

        // Add indicator for current sort field
        const currentSortHeader = document.querySelector(`[data-sort="${this.sortField}"] .sort-indicator`);
        if (currentSortHeader) {
            currentSortHeader.textContent = this.sortDirection === 'asc' ? '↑' : '↓';
        }
    }

    /**
     * Document Actions
     */
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
            // Simulate download
            this.showToast(`Đang tải xuống "${doc.title}"...`, 'info');
            setTimeout(() => {
                this.showToast('Tải xuống hoàn tất', 'success');
            }, 1500);
        }
    }

    /**
     * Bulk Actions
     */
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

    /**
     * Modal Methods
     */
    openShareModal(doc) {
        const modal = document.getElementById('shareModal');
        if (!modal) {
            console.error('Share modal not found');
            return;
        }

        // Cập nhật nội dung modal
        const shareUrl = `${window.location.origin}/documents/${doc.id}`;
        modal.querySelector('#shareUrl').value = shareUrl;
        modal.querySelector('#shareTitle').textContent = doc.title;

        // Hiển thị modal
        modal.classList.add('active');

        // Gắn event listeners cho các nút chia sẻ (chỉ gắn một lần)
        if (!modal.dataset.listenersAttached) {
            // Nút sao chép URL
            modal.querySelector('#copyShareUrlBtn').addEventListener('click', () => {
                this.copyShareUrl();
            });

            // Nút chia sẻ Facebook
            modal.querySelector('#shareToFacebookBtn').addEventListener('click', () => {
                this.shareToFacebook(shareUrl);
            });

            // Nút chia sẻ Twitter
            modal.querySelector('#shareToTwitterBtn').addEventListener('click', () => {
                this.shareToTwitter(shareUrl, doc.title);
            });

            // Nút chia sẻ WhatsApp
            modal.querySelector('#shareToWhatsAppBtn').addEventListener('click', () => {
                this.shareToWhatsApp(shareUrl, doc.title);
            });

            modal.dataset.listenersAttached = 'true';
        }
    }

    /**
     * Show toast notification
     */
    showToast(message, type = 'info') {
        // Create toast if it doesn't exist
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

        // Remove toast after it's hidden
        toast.addEventListener('hidden.bs.toast', () => {
            toast.remove();
        });
    }

    /**
     * Update selection UI elements
     */
    updateSelectionUI() {
        // Update selected count
        if (this.selectedCount) {
            this.selectedCount.textContent = this.selectedDocuments.size;
        }
        // Toggle bulk action bar
        if (this.bulkActions) {
            this.bulkActions.style.display = this.selectedDocuments.size > 0 ? 'flex' : 'none';
        }
    }

    /**
     * Initialize modal tabs functionality
     */
    initializeModalTabs() {
        // Handle tab switching for both Document Details and Add/Edit modals
        document.addEventListener('click', (e) => {
            if (e.target.matches('.tab-btn') || e.target.closest('.tab-btn')) {
                const tabBtn = e.target.matches('.tab-btn') ? e.target : e.target.closest('.tab-btn');
                const tabName = tabBtn.dataset.tab;
                const modal = tabBtn.closest('.modal');
                
                this.switchTab(modal, tabName);
            }
        });

        // Handle conditional field display in Add/Edit modal
        this.initConditionalFields();
    }

    /**
     * Switch tab in modal
     */
    switchTab(modal, tabName) {
        // Update tab buttons
        const tabBtns = modal.querySelectorAll('.tab-btn');
        const tabPanels = modal.querySelectorAll('.tab-panel');

        tabBtns.forEach(btn => btn.classList.remove('active'));
        tabPanels.forEach(panel => panel.classList.remove('active'));

        // Activate selected tab
        const activeBtn = modal.querySelector(`[data-tab="${tabName}"]`);
        const activePanel = modal.querySelector(`#tab${tabName.charAt(0).toUpperCase() + tabName.slice(1)}`);

        if (activeBtn) activeBtn.classList.add('active');
        if (activePanel) activePanel.classList.add('active');
    }

    /**
     * Load content for specific tab
     */
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

    /**
     * Load version history for document
     */
    loadVersionHistory(documentId) {
        // Placeholder - sẽ tích hợp với API thực tế
        const versionsContainer = document.getElementById('versionsTimeline');
        if (versionsContainer) {
            // Hiện tại hiển thị dữ liệu mẫu, sau này sẽ fetch từ API
            console.log(`Loading version history for document ${documentId}`);
        }
    }

    /**
     * Load analytics data for document
     */
    loadAnalytics(documentId) {
        // Placeholder - sẽ tích hợp với API thực tế
        const analyticsContainer = document.getElementById('tabAnalytics');
        if (analyticsContainer) {
            // Hiện tại hiển thị dữ liệu mẫu, sau này sẽ fetch từ API
            console.log(`Loading analytics for document ${documentId}`);
        }
    }

    /**
     * Load reviews and comments for document
     */
    loadReviews(documentId) {
        // Placeholder - sẽ tích hợp với API thực tế
        const reviewsContainer = document.getElementById('reviewsList');
        if (reviewsContainer) {
            // Hiện tại hiển thị dữ liệu mẫu, sau này sẽ fetch từ API
            console.log(`Loading reviews for document ${documentId}`);
        }
    }

    /**
     * Open document details modal with enhanced tab functionality
     */
    openDocumentDetailsModal(doc) {
        if (!doc) return;

        const modal = document.getElementById('documentDetailsModal');
        if (!modal) {
            console.error('Document details modal not found');
            return;
        }

        // Store current viewing document
        this.currentViewingDocument = doc;

        // Populate general information tab
        this.populateDocumentDetails(doc);

        // Reset to general tab
        this.switchTab(modal, 'general');

        // Show modal
        modal.classList.add('active');

        // Initialize tabs if not already done
        if (!this.tabsInitialized) {
            this.initializeModalTabs();
            this.tabsInitialized = true;
        }
    }

    /**
     * Populate document details in general tab
     */
    populateDocumentDetails(doc) {
        if (!doc) {
            console.error('No document data provided to populateDocumentDetails');
            return;
        }

        console.log('Populating document details for:', doc);

        // Thumbnail
        const thumbnail = document.getElementById('detailThumbnail');
        if (thumbnail) {
            thumbnail.src = doc.thumbnail || 'https://via.placeholder.com/300x200?text=No+Image';
            thumbnail.alt = doc.title || 'Document thumbnail';
        } else {
            console.warn('detailThumbnail element not found');
        }

        // Basic information
        this.setElementText('detailTitle', doc.title || 'Không có tiêu đề');
        this.setElementText('detailDescription', doc.description || 'Không có mô tả');
        this.setElementText('detailAuthor', doc.author?.name || 'Không có tác giả');
        this.setElementText('detailCategory', doc.category?.name || 'Không có danh mục');
        this.setElementText('detailFileType', doc.fileType || 'Không xác định');
        this.setElementText('detailPrice', doc.price ? `${doc.price.toLocaleString()} VNĐ` : 'Miễn phí');
        
        // Statistics
        this.setElementText('detailViews', doc.views?.toLocaleString() || '0');
        this.setElementText('detailDownloads', doc.downloads?.toLocaleString() || '0');
        this.setElementText('detailRating', doc.rating ? `${doc.rating}/5 ⭐ (${doc.reviewCount || 0} đánh giá)` : 'Chưa có đánh giá');
        this.setElementText('detailFileSize', doc.fileSize ? `${doc.fileSize} MB` : 'Không xác định');

        // Tags
        this.populateTags(doc.tags);

        // Status
        this.populateStatus(doc.status);

        // Dates
        this.setElementText('detailCreatedAt', doc.createdAt ? new Date(doc.createdAt).toLocaleDateString('vi-VN') : 'Không xác định');
        this.setElementText('detailUpdatedAt', doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString('vi-VN') : 'Không xác định');

        console.log('Document details populated successfully');
    }

    /**
     * Helper function to set element text with error handling
     */
    setElementText(elementId, text) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = text;
        } else {
            console.warn(`Element with id '${elementId}' not found`);
        }
    }

    /**
     * Populate tags section
     */
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

    /**
     * Populate status section
     */
    populateStatus(status) {
        const statusElement = document.getElementById('detailStatus');
        if (!statusElement) {
            console.warn('detailStatus element not found');
            return;
        }

        statusElement.textContent = this.getStatusText(status);
        statusElement.className = `badge ${this.getStatusBadgeClass(status)}`;
    }

    /**
     * Get status text in Vietnamese
     */
    getStatusText(status) {
        const statusMap = {
            'PUBLISHED': 'Đã xuất bản',
            'DRAFT': 'Bản nháp',
            'PENDING': 'Chờ duyệt',
            'REJECTED': 'Bị từ chối',
            'ARCHIVED': 'Đã lưu trữ',
            'SUSPENDED': 'Đã tạm ngưng'
        };
        return statusMap[status] || status || 'Không xác định';
    }

    /**
     * Initialize Modal Tab Management
     */
    initModalTabs() {
        // Handle tab switching for both Document Details and Add/Edit modals
        document.addEventListener('click', (e) => {
            if (e.target.matches('.tab-btn') || e.target.closest('.tab-btn')) {
                const tabBtn = e.target.matches('.tab-btn') ? e.target : e.target.closest('.tab-btn');
                const tabName = tabBtn.dataset.tab;
                const modal = tabBtn.closest('.modal');
                
                this.switchTab(modal, tabName);
            }
        });

        // Handle conditional field display in Add/Edit modal
        this.initConditionalFields();
    }

    /**
     * Switch tab in modal
     */
    switchTab(modal, tabName) {
        // Update tab buttons
        const tabBtns = modal.querySelectorAll('.tab-btn');
        const tabPanels = modal.querySelectorAll('.tab-panel');

        tabBtns.forEach(btn => btn.classList.remove('active'));
        tabPanels.forEach(panel => panel.classList.remove('active'));

        // Activate selected tab
        const activeBtn = modal.querySelector(`[data-tab="${tabName}"]`);
        const activePanel = modal.querySelector(`#tab${tabName.charAt(0).toUpperCase() + tabName.slice(1)}`);

        if (activeBtn) activeBtn.classList.add('active');
        if (activePanel) activePanel.classList.add('active');
    }

    /**
     * Load content for specific tab
     */
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

    /**
     * Initialize conditional fields in Add/Edit modal
     */
    initConditionalFields() {
        // Schedule publish checkbox
        const scheduleCheckbox = document.getElementById('schedulePublish');
        const scheduleFields = document.getElementById('scheduleFields');
        
        if (scheduleCheckbox && scheduleFields) {
            scheduleCheckbox.addEventListener('change', () => {
                scheduleFields.style.display = scheduleCheckbox.checked ? 'flex' : 'none';
            });
        }

        // Require approval checkbox
        const approvalCheckbox = document.getElementById('requireApproval');
        const approvalFields = document.getElementById('approvalFields');
        
        if (approvalCheckbox && approvalFields) {
            approvalCheckbox.addEventListener('change', () => {
                approvalFields.style.display = approvalCheckbox.checked ? 'flex' : 'none';
            });
        }

        // Meta description character counter
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

        // File upload handlers
        this.initFileUploadHandlers();

        // Save draft button
        const saveDraftBtn = document.getElementById('saveDraftBtn');
        if (saveDraftBtn) {
            saveDraftBtn.addEventListener('click', () => {
                this.saveDraft();
            });
        }
    }

    /**
     * Initialize file upload handlers with preview
     */
    initFileUploadHandlers() {
        // Document file upload
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

            // Remove file button
            const removeBtn = fileInfo.querySelector('.btn-remove');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    documentFile.value = '';
                    fileInfo.style.display = 'none';
                });
            }
        }

        // Thumbnail upload
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

            // Remove thumbnail button
            const removeBtn = thumbnailPreview.querySelector('.btn-remove');
            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    thumbnailFile.value = '';
                    thumbnailPreview.style.display = 'none';
                });
            }
        }
    }

    /**
     * Show file information
     */
    showFileInfo(file, container) {
        const fileName = container.querySelector('.file-name');
        const fileSize = container.querySelector('.file-size');
        
        if (fileName) fileName.textContent = file.name;
        if (fileSize) fileSize.textContent = this.formatFileSize(file.size);
        
        container.style.display = 'flex';
    }

    /**
     * Show thumbnail preview
     */
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

    /**
     * Format file size
     */
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    /**
     * Save document as draft
     */
    saveDraft() {
        const form = this.documentForm;
        
        // Set status to DRAFT
        const statusField = form.querySelector('#documentStatus');
        if (statusField) {
            statusField.value = 'DRAFT';
        }

        // Validate only required fields for draft
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

        // Save as draft
        this.saveDocument(true);
        this.showToast('Lưu bản nháp thành công', 'success');
    }
    
    /**
     * Get CSS class for status badge
     */
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

    /**
     * Load version history for document (placeholder)
     */
    loadVersionHistory(documentId) {
        // Placeholder for version history loading
        console.log('Loading version history for document:', documentId);
        // TODO: Implement version history loading
    }

    /**
     * Load analytics for document (placeholder)
     */
    loadAnalytics(documentId) {
        // Placeholder for analytics loading
        console.log('Loading analytics for document:', documentId);
        // TODO: Implement analytics loading
    }

    /**
     * Load reviews for document (placeholder)
     */
    loadReviews(documentId) {
        // Placeholder for reviews loading
        console.log('Loading reviews for document:', documentId);
        // TODO: Implement reviews loading
    }
    
}

// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', () => {
    window.documentsManager = new DocumentsManager();
});
