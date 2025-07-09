// Search JavaScript for EduShare
document.addEventListener('DOMContentLoaded', function() {
  initSearchForm();
  initLayoutToggle();
  initFilterSections();
  initFilterActions();
  initPagination();
  initBookmarkButtons();
  initMobileFilterToggle();
  initSortSelect();
  initURLParams();
  initHeaderSearch();
});

// Initialize search form
function initSearchForm() {
  const searchForm = document.getElementById('searchForm');
  const searchInput = document.getElementById('searchInput');
  
  if (searchForm) {
    searchForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const query = searchInput.value.trim();
      if (query.length > 0) {
        // Update URL with search query
        updateURL({q: query});
        
        // In a real application, this would call an API to get search results
        // For now, we'll just update the UI to show our mock results with the query
        updateSearchDisplay(query);
      }
    });
  }
}

// Initialize header search
function initHeaderSearch() {
  const headerSearchBtn = document.getElementById('headerSearchBtn');
  const headerSearchInput = document.getElementById('headerSearchInput');
  
  if (headerSearchBtn && headerSearchInput) {
    headerSearchBtn.addEventListener('click', function() {
      performHeaderSearch();
    });
    
    headerSearchInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        performHeaderSearch();
      }
    });
  }
}

// Perform header search
function performHeaderSearch() {
  const headerSearchInput = document.getElementById('headerSearchInput');
  const query = headerSearchInput.value.trim();
  
  if (query.length > 0) {
    window.location.href = `search.html?q=${encodeURIComponent(query)}`;
  }
}

// Initialize layout toggle between grid and list views
function initLayoutToggle() {
  const layoutButtons = document.querySelectorAll('.layout-option');
  const searchResults = document.getElementById('searchResults');
  
  if (layoutButtons.length && searchResults) {
    layoutButtons.forEach(button => {
      button.addEventListener('click', function() {
        // Remove active class from all buttons
        layoutButtons.forEach(btn => btn.classList.remove('active'));
        
        // Add active class to clicked button
        this.classList.add('active');
        
        // Get the layout type (grid or list)
        const layout = this.getAttribute('data-layout');
        
        // Update search results layout
        searchResults.className = `search-results ${layout}-view`;
        
        // Save layout preference in localStorage for persistence
        localStorage.setItem('search_layout_preference', layout);
      });
    });
    
    // Apply saved layout preference if available
    const savedLayout = localStorage.getItem('search_layout_preference');
    if (savedLayout) {
      const layoutButton = document.querySelector(`.layout-option[data-layout="${savedLayout}"]`);
      if (layoutButton) {
        layoutButton.click();
      }
    }
  }
}

// Initialize filter sections (collapse/expand)
function initFilterSections() {
  const filterHeaders = document.querySelectorAll('.filter-header');
  
  if (filterHeaders.length) {
    filterHeaders.forEach(header => {
      const filterToggle = header.querySelector('.filter-toggle');
      const filterBody = header.nextElementSibling;
      
      header.addEventListener('click', function() {
        filterToggle.classList.toggle('active');
        filterBody.classList.toggle('collapsed');
        
        // Save filter state in localStorage
        const filterTitle = header.querySelector('.filter-title').textContent;
        const isCollapsed = filterBody.classList.contains('collapsed');
        localStorage.setItem(`filter_${filterTitle.toLowerCase().replace(/\s+/g, '_')}`, isCollapsed ? 'collapsed' : 'expanded');
      });
      
      // Apply saved filter state if available
      const filterTitle = header.querySelector('.filter-title').textContent;
      const savedState = localStorage.getItem(`filter_${filterTitle.toLowerCase().replace(/\s+/g, '_')}`);
      
      if (savedState === 'collapsed') {
        filterToggle.classList.add('active');
        filterBody.classList.add('collapsed');
      }
    });
  }
  
  // Initialize "Show more" buttons
  const showMoreButtons = document.querySelectorAll('.show-more-btn');
  
  if (showMoreButtons.length) {
    showMoreButtons.forEach(button => {
      button.addEventListener('click', function(e) {
        e.stopPropagation();
        
        const filterOptions = this.previousElementSibling;
        const hiddenOptions = filterOptions.querySelectorAll('.filter-option.hidden');
        
        if (hiddenOptions.length) {
          // Show hidden options
          hiddenOptions.forEach(option => {
            option.classList.remove('hidden');
          });
          
          this.innerHTML = 'Thu gọn <i class="fas fa-chevron-up"></i>';
          this.classList.add('show-less');
        } else {
          // Hide options after the first 5
          const allOptions = filterOptions.querySelectorAll('.filter-option');
          
          if (allOptions.length > 5) {
            for (let i = 5; i < allOptions.length; i++) {
              allOptions[i].classList.add('hidden');
            }
            
            this.innerHTML = 'Xem thêm <i class="fas fa-chevron-down"></i>';
            this.classList.remove('show-less');
          }
        }
      });
      
      // Initially hide options after the first 5
      const filterOptions = button.previousElementSibling;
      const allOptions = filterOptions.querySelectorAll('.filter-option');
      
      if (allOptions.length > 5) {
        for (let i = 5; i < allOptions.length; i++) {
          allOptions[i].classList.add('hidden');
        }
      } else {
        // If 5 or fewer options, hide the "Show more" button
        button.style.display = 'none';
      }
    });
  }
}

