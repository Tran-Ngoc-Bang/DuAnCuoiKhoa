// Documents functionality for EduShare

// DOM Elements
const documentCards = document.querySelectorAll('.document-card');
const documentDetailsContainer = document.getElementById('documentDetails');
const relatedDocumentsContainer = document.getElementById('relatedDocuments');
const documentViewerContainer = document.getElementById('documentViewer');
const downloadButtons = document.querySelectorAll('.download-btn');
const shareButtons = document.querySelectorAll('.share-btn');
const bookmarkButtons = document.querySelectorAll('.bookmark-btn');
const ratingStars = document.querySelectorAll('.rating-star');
const commentForm = document.getElementById('commentForm');

// Document data (for demo purposes)
const documentData = [
  {
    id: 1,
    title: 'Lập trình ứng dụng web với React và NodeJS',
    description: 'Giới thiệu tổng quan về React, NodeJS và cách xây dựng ứng dụng web full-stack hiện đại. Tài liệu bao gồm các ví dụ thực tế và hướng dẫn từng bước để xây dựng một ứng dụng hoàn chỉnh.',
    category: 'Công nghệ thông tin',
    categorySlug: 'it',
    author: {
      name: 'GS. Nguyễn Văn A',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      institution: 'Đại học Công nghệ'
    },
    coverImage: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
    rating: 4.0,
    reviewCount: 120,
    downloadCount: 1248,
    viewCount: 5437,
    dateUpdated: '12/05/2023',
    price: 25,
    isPremium: true,
    pages: 85,
    fileSize: '3.7 MB',
    fileFormat: 'PDF',
    language: 'Tiếng Việt',
    tags: ['React', 'NodeJS', 'JavaScript', 'Web Development', 'Full-stack'],
    content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vitae justo vel purus sollicitudin convallis. Praesent commodo cursus magna, vel scelerisque nisl consectetur et.'
  },
  {
    id: 2,
    title: 'Kế hoạch kinh doanh: Từ ý tưởng đến thực hiện',
    description: 'Hướng dẫn chi tiết cách xây dựng kế hoạch kinh doanh hoàn chỉnh từ ý tưởng ban đầu. Tài liệu cung cấp các mẫu, ví dụ thực tế và phương pháp phân tích thị trường hiệu quả.',
    category: 'Kinh tế - Quản trị',
    categorySlug: 'business',
    author: {
      name: 'ThS. Trần Thị B',
      avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      institution: 'Đại học Kinh tế'
    },
    coverImage: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
    rating: 5.0,
    reviewCount: 89,
    downloadCount: 3845,
    viewCount: 12785,
    dateUpdated: '05/06/2023',
    price: 0,
    isPremium: false,
    pages: 120,
    fileSize: '5.2 MB',
    fileFormat: 'PDF',
    language: 'Tiếng Việt',
    tags: ['Kinh doanh', 'Startup', 'Kế hoạch', 'Marketing', 'Tài chính'],
    content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vitae justo vel purus sollicitudin convallis. Praesent commodo cursus magna, vel scelerisque nisl consectetur et.'
  },
  {
    id: 3,
    title: 'IELTS Writing: Cách đạt điểm 7.0+ cho Task 1 và 2',
    description: 'Phương pháp luyện thi và các mẫu bài viết đạt điểm cao cho phần thi Writing IELTS. Tài liệu bao gồm các kỹ thuật viết hiệu quả, cấu trúc bài thi và nhiều ví dụ thực tế.',
    category: 'Ngoại ngữ',
    categorySlug: 'language',
    author: {
      name: 'TS. Lê Văn C',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      institution: 'Trung tâm Anh ngữ XYZ'
    },
    coverImage: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
    rating: 4.5,
    reviewCount: 215,
    downloadCount: 2547,
    viewCount: 8372,
    dateUpdated: '28/05/2023',
    price: 50,
    isPremium: true,
    pages: 75,
    fileSize: '3.1 MB',
    fileFormat: 'PDF',
    language: 'Tiếng Anh',
    tags: ['IELTS', 'Writing', 'English', 'Task 1', 'Task 2', 'Academic'],
    content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vitae justo vel purus sollicitudin convallis. Praesent commodo cursus magna, vel scelerisque nisl consectetur et.'
  },
  {
    id: 4,
    title: 'Nhập môn Machine Learning và Trí tuệ nhân tạo',
    description: 'Tổng quan về các thuật toán Machine Learning cơ bản và ứng dụng trong thực tiễn. Bao gồm hướng dẫn cài đặt môi trường và thực hành với Python.',
    category: 'Công nghệ thông tin',
    categorySlug: 'it',
    author: {
      name: 'PGS.TS Phạm Văn D',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
      institution: 'Đại học Bách Khoa'
    },
    coverImage: 'https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
    rating: 4.8,
    reviewCount: 152,
    downloadCount: 2890,
    viewCount: 7621,
    dateUpdated: '10/06/2023',
    price: 45,
    isPremium: true,
    pages: 150,
    fileSize: '8.2 MB',
    fileFormat: 'PDF',
    language: 'Tiếng Việt',
    tags: ['Machine Learning', 'AI', 'Python', 'Data Science', 'Algorithms'],
    content: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vitae justo vel purus sollicitudin convallis. Praesent commodo cursus magna, vel scelerisque nisl consectetur et.'
  }
];

