// Comment Actions JavaScript

// Toast notification function
function showToast(message, type = 'info') {
  // Remove existing toasts
  const existingToasts = document.querySelectorAll('.toast');
  existingToasts.forEach(toast => toast.remove());
  
  // Create new toast
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  
  // Add to page
  document.body.appendChild(toast);
  
  // Auto remove after 3 seconds
  setTimeout(() => {
    if (toast.parentNode) {
      toast.remove();
    }
  }, 3000);
}

// Initialize like/dislike functionality
function initLikeDislikeActions() {
  const likeActions = document.querySelectorAll('.like-action');
  const dislikeActions = document.querySelectorAll('.dislike-action');

  likeActions.forEach(action => {
    action.addEventListener('click', function(e) {
      e.preventDefault();
      
      const commentId = this.getAttribute('data-comment-id');
      const documentId = window.location.pathname.split('/').pop();
      
      // Create a hidden form and submit it
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = `/documents/${documentId}/comments/${commentId}/like`;
      
      // Add CSRF token if available
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || 
                       document.querySelector('input[name="_csrf"]')?.value;
      if (csrfToken) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);
      }
      
      document.body.appendChild(form);
      form.submit();
    });
  });

  dislikeActions.forEach(action => {
    action.addEventListener('click', function(e) {
      e.preventDefault();
      
      const commentId = this.getAttribute('data-comment-id');
      const documentId = window.location.pathname.split('/').pop();
      
      // Create a hidden form and submit it
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = `/documents/${documentId}/comments/${commentId}/dislike`;
      
      // Add CSRF token if available
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || 
                       document.querySelector('input[name="_csrf"]')?.value;
      if (csrfToken) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);
      }
      
      document.body.appendChild(form);
      form.submit();
    });
  });
}

// Initialize report functionality
function initReportActions() {
  const reportActions = document.querySelectorAll('.report-action');
  const reportModal = document.getElementById('reportModal');
  const reportForm = document.getElementById('reportForm');
  const closeBtn = reportModal?.querySelector('.close');
  let currentCommentId = null;

  if (!reportModal || !reportForm) {
    console.warn('Report modal elements not found');
    return;
  }

  reportActions.forEach(action => {
    action.addEventListener('click', function(e) {
      e.preventDefault();
      currentCommentId = this.getAttribute('data-comment-id');
      
      // Update form action URL with the correct comment ID
      const form = document.getElementById('reportForm');
      const documentId = window.location.pathname.split('/').pop();
      form.action = `/documents/${documentId}/comments/${currentCommentId}/report`;
      
      reportModal.style.display = 'block';
      document.body.style.overflow = 'hidden'; // Prevent background scrolling
    });
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', closeReportModal);
  }

  // Close modal when clicking outside
  window.addEventListener('click', function(event) {
    if (event.target === reportModal) {
      closeReportModal();
    }
  });

  // Close modal with Escape key
  document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape' && reportModal.style.display === 'block') {
      closeReportModal();
    }
  });

  reportForm.addEventListener('submit', function(e) {
    const reason = document.getElementById('reportReason').value;
    const note = document.getElementById('reportNote').value;

    if (!reason) {
      e.preventDefault();
      showToast('Vui lòng chọn lý do báo cáo', 'warning');
      return;
    }

    // Let the form submit normally - it will redirect back to the document page
    // The server will handle the redirect and show flash messages
  });
}

// Close report modal
function closeReportModal() {
  const reportModal = document.getElementById('reportModal');
  const reportForm = document.getElementById('reportForm');
  
  if (reportModal) {
    reportModal.style.display = 'none';
    document.body.style.overflow = 'auto'; // Restore scrolling
  }
  
  if (reportForm) {
    reportForm.reset();
  }
}

// Initialize rating stars in review form
function initRatingStars() {
  const ratingStars = document.querySelectorAll('.rate-star');
  const ratingInput = document.getElementById('ratingInput');

  if (!ratingStars.length || !ratingInput) return;

  ratingStars.forEach(star => {
    star.addEventListener('click', function() {
      const value = this.getAttribute('data-value');
      ratingInput.value = value;
      updateStars(value);
    });

    // Hover effects
    star.addEventListener('mouseenter', function() {
      const value = this.getAttribute('data-value');
      updateStars(value, true);
    });

    star.addEventListener('mouseleave', function() {
      const currentRating = ratingInput.value || 0;
      updateStars(currentRating, false);
    });
  });
}