// Initialize filter actions (apply/reset)
function initFilterActions() {
  const applyFiltersBtn = document.getElementById('applyFilters');
  const resetFiltersBtn = document.getElementById('resetFilters');
  
  if (applyFiltersBtn) {
    applyFiltersBtn.addEventListener('click', function() {
      applyFilters();
    });
  }
  
  if (resetFiltersBtn) {
    resetFiltersBtn.addEventListener('click', function() {
      resetFilters();
    });
  }
}

// Apply filters and update results
function applyFilters() {
  // Collect all filter values
  const filterParams = {};
  
  // Category filters
  const selectedCategories = [];
  document.querySelectorAll('input[name="category"]:checked').forEach(input => {
    selectedCategories.push(input.value);
  });
  if (selectedCategories.length > 0) {
    filterParams.category = selectedCategories.join(',');
  }
  
  // Format filters
  const selectedFormats = [];
  document.querySelectorAll('input[name="format"]:checked').forEach(input => {
    selectedFormats.push(input.value);
  });
  if (selectedFormats.length > 0) {
    filterParams.format = selectedFormats.join(',');
  }
  
  // Price filters
  const selectedPrices = [];
  document.querySelectorAll('input[name="price"]:checked').forEach(input => {
    selectedPrices.push(input.value);
  });
  if (selectedPrices.length > 0) {
    filterParams.price = selectedPrices.join(',');
  }
  
  // Rating filter
  const selectedRating = document.querySelector('input[name="rating"]:checked');
  if (selectedRating && selectedRating.value !== 'any') {
    filterParams.rating = selectedRating.value;
  }
  
  // Time filter
  const selectedTime = document.querySelector('input[name="time"]:checked');
  if (selectedTime && selectedTime.value !== 'any') {
    filterParams.time = selectedTime.value;
  }
  
  // Get current search query if any
  const searchInput = document.getElementById('searchInput');
  if (searchInput && searchInput.value.trim().length > 0) {
    filterParams.q = searchInput.value.trim();
  }
  
  // Get current sort option
  const sortSelect = document.getElementById('sortSelect');
  if (sortSelect && sortSelect.value !== 'relevance') {
    filterParams.sort = sortSelect.value;
  }
  
  // Update URL with filter parameters
  updateURL(filterParams);
  
  // In a real application, this would fetch new results from an API
  // For now, we'll just simulate filtering by updating the UI
  const query = searchInput ? searchInput.value.trim() : '';
  updateSearchDisplay(query);
  
  // If on mobile, close the filter panel
  if (window.innerWidth < 992) {
    const searchSidebar = document.querySelector('.search-sidebar');
    const mobileFilterToggle = document.getElementById('mobileFilterToggle');
    
    if (searchSidebar && mobileFilterToggle) {
      searchSidebar.classList.remove('mobile-visible');
      mobileFilterToggle.classList.remove('active');
    }
  }
}