// Initialize document functionality
document.addEventListener('DOMContentLoaded', function() {
  initDocumentCards();
  initDocumentDetails();
  initDocumentViewer();
  initDownloadButtons();
  initShareButtons();
  initBookmarkButtons();
  initRatingSystem();
  initCommentSystem();
});

// Initialize document cards
function initDocumentCards() {
  if (documentCards.length) {
    documentCards.forEach(card => {
      // Add event listeners for hover effects and interactions
      
      // Handle document card click
      const documentLink = card.querySelector('.document-title a, .detail-btn, .view-document-btn');
      if (documentLink) {
        const originalHref = documentLink.getAttribute('href');
        if (originalHref && !originalHref.includes('?id=')) {
          const documentId = card.getAttribute('data-id') || '1';
          documentLink.setAttribute('href', `${originalHref}?id=${documentId}`);
        }
      }
    });
  }
}

// Initialize document details page
function initDocumentDetails() {
  if (documentDetailsContainer) {
    // Get document ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    const documentId = parseInt(urlParams.get('id')) || 1;
    
    // Find document data
    const document = documentData.find(doc => doc.id === documentId) || documentData[0];
    
    // Update page title
    document.title = `${document.title} - EduShare`;
    
    // Update document details
    updateDocumentUI(document);
    
    // Load related documents
    loadRelatedDocuments(document);
    
    // Track document view (would use AJAX in production)
    trackDocumentView(documentId);
  }
}