// Helper function to update star display
function updateStars(rating, isHover = false) {
  const ratingStars = document.querySelectorAll('.rate-star');
  
  ratingStars.forEach((star, index) => {
    const icon = star.querySelector('i');
    if (index < rating) {
      icon.className = 'fas fa-star';
      if (isHover) {
        star.classList.add('active');
      } else {
        star.classList.add('active');
      }
    } else {
      icon.className = 'far fa-star';
      star.classList.remove('active');
    }
  });
}

// Initialize review form submission
function initReviewForm() {
  const reviewForm = document.getElementById('reviewForm');
  
  if (!reviewForm) {
    console.log('Review form not found');
    return;
  }

  console.log('Review form found, adding submit listener');

  reviewForm.addEventListener('submit', function(e) {
    const rating = document.getElementById('ratingInput').value;
    const review = document.getElementById('reviewText').value;

    console.log('Form submitting with rating:', rating, 'review:', review);

    if (!rating) {
      e.preventDefault();
      showToast('Vui lòng chọn đánh giá sao', 'warning');
      return;
    }

    if (!review.trim()) {
      e.preventDefault();
      showToast('Vui lòng nhập nội dung đánh giá', 'warning');
      return;
    }

    console.log('Form validation passed, submitting...');
    
    // Show loading state
    const submitBtn = reviewForm.querySelector('.form-submit');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';
    submitBtn.disabled = true;
    
    // Let the form submit normally - it will redirect back to the document page
    // The server will handle the redirect and show flash messages
  });
}

// Update UI after redirect (for like/dislike actions)
function updateUIAfterRedirect() {
  // Check if we have updated data from server
  const updateDataElement = document.querySelector('[data-updated-comment-id]');
  if (!updateDataElement) return;
  
  const updatedCommentId = updateDataElement.getAttribute('data-updated-comment-id');
  const updatedLikesCount = updateDataElement.getAttribute('data-updated-likes-count');
  const updatedDislikesCount = updateDataElement.getAttribute('data-updated-dislikes-count');
  const updatedAction = updateDataElement.getAttribute('data-updated-action');
  
  if (updatedCommentId && updatedLikesCount !== null && updatedDislikesCount !== null) {
    // Find the comment element
    const commentElement = document.querySelector(`[data-comment-id="${updatedCommentId}"]`);
    if (commentElement) {
      // Update like count
      const likeCountElement = commentElement.querySelector('.like-count');
      if (likeCountElement) {
        likeCountElement.textContent = updatedLikesCount;
      }
      
      // Update dislike count
      const dislikeCountElement = commentElement.querySelector('.dislike-count');
      if (dislikeCountElement) {
        dislikeCountElement.textContent = updatedDislikesCount;
      }
      
      // Update like button state
      const likeAction = commentElement.querySelector('.like-action');
      if (likeAction) {
        const likeIcon = likeAction.querySelector('i');
        const likeText = likeAction.querySelector('.like-text');
        
        if (updatedAction === 'liked') {
          likeIcon.className = 'fas fa-thumbs-up liked';
          likeText.textContent = 'Đã thích';
        } else if (updatedAction === 'removed') {
          likeIcon.className = 'far fa-thumbs-up';
          likeText.textContent = 'Hữu ích';
        }
      }
      
      // Update dislike button state
      const dislikeAction = commentElement.querySelector('.dislike-action');
      if (dislikeAction) {
        const dislikeIcon = dislikeAction.querySelector('i');
        const dislikeText = dislikeAction.querySelector('.dislike-text');
        
        if (updatedAction === 'disliked') {
          dislikeIcon.className = 'fas fa-thumbs-down disliked';
          dislikeText.textContent = 'Đã không thích';
        } else if (updatedAction === 'removed') {
          dislikeIcon.className = 'far fa-thumbs-down';
          dislikeText.textContent = 'Không hữu ích';
        }
      }
      
      // Smooth scroll to the updated comment
      setTimeout(() => {
        commentElement.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'center' 
        });
        
        // Add highlight effect
        commentElement.classList.add('highlight');
        setTimeout(() => {
          commentElement.classList.remove('highlight');
        }, 2000);
      }, 100);
    }
  }
}