// Reset all filters to default
function resetFilters() {
  // Reset category filters
  document.querySelectorAll('input[name="category"]').forEach(input => {
    input.checked = false;
  });
  
  // Reset format filters
  document.querySelectorAll('input[name="format"]').forEach(input => {
    input.checked = false;
  });
  
  // Reset price filters
  document.querySelectorAll('input[name="price"]').forEach(input => {
    input.checked = false;
  });
  
  // Reset rating filter to "any"
  const anyRatingOption = document.querySelector('input[name="rating"][value="any"]');
  if (anyRatingOption) {
    anyRatingOption.checked = true;
  }
  
  // Reset time filter to "any"
  const anyTimeOption = document.querySelector('input[name="time"][value="any"]');
  if (anyTimeOption) {
    anyTimeOption.checked = true;
  }
  
  // Reset sort option to "relevance"
  const sortSelect = document.getElementById('sortSelect');
  if (sortSelect) {
    sortSelect.value = 'relevance';
  }
  
  // Apply the reset filters (updates URL and UI)
  applyFilters();
}

// Update URL with filter parameters
function updateURL(params) {
  const urlParams = new URLSearchParams(window.location.search);
  
  // Clear existing params that we control
  ['q', 'category', 'format', 'price', 'rating', 'time', 'sort', 'page'].forEach(param => {
    urlParams.delete(param);
  });
  
  // Add new params
  Object.keys(params).forEach(key => {
    if (params[key]) {
      urlParams.set(key, params[key]);
    }
  });
  
  // Update URL
  const newURL = window.location.pathname + (urlParams.toString() ? `?${urlParams.toString()}` : '');
  window.history.replaceState({}, '', newURL);
}

// Initialize URL parameters
function initURLParams() {
  const urlParams = new URLSearchParams(window.location.search);
  
  // Set search query from URL
  if (urlParams.has('q')) {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
      searchInput.value = urlParams.get('q');
    }
    
    // Update search display with the query
    updateSearchDisplay(urlParams.get('q'));
  }
  
  // Set category filters from URL
  if (urlParams.has('category')) {
    const categories = urlParams.get('category').split(',');
    categories.forEach(category => {
      const categoryInput = document.querySelector(`input[name="category"][value="${category}"]`);
      if (categoryInput) {
        categoryInput.checked = true;
      }
    });
  }
  
  // Set format filters from URL
  if (urlParams.has('format')) {
    const formats = urlParams.get('format').split(',');
    formats.forEach(format => {
      const formatInput = document.querySelector(`input[name="format"][value="${format}"]`);
      if (formatInput) {
        formatInput.checked = true;
      }
    });
  }
  
  // Set price filters from URL
  if (urlParams.has('price')) {
    const prices = urlParams.get('price').split(',');
    prices.forEach(price => {
      const priceInput = document.querySelector(`input[name="price"][value="${price}"]`);
      if (priceInput) {
        priceInput.checked = true;
      }
    });
  }
  
  // Set rating filter from URL
  if (urlParams.has('rating')) {
    const ratingInput = document.querySelector(`input[name="rating"][value="${urlParams.get('rating')}"]`);
    if (ratingInput) {
      ratingInput.checked = true;
    }
  }
  
  // Set time filter from URL
  if (urlParams.has('time')) {
    const timeInput = document.querySelector(`input[name="time"][value="${urlParams.get('time')}"]`);
    if (timeInput) {
      timeInput.checked = true;
    }
  }
  
  // Set sort option from URL
  if (urlParams.has('sort')) {
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
      sortSelect.value = urlParams.get('sort');
    }
  }
}