// Update document UI with data
function updateDocumentUI(document) {
  // Update cover image
  const coverImage = documentDetailsContainer.querySelector('.document-cover img');
  if (coverImage) coverImage.src = document.coverImage;
  
  // Update title
  const title = documentDetailsContainer.querySelector('.document-title');
  if (title) title.textContent = document.title;
  
  // Update category
  const category = documentDetailsContainer.querySelector('.document-category');
  if (category) {
    category.textContent = document.category;
    category.classList.add(`${document.categorySlug}-category`);
  }
  
  // Update author
  const authorName = documentDetailsContainer.querySelector('.author-name');
  const authorAvatar = documentDetailsContainer.querySelector('.author-avatar');
  const authorInstitution = documentDetailsContainer.querySelector('.author-institution');
  
  if (authorName) authorName.textContent = document.author.name;
  if (authorAvatar) authorAvatar.src = document.author.avatar;
  if (authorInstitution) authorInstitution.textContent = document.author.institution;
  
  // Update description
  const description = documentDetailsContainer.querySelector('.document-description');
  if (description) description.textContent = document.description;
  
  // Update price
  const price = documentDetailsContainer.querySelector('.document-price .price');
  const priceWrapper = documentDetailsContainer.querySelector('.document-price');
  
  if (price && priceWrapper) {
    if (document.price > 0) {
      price.textContent = `${document.price} xu`;
      priceWrapper.classList.add('premium');
      priceWrapper.classList.remove('free');
    } else {
      price.textContent = 'Miễn phí';
      priceWrapper.classList.add('free');
      priceWrapper.classList.remove('premium');
    }
  }
  
  // Update badge
  const badge = documentDetailsContainer.querySelector('.document-badge');
  if (badge) {
    if (document.isPremium) {
      badge.classList.add('premium');
      badge.classList.remove('free');
      badge.innerHTML = '<i class="fas fa-crown"></i> Premium';
    } else {
      badge.classList.add('free');
      badge.classList.remove('premium');
      badge.innerHTML = '<i class="fas fa-unlock-alt"></i> Free';
    }
  }
  
  // Update metadata
  const dateUpdated = documentDetailsContainer.querySelector('.date-updated .meta-value');
  const pageCount = documentDetailsContainer.querySelector('.page-count .meta-value');
  const fileSize = documentDetailsContainer.querySelector('.file-size .meta-value');
  const fileFormat = documentDetailsContainer.querySelector('.file-format .meta-value');
  const language = documentDetailsContainer.querySelector('.language .meta-value');
  
  if (dateUpdated) dateUpdated.textContent = document.dateUpdated;
  if (pageCount) pageCount.textContent = document.pages;
  if (fileSize) fileSize.textContent = document.fileSize;
  if (fileFormat) fileFormat.textContent = document.fileFormat;
  if (language) language.textContent = document.language;
  
  // Update stats
  const ratingValue = documentDetailsContainer.querySelector('.rating-value');
  const reviewCount = documentDetailsContainer.querySelector('.review-count');
  const downloadCount = documentDetailsContainer.querySelector('.download-count .meta-value');
  const viewCount = documentDetailsContainer.querySelector('.view-count .meta-value');
  
  if (ratingValue) ratingValue.textContent = document.rating.toFixed(1);
  if (reviewCount) reviewCount.textContent = `(${document.reviewCount} đánh giá)`;
  if (downloadCount) downloadCount.textContent = document.downloadCount.toLocaleString();
  if (viewCount) viewCount.textContent = document.viewCount.toLocaleString();
  
  // Update stars
  const stars = documentDetailsContainer.querySelectorAll('.rating-stars .star');
  if (stars.length) {
    const fullStars = Math.floor(document.rating);
    const hasHalfStar = document.rating % 1 >= 0.5;
    
    stars.forEach((star, index) => {
      if (index < fullStars) {
        star.innerHTML = '<i class="fas fa-star"></i>';
      } else if (index === fullStars && hasHalfStar) {
        star.innerHTML = '<i class="fas fa-star-half-alt"></i>';
      } else {
        star.innerHTML = '<i class="far fa-star"></i>';
      }
    });
  }
  
  // Update tags
  const tagsContainer = documentDetailsContainer.querySelector('.document-tags');
  if (tagsContainer && document.tags) {
    tagsContainer.innerHTML = '';
    document.tags.forEach(tag => {
      const tagElement = document.createElement('span');
      tagElement.className = 'document-tag';
      tagElement.textContent = tag;
      tagsContainer.appendChild(tagElement);
    });
  }
  
  // Update download button
  const downloadBtn = documentDetailsContainer.querySelector('.download-btn');
  if (downloadBtn) {
    if (document.isPremium) {
      downloadBtn.innerHTML = `<i class="fas fa-download"></i> Tải xuống (${document.price} xu)`;
    } else {
      downloadBtn.innerHTML = '<i class="fas fa-download"></i> Tải xuống miễn phí';
    }
  }
}

// Load related documents
function loadRelatedDocuments(currentDocument) {
  if (!relatedDocumentsContainer) return;
  
  // Find documents in the same category
  const relatedDocs = documentData.filter(doc => 
    doc.category === currentDocument.category && doc.id !== currentDocument.id
  );
  
  if (relatedDocs.length === 0) return;
  
  // Clear container
  relatedDocumentsContainer.innerHTML = '';
  
  // Create and append document cards
  relatedDocs.slice(0, 3).forEach(doc => {
    const card = createDocumentCard(doc);
    relatedDocumentsContainer.appendChild(card);
  });
  
  // Re-initialize document cards
  initDocumentCards();
}