// Show toast from flash message
function showToastFromFlash() {
  // Check if we have toast message from server
  const toastMessageElement = document.querySelector('[data-toast-message]');
  if (toastMessageElement) {
    const message = toastMessageElement.getAttribute('data-toast-message');
    const type = toastMessageElement.getAttribute('data-toast-type') || 'info';
    showToast(message, type);
  }
}

// Handle new comment scroll
function handleNewCommentScroll() {
  // Check if we have new comment data from server
  const newCommentElement = document.querySelector('[data-new-comment-id]');
  if (!newCommentElement) return;
  
  const newCommentId = newCommentElement.getAttribute('data-new-comment-id');
  if (!newCommentId) return;
  
  // Reset review form
  const reviewForm = document.getElementById('reviewForm');
  if (reviewForm) {
    // Reset form fields
    const ratingInput = document.getElementById('ratingInput');
    const reviewText = document.getElementById('reviewText');
    const ratingStars = document.querySelectorAll('.rate-star i');
    
    if (ratingInput) ratingInput.value = '';
    if (reviewText) reviewText.value = '';
    
    // Reset stars
    ratingStars.forEach(star => {
      star.className = 'far fa-star';
      star.style.color = '#ccc';
    });
    
    // Reset submit button
    const submitBtn = reviewForm.querySelector('.form-submit');
    if (submitBtn) {
      submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Gửi đánh giá';
      submitBtn.disabled = false;
    }
  }
  
  // Switch to reviews tab if not already active
  const reviewsTab = document.querySelector('.content-tab[data-tab="reviews"]');
  const reviewsContent = document.getElementById('reviews-content');
  
  if (reviewsTab && reviewsContent && !reviewsContent.classList.contains('active')) {
    // Remove active class from all tabs and content
    document.querySelectorAll('.content-tab').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    // Add active class to reviews tab and content
    reviewsTab.classList.add('active');
    reviewsContent.classList.add('active');
  }
  
  // Find the comment element
  const commentElement = document.querySelector(`[data-comment-id="${newCommentId}"]`);
  if (commentElement) {
    // Smooth scroll to the new comment
    setTimeout(() => {
      commentElement.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'center' 
      });
      
      // Add highlight effect
      commentElement.classList.add('highlight');
      setTimeout(() => {
        commentElement.classList.remove('highlight');
      }, 3000);
    }, 500);
  } else {
    // Try to find the comment in the review list
    const reviewItems = document.querySelectorAll('.review-item');
    let foundComment = null;
    
    reviewItems.forEach(item => {
      const itemId = item.getAttribute('data-comment-id');
      if (itemId === newCommentId) {
        foundComment = item;
      }
    });
    
    if (foundComment) {
      // Smooth scroll to the found comment
      setTimeout(() => {
        foundComment.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'center' 
        });
        
        // Add highlight effect
        foundComment.classList.add('highlight');
        setTimeout(() => {
          foundComment.classList.remove('highlight');
        }, 3000);
      }, 500);
    } else {
      // If comment not found, it might be newly added and not yet in DOM
      // In this case, we'll scroll to the reviews section and show a message
      const reviewsSection = document.getElementById('reviews-content');
      if (reviewsSection) {
        setTimeout(() => {
          reviewsSection.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'start' 
          });
          
          // Show a message that the comment was added
          showToast('Đánh giá của bạn đã được thêm vào danh sách!', 'success');
          
          // Scroll to the bottom of the review list to see the new comment
          const reviewList = document.querySelector('.review-list');
          if (reviewList) {
            // Scroll to the bottom to see the newest comment (assuming newest comments are at the bottom)
            reviewList.scrollTop = reviewList.scrollHeight;
          }
          
          // Reload the page after a short delay to show the new comment
          setTimeout(() => {
            window.location.reload();
          }, 2000);
        }, 500);
      }
    }
  }
  
  // Clear the new comment data to prevent re-processing
  newCommentElement.remove();
}

// Initialize all comment actions when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  initLikeDislikeActions();
  initReportActions();
  initRatingStars();
  initReviewForm();
  updateUIAfterRedirect();
  showToastFromFlash();
  handleNewCommentScroll();
  
  console.log('Comment actions initialized');
});

// Export functions for global access
window.CommentActions = {
  showToast,
  initLikeDislikeActions,
  initReportActions,
  closeReportModal,
  initRatingStars,
  initReviewForm
};

// Export showToast globally
window.showToast = showToast; 