// Initialize pagination
function initPagination() {
  const paginationItems = document.querySelectorAll('.pagination-item:not(.disabled)');
  
  if (paginationItems.length) {
    paginationItems.forEach(item => {
      const link = item.querySelector('a');
      
      if (link) {
        link.addEventListener('click', function(e) {
          e.preventDefault();
          
          // Remove active class from all pagination items
          paginationItems.forEach(item => item.classList.remove('active'));
          
          // Add active class to clicked item
          item.classList.add('active');
          
          // Get page number from link text or attribute
          let page = link.textContent.trim();
          if (link.getAttribute('aria-label') === 'Previous') {
            page = getCurrentPage() - 1;
          } else if (link.getAttribute('aria-label') === 'Next') {
            page = getCurrentPage() + 1;
          }
          
          // Update URL with page number
          const urlParams = new URLSearchParams(window.location.search);
          urlParams.set('page', page);
          
          const newURL = `${window.location.pathname}?${urlParams.toString()}`;
          window.history.replaceState({}, '', newURL);
          
          // In a real application, this would fetch the specific page of results
          // For now, we'll just scroll to the top of the results
          const searchResultsSection = document.querySelector('.search-results-section');
          if (searchResultsSection) {
            searchResultsSection.scrollIntoView({ behavior: 'smooth' });
          }
        });
      }
    });
  }
}

// Get current pagination page
function getCurrentPage() {
  const activePaginationItem = document.querySelector('.pagination-item.active');
  
  if (activePaginationItem) {
    const link = activePaginationItem.querySelector('a');
    if (link) {
      const page = parseInt(link.textContent.trim());
      return isNaN(page) ? 1 : page;
    }
  }
  
  // Default to page 1 if no active pagination item found
  return 1;
}

// Initialize bookmark buttons
function initBookmarkButtons() {
  const bookmarkButtons = document.querySelectorAll('.result-bookmark-btn');
  
  if (bookmarkButtons.length) {
    bookmarkButtons.forEach(button => {
      button.addEventListener('click', function() {
        // Toggle active class
        this.classList.toggle('active');
        
        // Toggle bookmark icon
        const icon = this.querySelector('i');
        if (icon) {
          if (icon.classList.contains('far')) {
            icon.classList.remove('far');
            icon.classList.add('fas');
            showToast('Đã lưu tài liệu vào mục yêu thích', 'success');
          } else {
            icon.classList.remove('fas');
            icon.classList.add('far');
            showToast('Đã xóa tài liệu khỏi mục yêu thích', 'info');
          }
        }
      });
    });
  }
}

// Initialize mobile filter toggle
function initMobileFilterToggle() {
  const mobileFilterToggle = document.getElementById('mobileFilterToggle');
  const searchSidebar = document.querySelector('.search-sidebar');
  
  if (mobileFilterToggle && searchSidebar) {
    mobileFilterToggle.addEventListener('click', function() {
      this.classList.toggle('active');
      searchSidebar.classList.toggle('mobile-visible');
    });
  }
}

// Initialize sort select
function initSortSelect() {
  const sortSelect = document.getElementById('sortSelect');
  
  if (sortSelect) {
    sortSelect.addEventListener('change', function() {
      // Update URL with new sort option
      const urlParams = new URLSearchParams(window.location.search);
      
      if (this.value === 'relevance') {
        urlParams.delete('sort');
      } else {
        urlParams.set('sort', this.value);
      }
      
      const newURL = `${window.location.pathname}?${urlParams.toString()}`;
      window.history.replaceState({}, '', newURL);
      
      // In a real application, this would fetch sorted results from an API
      // For now, we'll just update the UI to simulate sorting
      sortSearchResults(this.value);
    });
  }
}

// Sort search results
function sortSearchResults(sortBy) {
  // In a real application, this would fetch sorted results from an API
  // For now, we'll just show a toast message to indicate sorting
  showToast(`Đã sắp xếp kết quả theo: ${getSortLabel(sortBy)}`, 'info');
}

// Get sort label for display
function getSortLabel(sortValue) {
  switch (sortValue) {
    case 'date_desc':
      return 'Mới nhất';
    case 'rating_desc':
      return 'Đánh giá cao nhất';
    case 'downloads_desc':
      return 'Lượt tải nhiều nhất';
    case 'price_asc':
      return 'Giá thấp đến cao';
    case 'price_desc':
      return 'Giá cao đến thấp';
    case 'relevance':
    default:
      return 'Độ liên quan';
  }
}