// Create document card element
function createDocumentCard(document) {
  const card = document.createElement('div');
  card.className = 'document-card';
  card.setAttribute('data-id', document.id);
  
  card.innerHTML = `
    <div class="document-image">
      <img src="${document.coverImage}" alt="${document.title}">
      <div class="document-badge ${document.isPremium ? 'premium' : 'free'}">
        <i class="fas ${document.isPremium ? 'fa-crown' : 'fa-unlock-alt'}"></i> ${document.isPremium ? 'Premium' : 'Free'}
      </div>
      <div class="document-author">
        <img src="${document.author.avatar}" alt="${document.author.name}" class="author-avatar">
        <span class="author-name">${document.author.name}</span>
      </div>
      <div class="document-overlay">
        <a href="document-details.html?id=${document.id}" class="view-document-btn">
          <i class="fas fa-eye"></i>
        </a>
      </div>
    </div>
    <div class="document-content">
      <div class="document-tags">
        <span class="document-category ${document.categorySlug}-category">${document.category}</span>
        <span class="document-date">Cập nhật: ${document.dateUpdated}</span>
      </div>
      <h3 class="document-title">
        <a href="document-details.html?id=${document.id}">${document.title}</a>
      </h3>
      <p class="document-description">${document.description}</p>
      <div class="document-stats">
        <div class="document-rating">
          <div class="stars">
            ${getRatingStars(document.rating)}
          </div>
          <span class="rating-count">(${document.reviewCount})</span>
        </div>
        <div class="document-metrics">
          <span class="download-count"><i class="fas fa-download"></i> ${formatNumber(document.downloadCount)}</span>
          <span class="view-count"><i class="fas fa-eye"></i> ${formatNumber(document.viewCount)}</span>
        </div>
      </div>
    </div>
    <div class="document-footer">
      <div class="document-price ${document.price > 0 ? 'premium' : 'free'}">
        <span class="price">${document.price > 0 ? document.price + ' xu' : 'Miễn phí'}</span>
        ${document.price > 0 ? '<span class="price-label">/ Tải xuống</span>' : ''}
      </div>
      <a href="document-details.html?id=${document.id}" class="detail-btn">Xem chi tiết</a>
    </div>
  `;
  
  return card;
}

// Get HTML for rating stars
function getRatingStars(rating) {
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.5;
  let starsHTML = '';
  
  for (let i = 0; i < 5; i++) {
    if (i < fullStars) {
      starsHTML += '<i class="fas fa-star"></i>';
    } else if (i === fullStars && hasHalfStar) {
      starsHTML += '<i class="fas fa-star-half-alt"></i>';
    } else {
      starsHTML += '<i class="far fa-star"></i>';
    }
  }
  
  return starsHTML;
}

// Format number with thousands separator
function formatNumber(number) {
  if (number >= 1000) {
    return (number / 1000).toFixed(1) + 'K';
  }
  return number.toString();
}

// Initialize document viewer
function initDocumentViewer() {
  if (documentViewerContainer) {
    // Get document ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    const documentId = parseInt(urlParams.get('id')) || 1;
    
    // Find document data
    const document = documentData.find(doc => doc.id === documentId) || documentData[0];
    
    // In a real implementation, this would load the PDF using a PDF viewer library
    // For demo purposes, just show a placeholder
    const viewerPlaceholder = document.createElement('div');
    viewerPlaceholder.className = 'document-preview-placeholder';
    viewerPlaceholder.innerHTML = `
      <div class="preview-header">
        <h3>${document.title}</h3>
        <p>${document.category} | ${document.pages} trang</p>
      </div>
      <div class="preview-content">
        <p>${document.content}</p>
        <div class="preview-blurred">
          <p>Tài liệu này đã bị làm mờ. Vui lòng tải xuống để xem đầy đủ.</p>
          <button class="primary-btn download-preview-btn">
            <i class="fas fa-download"></i> ${document.isPremium ? `Tải xuống (${document.price} xu)` : 'Tải xuống miễn phí'}
          </button>
        </div>
      </div>
    `;
    
    documentViewerContainer.appendChild(viewerPlaceholder);
    
    // Initialize download button in preview
    const downloadPreviewBtn = documentViewerContainer.querySelector('.download-preview-btn');
    if (downloadPreviewBtn) {
      downloadPreviewBtn.addEventListener('click', function() {
        downloadDocument(document);
      });
    }
  }
}

