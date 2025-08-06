// Document Detail JavaScript
document.addEventListener('DOMContentLoaded', function() {
  initTabs();
  initPreviewPages();
  initTooltips();
  initBookmark();
  initShareButton();
  initRatingStars();
  initReviewForm();
  initStickyElements();
  initDocumentActions();
  initLightbox();
  initAuthorFollow();
  initReviewFilter();
  initDocumentTypeSelection();
});

// Initialize tabs (Description, Reviews, etc.)
function initTabs() {
  const tabs = document.querySelectorAll('.content-tab');
  const tabContents = document.querySelectorAll('.tab-content');
  
  if (tabs.length && tabContents.length) {
    tabs.forEach(tab => {
      tab.addEventListener('click', function() {
        const target = this.getAttribute('data-tab');
        
        // Remove active class from all tabs and content
        tabs.forEach(t => t.classList.remove('active'));
        tabContents.forEach(c => c.classList.remove('active'));
        
        // Add active class to clicked tab and corresponding content
        this.classList.add('active');
        document.getElementById(`${target}-content`).classList.add('active');
        
        // Save active tab to localStorage
        localStorage.setItem('activeDocumentTab', target);
      });
    });
    
    // Check for active tab in localStorage and activate it
    const savedTab = localStorage.getItem('activeDocumentTab');
    if (savedTab) {
      const tabToActivate = document.querySelector(`.content-tab[data-tab="${savedTab}"]`);
      if (tabToActivate) {
        tabToActivate.click();
      } else {
        // If saved tab doesn't exist, activate first tab
        tabs[0].click();
      }
    } else {
      // Activate first tab by default
      tabs[0].click();
    }
  }
}

// Initialize preview pages
function initPreviewPages() {
  const previewPages = document.querySelectorAll('.preview-page');
  const previewImage = document.querySelector('.preview-image img');
  
  if (previewPages.length && previewImage) {
    previewPages.forEach(page => {
      page.addEventListener('click', function() {
        // Get image source from clicked preview
        const imgSrc = this.querySelector('img').getAttribute('src');
        
        // Update main preview image
        previewImage.setAttribute('src', imgSrc);
        
        // Update active state
        previewPages.forEach(p => p.classList.remove('active'));
        this.classList.add('active');
      });
    });
    
    // Activate first preview page by default
    previewPages[0]?.classList.add('active');
  }
}

// Initialize tooltips
function initTooltips() {
  const tooltips = document.querySelectorAll('[data-tooltip]');
  
  if (tooltips.length) {
    tooltips.forEach(tooltip => {
      const tooltipText = tooltip.getAttribute('data-tooltip');
      
      // Create tooltip element
      const tooltipElement = document.createElement('div');
      tooltipElement.className = 'tooltip';
      tooltipElement.textContent = tooltipText;
      
      // Add tooltip element to body
      document.body.appendChild(tooltipElement);
      
      // Show tooltip on hover
      tooltip.addEventListener('mouseenter', function(e) {
        const rect = this.getBoundingClientRect();
        const tooltipWidth = tooltipElement.offsetWidth;
        
        // Position tooltip
        tooltipElement.style.top = `${rect.top - tooltipElement.offsetHeight - 10 + window.scrollY}px`;
        tooltipElement.style.left = `${rect.left + (rect.width / 2) - (tooltipWidth / 2) + window.scrollX}px`;
        tooltipElement.style.opacity = '1';
        tooltipElement.style.visibility = 'visible';
      });
      
      // Hide tooltip on leave
      tooltip.addEventListener('mouseleave', function() {
        tooltipElement.style.opacity = '0';
        tooltipElement.style.visibility = 'hidden';
      });
    });
    
    // Add tooltip styles if not already present
    if (!document.getElementById('tooltip-styles')) {
      const tooltipStyles = document.createElement('style');
      tooltipStyles.id = 'tooltip-styles';
      tooltipStyles.textContent = `
        .tooltip {
          position: absolute;
          background-color: rgba(0, 0, 0, 0.8);
          color: white;
          padding: 5px 10px;
          border-radius: 4px;
          font-size: 12px;
          z-index: 1000;
          opacity: 0;
          visibility: hidden;
          transition: opacity 0.2s, visibility 0.2s;
          text-align: center;
          pointer-events: none;
          white-space: nowrap;
        }
        
        .tooltip::after {
          content: '';
          position: absolute;
          top: 100%;
          left: 50%;
          margin-left: -5px;
          border-width: 5px;
          border-style: solid;
          border-color: rgba(0, 0, 0, 0.8) transparent transparent transparent;
        }
      `;
      document.head.appendChild(tooltipStyles);
    }
  }
}