// Update search display with query
function updateSearchDisplay(query) {
  // Update search query display
  const searchQueryDisplay = document.querySelector('.search-query-display');
  if (searchQueryDisplay) {
    if (query) {
      searchQueryDisplay.innerHTML = `<span>Kết quả cho:</span> <strong>"${query}"</strong>`;
    } else {
      searchQueryDisplay.innerHTML = `<span>Tất cả tài liệu</span>`;
    }
  }
  
  // In a real application, this would fetch results from an API and update the count
  // For this demo, we'll just show a toast message indicating the search
  if (query) {
    showToast(`Đang tìm kiếm: "${query}"`, 'info');
  }
}

// Show toast message
function showToast(message, type = 'info') {
  // Check if toast container exists, if not create it
  let toastContainer = document.querySelector('.toast-container');
  
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container';
    document.body.appendChild(toastContainer);
    
    // Add toast container styles if they don't exist
    if (!document.getElementById('toast-styles')) {
      const toastStyles = document.createElement('style');
      toastStyles.id = 'toast-styles';
      toastStyles.textContent = `
        .toast-container {
          position: fixed;
          bottom: 20px;
          right: 20px;
          z-index: 9999;
        }
        
        .toast {
          min-width: 250px;
          margin-top: 10px;
          padding: 15px 20px;
          border-radius: 4px;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          font-size: 14px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          transform: translateX(100%);
          opacity: 0;
          transition: transform 0.3s, opacity 0.3s;
        }
        
        .toast.show {
          transform: translateX(0);
          opacity: 1;
        }
        
        .toast-icon {
          margin-right: 10px;
          font-size: 16px;
        }
        
        .toast-message {
          flex-grow: 1;
        }
        
        .toast-close {
          margin-left: 10px;
          cursor: pointer;
          font-size: 16px;
          opacity: 0.7;
          transition: opacity 0.2s;
        }
        
        .toast-close:hover {
          opacity: 1;
        }
        
        .toast-info {
          background-color: #f0f7ff;
          color: #0066cc;
          border-left: 4px solid #0066cc;
        }
        
        .toast-success {
          background-color: #f0fff4;
          color: #38a169;
          border-left: 4px solid #38a169;
        }
        
        .toast-warning {
          background-color: #fffbeb;
          color: #d97706;
          border-left: 4px solid #d97706;
        }
        
        .toast-error {
          background-color: #fff5f5;
          color: #e53e3e;
          border-left: 4px solid #e53e3e;
        }
        
        @media (max-width: 576px) {
          .toast-container {
            bottom: 0;
            right: 0;
            left: 0;
          }
          
          .toast {
            min-width: auto;
            width: calc(100% - 20px);
            margin: 10px;
            border-radius: 4px;
          }
        }
      `;
      document.head.appendChild(toastStyles);
    }
  }
  
  // Create toast element
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  // Icon based on type
  let icon;
  switch (type) {
    case 'success':
      icon = 'fas fa-check-circle';
      break;
    case 'warning':
      icon = 'fas fa-exclamation-triangle';
      break;
    case 'error':
      icon = 'fas fa-times-circle';
      break;
    case 'info':
    default:
      icon = 'fas fa-info-circle';
      break;
  }
  
  // Set toast content
  toast.innerHTML = `
    <div class="toast-icon"><i class="${icon}"></i></div>
    <div class="toast-message">${message}</div>
    <div class="toast-close"><i class="fas fa-times"></i></div>
  `;
  
  // Add to container
  toastContainer.appendChild(toast);
  
  // Show toast with animation
  setTimeout(() => {
    toast.classList.add('show');
  }, 10);
  
  // Add close button event
  const closeBtn = toast.querySelector('.toast-close');
  if (closeBtn) {
    closeBtn.addEventListener('click', function() {
      toast.classList.remove('show');
      setTimeout(() => {
        toastContainer.removeChild(toast);
      }, 300);
    });
  }
  
  // Auto remove after 4 seconds
  setTimeout(() => {
    if (toast.parentNode === toastContainer) {
      toast.classList.remove('show');
      setTimeout(() => {
        if (toast.parentNode === toastContainer) {
          toastContainer.removeChild(toast);
        }
      }, 300);
    }
  }, 4000);
}