// Initialize download buttons
function initDownloadButtons() {
  if (downloadButtons.length) {
    downloadButtons.forEach(button => {
      button.addEventListener('click', function(e) {
        e.preventDefault();
        
        // Get document ID
        const documentId = this.getAttribute('data-id') || 
                          this.closest('[data-id]')?.getAttribute('data-id') ||
                          new URLSearchParams(window.location.search).get('id') || 1;
        
        // Find document data
        const document = documentData.find(doc => doc.id === parseInt(documentId)) || documentData[0];
        
        // Download document
        downloadDocument(document);
      });
    });
  }
}

// Download document
function downloadDocument(document) {
  // Check if user is authenticated
  if (!isAuthenticated()) {
    // Redirect to login page
    window.location.href = 'login.html?redirect=' + encodeURIComponent(window.location.href);
    return;
  }
  
  // Check if document is premium
  if (document.isPremium) {
    // Show payment confirmation
    showPaymentConfirmation(document);
  } else {
    // Start download directly
    simulateDownload(document);
  }
}

// Show payment confirmation
function showPaymentConfirmation(document) {
  // Create modal element
  const modal = document.createElement('div');
  modal.className = 'modal payment-modal';
  
  // Create modal content
  modal.innerHTML = `
    <div class="modal-content">
      <div class="modal-header">
        <h3>Xác nhận thanh toán</h3>
        <button class="modal-close">&times;</button>
      </div>
      <div class="modal-body">
        <div class="payment-info">
          <div class="document-info">
            <img src="${document.coverImage}" alt="${document.title}" class="document-thumbnail">
            <div>
              <h4>${document.title}</h4>
              <p>${document.category} | ${document.pages} trang</p>
            </div>
          </div>
          <div class="payment-details">
            <div class="payment-row">
              <span>Giá tài liệu:</span>
              <span class="payment-price">${document.price} xu</span>
            </div>
            <div class="payment-row">
              <span>Số dư hiện tại:</span>
              <span class="payment-balance">100 xu</span>
            </div>
            <div class="payment-row payment-total">
              <span>Số dư còn lại:</span>
              <span class="payment-remaining">${100 - document.price} xu</span>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="secondary-btn modal-cancel">Hủy</button>
        <button class="primary-btn payment-confirm">Xác nhận thanh toán</button>
      </div>
    </div>
  `;
  
  // Add modal to body
  document.body.appendChild(modal);
  
  // Prevent body scrolling
  document.body.classList.add('modal-open');
  
  // Show modal
  setTimeout(() => {
    modal.classList.add('show');
  }, 10);
  
  // Handle close button
  const closeBtn = modal.querySelector('.modal-close');
  closeBtn.addEventListener('click', closeModal);
  
  // Handle cancel button
  const cancelBtn = modal.querySelector('.modal-cancel');
  cancelBtn.addEventListener('click', closeModal);
  
  // Handle payment confirmation
  const confirmBtn = modal.querySelector('.payment-confirm');
  confirmBtn.addEventListener('click', function() {
    // Close modal
    closeModal();
    
    // Start download
    simulateDownload(document);
    
    // Show success message
    showDownloadMessage('Thanh toán thành công! Tài liệu của bạn đang được tải xuống...', 'success');
  });
  
  // Close modal function
  function closeModal() {
    modal.classList.remove('show');
    
    // Remove modal after animation
    setTimeout(() => {
      document.body.removeChild(modal);
      document.body.classList.remove('modal-open');
    }, 300);
  }
}

// Simulate download (would use actual download in production)
function simulateDownload(document) {
  // Show loading message
  showDownloadMessage('Đang chuẩn bị tài liệu...', 'info');
  
  // Simulate download delay
  setTimeout(function() {
    // Show success message
    showDownloadMessage('Tài liệu đang được tải xuống...', 'success');
    
    // Update download count (would use AJAX in production)
    trackDocumentDownload(document.id);
    
    // In a real implementation, this would trigger the actual file download
    // For demo purposes, just create a dummy link
    const link = document.createElement('a');
    link.href = '#';
    link.download = document.title.replace(/\s+/g, '-').toLowerCase() + '.pdf';
    link.click();
  }, 1500);
}