// Initialize bookmark button
function initBookmark() {
  const bookmarkButton = document.querySelector('.bookmark-button');
  
  if (bookmarkButton) {
    bookmarkButton.addEventListener('click', function() {
      // Toggle active class
      this.classList.toggle('active');
      
      // Update icon and text based on state
      const bookmarkIcon = this.querySelector('i');
      const bookmarkText = this.querySelector('span');
      
      if (this.classList.contains('active')) {
        bookmarkIcon.className = 'fas fa-bookmark';
        bookmarkText.textContent = 'Đã lưu';
        showToast('Đã thêm vào danh sách yêu thích', 'success');
      } else {
        bookmarkIcon.className = 'far fa-bookmark';
        bookmarkText.textContent = 'Lưu';
        showToast('Đã xóa khỏi danh sách yêu thích', 'info');
      }
      
      // In a real application, this would make an API call to update bookmark status
    });
  }
}

// Initialize share button and modal
function initShareButton() {
  const shareButton = document.querySelector('.share-button');
  const shareModalOverlay = document.getElementById('shareModal');
  const shareModalClose = document.querySelector('.share-modal-close');
  const shareLinkInput = document.getElementById('shareLink');
  const shareLinkCopy = document.getElementById('shareLinkCopy');
  const shareEmbedCode = document.getElementById('shareEmbedCode');
  const shareEmbedCopy = document.getElementById('shareEmbedCopy');
  
  // Share options (Facebook, Twitter, etc.)
  const shareOptions = document.querySelectorAll('.share-option');
  
  if (shareButton && shareModalOverlay) {
    // Open share modal when share button is clicked
    shareButton.addEventListener('click', function() {
      shareModalOverlay.classList.add('show');
      
      // Set current URL in share link input
      if (shareLinkInput) {
        shareLinkInput.value = window.location.href || '';
      }
      
      // Set embed code
      if (shareEmbedCode) {
        const documentTitle = document.querySelector('.document-title')?.textContent || 'Tài liệu';
        shareEmbedCode.value = `<iframe src="${window.location.href || ''}?embed=true" title="${documentTitle}" width="100%" height="600" frameborder="0"></iframe>`;
      }
    });
    
    // Close share modal when close button is clicked
    if (shareModalClose) {
      shareModalClose.addEventListener('click', function() {
        shareModalOverlay.classList.remove('show');
      });
    }
    
    // Close share modal when clicking outside the modal
    shareModalOverlay.addEventListener('click', function(e) {
      if (e.target === shareModalOverlay) {
        shareModalOverlay.classList.remove('show');
      }
    });
    
    // Copy share link when copy button is clicked
    if (shareLinkInput && shareLinkCopy) {
      shareLinkCopy.addEventListener('click', function() {
        shareLinkInput.select();
        document.execCommand('copy');
        
        // Show success message
        const originalText = this.innerHTML;
        this.innerHTML = '<i class="fas fa-check"></i> Đã sao chép';
        
        setTimeout(() => {
          this.innerHTML = originalText;
        }, 2000);
        
        showToast('Đã sao chép liên kết vào clipboard', 'success');
      });
    }
    
    // Copy embed code when copy button is clicked
    if (shareEmbedCode && shareEmbedCopy) {
      shareEmbedCopy.addEventListener('click', function() {
        shareEmbedCode.select();
        document.execCommand('copy');
        
        // Show success message
        const originalText = this.innerHTML;
        this.innerHTML = '<i class="fas fa-check"></i> Đã sao chép';
        
        setTimeout(() => {
          this.innerHTML = originalText;
        }, 2000);
        
        showToast('Đã sao chép mã nhúng vào clipboard', 'success');
      });
    }
    
    // Handle share options (Facebook, Twitter, etc.)
    if (shareOptions.length) {
      shareOptions.forEach(option => {
        option.addEventListener('click', function() {
          const type = this.getAttribute('data-type');
          const url = encodeURIComponent(window.location.href || '');
          const title = encodeURIComponent(document.querySelector('.document-title')?.textContent || 'Tài liệu');
          
          let shareUrl = '';
          
          switch (type) {
            case 'facebook':
              shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${url}`;
              break;
            case 'twitter':
              shareUrl = `https://twitter.com/intent/tweet?url=${url}&text=${title}`;
              break;
            case 'linkedin':
              shareUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${url}`;
              break;
            case 'email':
              shareUrl = `mailto:?subject=${title}&body=${url}`;
              break;
          }
          
          if (shareUrl) {
            window.open(shareUrl, '_blank');
          }
        });
      });
    }
  }
}

// Initialize rating stars in the review form
function initRatingStars() {
  const rateStars = document.querySelectorAll('.rate-star');
  const ratingInput = document.getElementById('ratingInput');
  
  if (rateStars.length && ratingInput) {
    rateStars.forEach((star, index) => {
      // Set data-value attribute for each star
      star.setAttribute('data-value', index + 1);
      
      // Hover effect
      star.addEventListener('mouseenter', function() {
        const value = parseInt(this.getAttribute('data-value'));
        
        // Highlight stars up to the hovered one
        rateStars.forEach((s, i) => {
          if (i < value) {
            s.classList.add('active');
          } else {
            s.classList.remove('active');
          }
        });
      });
      
      // Click to select rating
      star.addEventListener('click', function() {
        const value = parseInt(this.getAttribute('data-value'));
        
        // Set the rating value in the hidden input
        ratingInput.value = value;
        
        // Highlight selected stars permanently
        rateStars.forEach((s, i) => {
          if (i < value) {
            s.classList.add('active');
          } else {
            s.classList.remove('active');
          }
        });
        
        // Add selected class to indicate this is the actual selection
        rateStars.forEach(s => s.classList.remove('selected'));
        this.classList.add('selected');
      });
    });
    
    // Reset stars when mouse leaves the rating container
    const ratingContainer = document.querySelector('.form-rating');
    if (ratingContainer) {
      ratingContainer.addEventListener('mouseleave', function() {
        const selectedValue = parseInt(ratingInput.value || 0);
        
        // Reset to selected stars
        rateStars.forEach((s, i) => {
          if (i < selectedValue) {
            s.classList.add('active');
          } else {
            s.classList.remove('active');
          }
        });
      });
    }
  }
}

// Initialize review form submission
function initReviewForm() {
  const reviewForm = document.getElementById('reviewForm');
  
  if (reviewForm) {
    reviewForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      // Get form values
      const rating = document.getElementById('ratingInput').value;
      const reviewText = document.getElementById('reviewText').value;
      
      // Validate form
      if (!rating) {
        showToast('Vui lòng chọn số sao đánh giá', 'warning');
        return;
      }
      
      if (!reviewText.trim()) {
        showToast('Vui lòng nhập nội dung đánh giá', 'warning');
        return;
      }
      
      // Show loading state
      const submitButton = reviewForm.querySelector('button[type="submit"]');
      const originalButtonText = submitButton.innerHTML;
      submitButton.disabled = true;
      submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
      
      // Simulate API call with timeout
      setTimeout(() => {
        // Reset form
        reviewForm.reset();
        document.querySelectorAll('.rate-star').forEach(star => star.classList.remove('active', 'selected'));
        document.getElementById('ratingInput').value = '';
        
        // Reset button
        submitButton.disabled = false;
        submitButton.innerHTML = originalButtonText;
        
        // Show success message
        showToast('Cảm ơn bạn đã gửi đánh giá!', 'success');
        
        // In a real application, this would reload reviews from the server
        // For now, we'll just simulate adding a new review to the list
        addNewReview({
          avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-1.2.1&auto=format&fit=crop&w=256&h=256&q=80',
          name: 'Bạn',
          date: 'Vừa xong',
          rating: parseInt(rating),
          text: reviewText
        });
      }, 1500);
    });
  }
}

// Add a new review to the review list
function addNewReview(review) {
  const reviewList = document.querySelector('.review-list');
  
  if (reviewList) {
    // Create new review element
    const reviewItem = document.createElement('div');
    reviewItem.className = 'review-item';
    
    // Generate rating stars HTML
    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
      if (i <= review.rating) {
        starsHtml += '<i class="fas fa-star"></i>';
      } else {
        starsHtml += '<i class="far fa-star"></i>';
      }
    }
    
    // Set review HTML
    reviewItem.innerHTML = `
      <div class="review-header">
        <div class="reviewer-info">
          <div class="reviewer-avatar">
            <img src="${review.avatar}" alt="${review.name}">
          </div>
          <div class="reviewer-details">
            <div class="reviewer-name">${review.name}</div>
            <div class="review-date">${review.date}</div>
          </div>
        </div>
        <div class="review-rating">
          ${starsHtml}
        </div>
      </div>
      <div class="review-body">
        <p>${review.text}</p>
      </div>
      <div class="review-footer">
        <div class="review-actions">
          <div class="review-action">
            <i class="far fa-thumbs-up"></i> Hữu ích
          </div>
          <div class="review-action">
            <i class="far fa-flag"></i> Báo cáo
          </div>
        </div>
        <div class="review-helpful">
          0 người thấy hữu ích
        </div>
      </div>
    `;
    
    // Add new review to the top of the list
    reviewList.insertBefore(reviewItem, reviewList.firstChild);
    
    // Add event listeners to the new review actions
    const reviewActions = reviewItem.querySelectorAll('.review-action');
    reviewActions.forEach(action => {
      action.addEventListener('click', function() {
        const isHelpful = this.querySelector('i').classList.contains('fa-thumbs-up');
        
        if (isHelpful) {
          // Toggle helpful action
          const icon = this.querySelector('i');
          if (icon.classList.contains('far')) {
            icon.classList.remove('far');
            icon.classList.add('fas');
            this.style.color = 'var(--primary)';
            showToast('Cảm ơn bạn đã đánh giá', 'success');
          } else {
            icon.classList.remove('fas');
            icon.classList.add('far');
            this.style.color = '';
            showToast('Đã hủy đánh giá', 'info');
          }
        } else {
          // Report action
          showToast('Cảm ơn bạn đã báo cáo. Chúng tôi sẽ xem xét đánh giá này.', 'info');
        }
      });
    });
  }
}

// Initialize sticky elements
function initStickyElements() {
  const documentSidebar = document.querySelector('.document-sidebar');
  const contentTabs = document.querySelector('.content-tabs');
  
  if (documentSidebar) {
    window.addEventListener('scroll', () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      
      // Make sure the sidebar doesn't go below the footer
      const footer = document.querySelector('.footer');
      const footerTop = footer.getBoundingClientRect().top + window.pageYOffset;
      const sidebarHeight = documentSidebar.offsetHeight;
      const sidebarTop = documentSidebar.getBoundingClientRect().top + window.pageYOffset;
      
      if (scrollTop + sidebarHeight + 40 >= footerTop) {
        documentSidebar.style.top = `${footerTop - sidebarHeight - scrollTop - 40}px`;
      } else {
        documentSidebar.style.top = 'var(--spacing-6)';
      }
    });
  }
}

// Initialize document actions (download, report, etc.)
function initDocumentActions() {
  const downloadButton = document.querySelector('.download-button');
  const reportButton = document.querySelector('.report-button');
  const reviewHelpfulButtons = document.querySelectorAll('.review-action');
  
  // Handle download button click
  if (downloadButton) {
    downloadButton.addEventListener('click', function() {
      const isPremium = this.getAttribute('data-premium') === 'true';
      
      if (isPremium) {
        // Show download confirmation modal for premium documents
        showDownloadConfirmationModal();
      } else {
        // For free documents, simulate direct download
        showToast('Đang tải xuống tài liệu...', 'info');
        
        // Simulate download delay
        setTimeout(() => {
          // In a real application, this would trigger the actual file download
          // For demo purposes, let's just show a success message
          showToast('Tải xuống hoàn tất!', 'success');
        }, 2000);
      }
    });
  }
  
  // Handle report button click
  if (reportButton) {
    reportButton.addEventListener('click', function() {
      // Show report confirmation
      if (confirm('Bạn muốn báo cáo tài liệu này vi phạm?')) {
        showToast('Cảm ơn bạn đã báo cáo. Chúng tôi sẽ xem xét tài liệu này.', 'info');
      }
    });
  }
  
  // Handle review helpful buttons
  if (reviewHelpfulButtons.length) {
    reviewHelpfulButtons.forEach(button => {
      button.addEventListener('click', function() {
        const isHelpful = this.querySelector('i').classList.contains('fa-thumbs-up');
        
        if (isHelpful) {
          // Toggle helpful action
          const icon = this.querySelector('i');
          if (icon.classList.contains('far')) {
            icon.classList.remove('far');
            icon.classList.add('fas');
            this.style.color = 'var(--primary)';
            
            // Update helpful count
            const helpfulCount = this.closest('.review-footer').querySelector('.review-helpful');
            if (helpfulCount) {
              const count = parseInt(helpfulCount.textContent.split(' ')[0]) + 1;
              helpfulCount.textContent = `${count} người thấy hữu ích`;
            }
            
            showToast('Cảm ơn bạn đã đánh giá', 'success');
          } else {
            icon.classList.remove('fas');
            icon.classList.add('far');
            this.style.color = '';
            
            // Update helpful count
            const helpfulCount = this.closest('.review-footer').querySelector('.review-helpful');
            if (helpfulCount) {
              const count = Math.max(0, parseInt(helpfulCount.textContent.split(' ')[0]) - 1);
              helpfulCount.textContent = `${count} người thấy hữu ích`;
            }
            
            showToast('Đã hủy đánh giá', 'info');
          }
        } else {
          // Report action
          showToast('Cảm ơn bạn đã báo cáo. Chúng tôi sẽ xem xét đánh giá này.', 'info');
        }
      });
    });
  }
}

// Show download confirmation modal
function showDownloadConfirmationModal() {
  // Check if modal already exists
  let downloadModal = document.getElementById('downloadModal');
  
  if (!downloadModal) {
    // Create modal if it doesn't exist
    downloadModal = document.createElement('div');
    downloadModal.id = 'downloadModal';
    downloadModal.className = 'download-modal-overlay';
    
    // Get document info
    const documentTitle = document.querySelector('.document-title')?.textContent || 'Tài liệu';
    const documentPrice = document.querySelector('.document-price')?.textContent || '';
    const documentFormat = document.querySelector('.document-format')?.textContent || 'PDF';
    const documentSize = document.querySelector('.info-value[data-info="size"]')?.textContent || '';
    const documentPages = document.querySelector('.info-value[data-info="pages"]')?.textContent || '';
    
    // Create modal content
    downloadModal.innerHTML = `
      <div class="download-modal">
        <div class="download-modal-header">
          <div class="document-icon">
            <i class="fas fa-file-pdf"></i>
          </div>
          <h3 class="download-modal-title">Xác nhận tải xuống</h3>
          <div class="download-document-name">${documentTitle}</div>
          <button class="download-modal-close">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="download-modal-body">
          <div class="download-info">
            <div class="download-info-item">
              <div class="download-info-value">${documentFormat}</div>
              <div class="download-info-label">Định dạng</div>
            </div>
            <div class="download-info-item">
              <div class="download-info-value">${documentSize}</div>
              <div class="download-info-label">Kích thước</div>
            </div>
            <div class="download-info-item">
              <div class="download-info-value">${documentPages}</div>
              <div class="download-info-label">Số trang</div>
            </div>
            <div class="download-info-item">
              <div class="download-info-value">Cao cấp</div>
              <div class="download-info-label">Phân loại</div>
            </div>
          </div>
          
          <div class="download-cost">
            <div class="download-cost-label">Chi phí:</div>
            <div class="download-cost-value">
              <i class="fas fa-coins"></i> ${documentPrice}
            </div>
          </div>
          
          <div class="download-actions">
            <button id="confirmDownload" class="download-confirm-btn">
              <i class="fas fa-download"></i> Xác nhận tải xuống
            </button>
            <button id="cancelDownload" class="download-cancel-btn">
              <i class="fas fa-times"></i> Hủy
            </button>
          </div>
        </div>
      </div>
    `;
    
    // Add modal to document
    document.body.appendChild(downloadModal);
    
    // Add event listeners
    const closeButton = downloadModal.querySelector('.download-modal-close');
    const cancelButton = downloadModal.querySelector('#cancelDownload');
    const confirmButton = downloadModal.querySelector('#confirmDownload');
    
    closeButton.addEventListener('click', () => {
      downloadModal.classList.remove('show');
    });
    
    cancelButton.addEventListener('click', () => {
      downloadModal.classList.remove('show');
    });
    
    downloadModal.addEventListener('click', (e) => {
      if (e.target === downloadModal) {
        downloadModal.classList.remove('show');
      }
    });
    
    confirmButton.addEventListener('click', () => {
      // Show loading state
      confirmButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
      confirmButton.disabled = true;
      
      // Simulate API call with timeout
      setTimeout(() => {
        // In a real application, this would check if the user has enough coins
        // For demo purposes, let's assume the user has enough
        downloadModal.classList.remove('show');
        
        // Reset button state after modal closes
        setTimeout(() => {
          confirmButton.innerHTML = '<i class="fas fa-download"></i> Xác nhận tải xuống';
          confirmButton.disabled = false;
        }, 300);
        
        // Show download progress
        showToast('Đang tải xuống tài liệu...', 'info');
        
        // Simulate download completion
        setTimeout(() => {
          showToast('Tải xuống hoàn tất!', 'success');
        }, 2000);
      }, 1500);
    });
  }
  
  // Show modal
  downloadModal.classList.add('show');
}

// Initialize author follow button
function initAuthorFollow() {
  const authorFollowBtn = document.querySelector('.author-follow-btn');
  
  if (authorFollowBtn) {
    authorFollowBtn.addEventListener('click', function() {
      // Toggle follow state
      this.classList.toggle('following');
      
      if (this.classList.contains('following')) {
        // Update button text and style for following state
        this.innerHTML = '<i class="fas fa-user-check"></i> Đang theo dõi';
        this.style.backgroundColor = 'var(--primary)';
        showToast('Bạn đã bắt đầu theo dõi tác giả này', 'success');
      } else {
        // Reset button text and style
        this.innerHTML = '<i class="fas fa-user-plus"></i> Theo dõi tác giả';
        this.style.backgroundColor = '';
        showToast('Bạn đã hủy theo dõi tác giả này', 'info');
      }
      
      // In a real application, this would make an API call to update follow status
    });
  }
}

// Initialize review filter functionality
function initReviewFilter() {
  const filterSelect = document.querySelector('.filter-select');
  const sortSelect = document.querySelector('.sort-select');
  const reviewItems = document.querySelectorAll('.review-item');
  
  if (filterSelect && reviewItems.length) {
    filterSelect.addEventListener('change', function() {
      const rating = this.value;
      
      reviewItems.forEach(item => {
        if (rating === 'all') {
          item.style.display = '';
        } else {
          // Count stars in this review
          const stars = item.querySelectorAll('.review-rating .fas.fa-star').length;
          
          if (stars === parseInt(rating)) {
            item.style.display = '';
          } else {
            item.style.display = 'none';
          }
        }
      });
      
      showToast(`Hiển thị đánh giá: ${rating === 'all' ? 'Tất cả' : rating + ' sao'}`, 'info');
    });
  }
  
  if (sortSelect && reviewItems.length) {
    sortSelect.addEventListener('change', function() {
      const sortBy = this.value;
      const reviewList = document.querySelector('.review-list');
      
      if (reviewList) {
        const reviewsArray = Array.from(reviewItems);
        
        // Sort reviews based on selected option
        reviewsArray.sort((a, b) => {
          if (sortBy === 'recent') {
            // Sort by date (newest first)
            const dateA = a.querySelector('.review-date').textContent;
            const dateB = b.querySelector('.review-date').textContent;
            
            // For demo purposes, we'll just reverse the current order
            return -1;
          } else if (sortBy === 'helpful') {
            // Sort by helpfulness (most helpful first)
            const helpfulA = parseInt(a.querySelector('.review-helpful').textContent);
            const helpfulB = parseInt(b.querySelector('.review-helpful').textContent);
            
            return helpfulB - helpfulA;
          } else if (sortBy === 'highest') {
            // Sort by rating (highest first)
            const starsA = a.querySelectorAll('.review-rating .fas.fa-star').length;
            const starsB = b.querySelectorAll('.review-rating .fas.fa-star').length;
            
            return starsB - starsA;
          } else if (sortBy === 'lowest') {
            // Sort by rating (lowest first)
            const starsA = a.querySelectorAll('.review-rating .fas.fa-star').length;
            const starsB = b.querySelectorAll('.review-rating .fas.fa-star').length;
            
            return starsA - starsB;
          }
          
          return 0;
        });
        
        // Remove all reviews from the DOM
        reviewItems.forEach(item => item.remove());
        
        // Append sorted reviews back to the list
        reviewsArray.forEach(item => reviewList.appendChild(item));
        
        showToast(`Đã sắp xếp đánh giá: ${sortBy === 'recent' ? 'Mới nhất' : 
                                         sortBy === 'helpful' ? 'Hữu ích nhất' : 
                                         sortBy === 'highest' ? 'Đánh giá cao nhất' : 
                                         'Đánh giá thấp nhất'}`, 'info');
      }
    });
  }
}

// Initialize document type selection
function initDocumentTypeSelection() {
  const documentTypes = document.querySelectorAll('.document-type');
  const downloadButton = document.querySelector('.download-button');
  
  if (documentTypes.length && downloadButton) {
    documentTypes.forEach(type => {
      type.addEventListener('click', function() {
        // Remove active class from all types
        documentTypes.forEach(t => t.classList.remove('active'));
        
        // Add active class to clicked type
        this.classList.add('active');
        
        // Get document type information
        const typeName = this.querySelector('.document-type-name').textContent;
        const typeSize = this.querySelector('.document-type-size').textContent;
        
        // Update download button text
        downloadButton.innerHTML = `<i class="fas fa-download"></i> Tải xuống (${typeName})`;
        
        // Update document information in the UI
        const documentFormat = document.querySelector('.document-format');
        const documentSize = document.querySelector('.info-value[data-info="size"]');
        
        if (documentFormat) {
          documentFormat.className = 'document-format';
          documentFormat.classList.add(typeName.toLowerCase());
          
          // Update icon based on format
          const icon = documentFormat.querySelector('i');
          if (icon) {
            icon.className = typeName.toLowerCase() === 'pdf' ? 'fas fa-file-pdf' : 
                             typeName.toLowerCase() === 'docx' ? 'fas fa-file-word' : 
                             'fas fa-file-code';
          }
          
          documentFormat.innerHTML = `<i class="${icon.className}"></i> ${typeName}`;
        }
        
        if (documentSize) {
          documentSize.textContent = typeSize;
        }
        
        showToast(`Đã chọn định dạng ${typeName}`, 'info');
      });
    });
  }
}

// Initialize lightbox for preview images
function initLightbox() {
  const previewImage = document.querySelector('.preview-image img');
  const previewPages = document.querySelectorAll('.preview-page img');
  
  if (previewImage && previewPages.length) {
    // Create lightbox elements
    const lightbox = document.createElement('div');
    lightbox.className = 'lightbox-overlay';
    lightbox.innerHTML = `
      <div class="lightbox-content">
        <img src="" class="lightbox-image" alt="Preview">
        <button class="lightbox-close">
          <i class="fas fa-times"></i>
        </button>
        <div class="lightbox-navigation">
          <button class="lightbox-nav-button prev">
            <i class="fas fa-chevron-left"></i>
          </button>
          <button class="lightbox-nav-button next">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
        <div class="lightbox-counter">1 / ${previewPages.length}</div>
      </div>
    `;
    
    // Add lightbox to document
    document.body.appendChild(lightbox);
    
    // Get lightbox elements
    const lightboxImage = lightbox.querySelector('.lightbox-image');
    const lightboxClose = lightbox.querySelector('.lightbox-close');
    const lightboxPrev = lightbox.querySelector('.lightbox-nav-button.prev');
    const lightboxNext = lightbox.querySelector('.lightbox-nav-button.next');
    const lightboxCounter = lightbox.querySelector('.lightbox-counter');
    
    // Set current image index
    let currentIndex = 0;
    
    // Show lightbox for main preview image
    previewImage.addEventListener('click', function() {
      // Find the active preview page and get its index
      const activePage = document.querySelector('.preview-page.active');
      if (activePage) {
        const pages = Array.from(document.querySelectorAll('.preview-page'));
        currentIndex = pages.indexOf(activePage);
      } else {
        currentIndex = 0;
      }
      
      // Set lightbox image
      lightboxImage.src = this.src;
      
      // Update counter
      lightboxCounter.textContent = `${currentIndex + 1} / ${previewPages.length}`;
      
      // Show lightbox
      lightbox.classList.add('show');
    });
    
    // Show lightbox for preview page images
    previewPages.forEach((page, index) => {
      page.addEventListener('click', function() {
        // Set current index
        currentIndex = index;
        
        // Set lightbox image
        lightboxImage.src = this.src;
        
        // Update counter
        lightboxCounter.textContent = `${currentIndex + 1} / ${previewPages.length}`;
        
        // Show lightbox
        lightbox.classList.add('show');
      });
    });
    
    // Close lightbox
    lightboxClose.addEventListener('click', () => {
      lightbox.classList.remove('show');
    });
    
    // Close lightbox when clicking outside the image
    lightbox.addEventListener('click', (e) => {
      if (e.target === lightbox) {
        lightbox.classList.remove('show');
      }
    });
    
    // Navigate to previous image
    lightboxPrev.addEventListener('click', (e) => {
      e.stopPropagation();
      
      // Update current index
      currentIndex = (currentIndex - 1 + previewPages.length) % previewPages.length;
      
      // Update lightbox image
      lightboxImage.src = previewPages[currentIndex].src;
      
      // Update counter
      lightboxCounter.textContent = `${currentIndex + 1} / ${previewPages.length}`;
    });
    
    // Navigate to next image
    lightboxNext.addEventListener('click', (e) => {
      e.stopPropagation();
      
      // Update current index
      currentIndex = (currentIndex + 1) % previewPages.length;
      
      // Update lightbox image
      lightboxImage.src = previewPages[currentIndex].src;
      
      // Update counter
      lightboxCounter.textContent = `${currentIndex + 1} / ${previewPages.length}`;
    });
    
    // Keyboard navigation
    document.addEventListener('keydown', (e) => {
      if (!lightbox.classList.contains('show')) return;
      
      if (e.key === 'Escape') {
        lightbox.classList.remove('show');
      } else if (e.key === 'ArrowLeft') {
        lightboxPrev.click();
      } else if (e.key === 'ArrowRight') {
        lightboxNext.click();
      }
    });
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