// Show download message
function showDownloadMessage(message, type) {
  // Create message element
  const messageElement = document.createElement('div');
  messageElement.className = `message-toast ${type}-message`;
  messageElement.innerHTML = `
    <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-info-circle'}"></i>
    <span>${message}</span>
  `;
  
  // Add message to body
  document.body.appendChild(messageElement);
  
  // Show message
  setTimeout(() => {
    messageElement.classList.add('show');
  }, 10);
  
  // Remove message after 5 seconds
  setTimeout(() => {
    messageElement.classList.remove('show');
    
    // Remove element after animation
    setTimeout(() => {
      document.body.removeChild(messageElement);
    }, 300);
  }, 5000);
}

// Initialize share buttons
function initShareButtons() {
  if (shareButtons.length) {
    shareButtons.forEach(button => {
      button.addEventListener('click', function(e) {
        e.preventDefault();
        
        // Get document ID
        const documentId = this.getAttribute('data-id') || 
                          this.closest('[data-id]')?.getAttribute('data-id') ||
                          new URLSearchParams(window.location.search).get('id') || 1;
        
        // Find document data
        const document = documentData.find(doc => doc.id === parseInt(documentId)) || documentData[0];
        
        // Show share modal
        showShareModal(document);
      });
    });
  }
}

// Show share modal
function showShareModal(document) {
  // Get current URL
  const shareUrl = window.location.href;
  
  // Create modal element
  const modal = document.createElement('div');
  modal.className = 'modal share-modal';
  
  // Create modal content
  modal.innerHTML = `
    <div class="modal-content">
      <div class="modal-header">
        <h3>Chia sẻ tài liệu</h3>
        <button class="modal-close">&times;</button>
      </div>
      <div class="modal-body">
        <div class="share-info">
          <img src="${document.coverImage}" alt="${document.title}" class="share-thumbnail">
          <h4>${document.title}</h4>
        </div>
        <div class="share-options">
          <a href="https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(shareUrl)}" target="_blank" class="share-option facebook">
            <i class="fab fa-facebook-f"></i>
            <span>Facebook</span>
          </a>
          <a href="https://twitter.com/intent/tweet?url=${encodeURIComponent(shareUrl)}&text=${encodeURIComponent(document.title)}" target="_blank" class="share-option twitter">
            <i class="fab fa-twitter"></i>
            <span>Twitter</span>
          </a>
          <a href="https://www.linkedin.com/shareArticle?mini=true&url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(document.title)}" target="_blank" class="share-option linkedin">
            <i class="fab fa-linkedin-in"></i>
            <span>LinkedIn</span>
          </a>
          <a href="mailto:?subject=${encodeURIComponent(document.title)}&body=${encodeURIComponent('Tôi đã tìm thấy tài liệu này trên EduShare: ' + shareUrl)}" class="share-option email">
            <i class="fas fa-envelope"></i>
            <span>Email</span>
          </a>
        </div>
        <div class="share-link">
          <p>Hoặc sao chép đường dẫn:</p>
          <div class="copy-link-container">
            <input type="text" value="${shareUrl}" id="shareUrlInput" readonly>
            <button class="copy-link-btn" id="copyLinkBtn">
              <i class="fas fa-copy"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
  
  // Add modal to body
  document.body.appendChild(modal);
  
  // Prevent body scrolling
  document.body.classList.add('modal-open');
  
  // Show modal
  setTimeout(() => {
    modal.classList.add('show');
  }, 10);
  
  // Handle close button
  const closeBtn = modal.querySelector('.modal-close');
  closeBtn.addEventListener('click', closeModal);
  
  // Handle copy link button
  const copyLinkBtn = modal.querySelector('#copyLinkBtn');
  const shareUrlInput = modal.querySelector('#shareUrlInput');
  
  copyLinkBtn.addEventListener('click', function() {
    // Select input text
    shareUrlInput.select();
    shareUrlInput.setSelectionRange(0, 99999);
    
    // Copy text
    document.execCommand('copy');
    
    // Show copied message
    this.innerHTML = '<i class="fas fa-check"></i>';
    
    // Reset button after 2 seconds
    setTimeout(() => {
      this.innerHTML = '<i class="fas fa-copy"></i>';
    }, 2000);
  });
  
  // Close modal when clicking outside
  modal.addEventListener('click', function(e) {
    if (e.target === modal) {
      closeModal();
    }
  });
  
  // Close modal function
  function closeModal() {
    modal.classList.remove('show');
    
    // Remove modal after animation
    setTimeout(() => {
      document.body.removeChild(modal);
      document.body.classList.remove('modal-open');
    }, 300);
  }
}

// Initialize bookmark buttons
function initBookmarkButtons() {
  if (bookmarkButtons.length) {
    // Get bookmarked documents from local storage
    const bookmarks = getBookmarks();
    
    bookmarkButtons.forEach(button => {
      // Set initial state
      const documentId = button.getAttribute('data-id') || 
                        button.closest('[data-id]')?.getAttribute('data-id') ||
                        new URLSearchParams(window.location.search).get('id') || '1';
      
      if (bookmarks.includes(documentId)) {
        button.classList.add('active');
        button.querySelector('i').className = 'fas fa-bookmark';
      }
      
      // Add click event
      button.addEventListener('click', function(e) {
        e.preventDefault();
        
        // Check if user is authenticated
        if (!isAuthenticated()) {
          // Redirect to login page
          window.location.href = 'login.html?redirect=' + encodeURIComponent(window.location.href);
          return;
        }
        
        // Toggle bookmark
        toggleBookmark(documentId, this);
      });
    });
  }
}

// Toggle bookmark
function toggleBookmark(documentId, button) {
  // Get current bookmarks
  const bookmarks = getBookmarks();
  
  // Check if already bookmarked
  const index = bookmarks.indexOf(documentId);
  
  if (index === -1) {
    // Add to bookmarks
    bookmarks.push(documentId);
    
    // Update button
    button.classList.add('active');
    button.querySelector('i').className = 'fas fa-bookmark';
    
    // Show message
    showBookmarkMessage('Đã thêm vào danh sách yêu thích');
  } else {
    // Remove from bookmarks
    bookmarks.splice(index, 1);
    
    // Update button
    button.classList.remove('active');
    button.querySelector('i').className = 'far fa-bookmark';
    
    // Show message
    showBookmarkMessage('Đã xóa khỏi danh sách yêu thích');
  }
  
  // Save to local storage
  localStorage.setItem('bookmarks', JSON.stringify(bookmarks));
}

// Get bookmarks from local storage
function getBookmarks() {
  const bookmarksStr = localStorage.getItem('bookmarks');
  return bookmarksStr ? JSON.parse(bookmarksStr) : [];
}

// Show bookmark message
function showBookmarkMessage(message) {
  // Create message element
  const messageElement = document.createElement('div');
  messageElement.className = 'message-toast info-message';
  messageElement.innerHTML = `
    <i class="fas fa-bookmark"></i>
    <span>${message}</span>
  `;
  
  // Add message to body
  document.body.appendChild(messageElement);
  
  // Show message
  setTimeout(() => {
    messageElement.classList.add('show');
  }, 10);
  
  // Remove message after 3 seconds
  setTimeout(() => {
    messageElement.classList.remove('show');
    
    // Remove element after animation
    setTimeout(() => {
      document.body.removeChild(messageElement);
    }, 300);
  }, 3000);
}

// Initialize rating system
function initRatingSystem() {
  if (ratingStars.length) {
    let selectedRating = 0;
    
    // Add hover effect
    ratingStars.forEach((star, index) => {
      const rating = index + 1;
      
      star.addEventListener('mouseenter', function() {
        // Highlight stars up to current
        ratingStars.forEach((s, i) => {
          if (i < rating) {
            s.innerHTML = '<i class="fas fa-star"></i>';
          } else {
            s.innerHTML = '<i class="far fa-star"></i>';
          }
        });
      });
      
      star.addEventListener('mouseleave', function() {
        // Reset to selected rating
        ratingStars.forEach((s, i) => {
          if (i < selectedRating) {
            s.innerHTML = '<i class="fas fa-star"></i>';
          } else {
            s.innerHTML = '<i class="far fa-star"></i>';
          }
        });
      });
      
      star.addEventListener('click', function() {
        // Set selected rating
        selectedRating = rating;
        
        // Update UI
        ratingStars.forEach((s, i) => {
          if (i < selectedRating) {
            s.innerHTML = '<i class="fas fa-star"></i>';
          } else {
            s.innerHTML = '<i class="far fa-star"></i>';
          }
        });
        
        // Show message
        showRatingMessage(`Bạn đã đánh giá ${selectedRating} sao`);
      });
    });
  }
}

// Show rating message
function showRatingMessage(message) {
  // Create message element
  const messageElement = document.createElement('div');
  messageElement.className = 'message-toast success-message';
  messageElement.innerHTML = `
    <i class="fas fa-check-circle"></i>
    <span>${message}</span>
  `;
  
  // Add message to body
  document.body.appendChild(messageElement);
  
  // Show message
  setTimeout(() => {
    messageElement.classList.add('show');
  }, 10);
  
  // Remove message after 3 seconds
  setTimeout(() => {
    messageElement.classList.remove('show');
    
    // Remove element after animation
    setTimeout(() => {
      document.body.removeChild(messageElement);
    }, 300);
  }, 3000);
}

// Initialize comment system
function initCommentSystem() {
  if (commentForm) {
    commentForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      // Check if user is authenticated
      if (!isAuthenticated()) {
        // Redirect to login page
        window.location.href = 'login.html?redirect=' + encodeURIComponent(window.location.href);
        return;
      }
      
      // Get comment text
      const commentInput = this.querySelector('textarea');
      const commentText = commentInput.value.trim();
      
      if (!commentText) {
        return;
      }
      
      // Create new comment
      const user = JSON.parse(localStorage.getItem('user'));
      addNewComment({
        user: {
          name: user.name,
          avatar: user.avatar
        },
        date: new Date().toLocaleDateString('vi-VN'),
        text: commentText
      });
      
      // Clear input
      commentInput.value = '';
      
      // Show message
      showCommentMessage('Bình luận của bạn đã được đăng');
    });
  }
}

// Add new comment
function addNewComment(comment) {
  const commentsContainer = document.querySelector('.comments-list');
  if (!commentsContainer) return;
  
  // Create comment element
  const commentElement = document.createElement('div');
  commentElement.className = 'comment-item';
  
  commentElement.innerHTML = `
    <div class="comment-author">
      <img src="${comment.user.avatar}" alt="${comment.user.name}" class="comment-avatar">
      <div class="comment-meta">
        <h4 class="comment-name">${comment.user.name}</h4>
        <span class="comment-date">${comment.date}</span>
      </div>
    </div>
    <div class="comment-content">
      <p>${comment.text}</p>
    </div>
  `;
  
  // Add comment to container
  commentsContainer.prepend(commentElement);
  
  // Update comment count
  const commentCount = document.querySelector('.comment-count');
  if (commentCount) {
    const count = parseInt(commentCount.textContent) || 0;
    commentCount.textContent = count + 1;
  }
}

// Show comment message
function showCommentMessage(message) {
  // Create message element
  const messageElement = document.createElement('div');
  messageElement.className = 'message-toast success-message';
  messageElement.innerHTML = `
    <i class="fas fa-check-circle"></i>
    <span>${message}</span>
  `;
  
  // Add message to body
  document.body.appendChild(messageElement);
  
  // Show message
  setTimeout(() => {
    messageElement.classList.add('show');
  }, 10);
  
  // Remove message after 3 seconds
  setTimeout(() => {
    messageElement.classList.remove('show');
    
    // Remove element after animation
    setTimeout(() => {
      document.body.removeChild(messageElement);
    }, 300);
  }, 3000);
}

// Track document view
function trackDocumentView(documentId) {
  // In a real implementation, this would send an AJAX request to the server
  console.log(`Document ${documentId} viewed`);
}

// Track document download
function trackDocumentDownload(documentId) {
  // In a real implementation, this would send an AJAX request to the server
  console.log(`Document ${documentId} downloaded`);
}

// Check if user is authenticated (imported from auth.js)
function isAuthenticated() {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('isAuthenticated') === 'true';
  }
  return false;
}
