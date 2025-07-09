// JavaScript cho các phần được cải tiến của trang chủ

document.addEventListener('DOMContentLoaded', function() {
  // Danh Mục Tài Liệu - Lọc danh mục
  initCategoryFilter();
  
  // Tài Liệu Nổi Bật - Slider
  initFeaturedDocumentsSlider();
  
  // Upload Document - Form & Validation
  initUploadForm();
  
  // Newsletter - Form Validation
  initNewsletterForm();
});

// Lọc danh mục
function initCategoryFilter() {
  const categoryPills = document.querySelectorAll('.category-pill');
  const categoryCards = document.querySelectorAll('.category-card');
  
  categoryPills.forEach(pill => {
    pill.addEventListener('click', function() {
      // Loại bỏ active class từ tất cả pills
      categoryPills.forEach(p => p.classList.remove('active'));
      
      // Thêm active class cho pill được chọn
      this.classList.add('active');
      
      const selectedCategory = this.getAttribute('data-category');
      
      // Hiển thị tất cả cards nếu "all" được chọn
      if (selectedCategory === 'all') {
        categoryCards.forEach(card => {
          card.style.display = 'flex';
          setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
          }, 50);
        });
      } else {
        // Nếu không, chỉ hiển thị cards phù hợp với danh mục được chọn
        categoryCards.forEach(card => {
          if (card.getAttribute('data-category') === selectedCategory) {
            card.style.display = 'flex';
            setTimeout(() => {
              card.style.opacity = '1';
              card.style.transform = 'translateY(0)';
            }, 50);
          } else {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            setTimeout(() => {
              card.style.display = 'none';
            }, 300);
          }
        });
      }
    });
  });
}

// Slider cho Tài Liệu Nổi Bật
function initFeaturedDocumentsSlider() {
  const prevBtn = document.getElementById('featuredPrevBtn');
  const nextBtn = document.getElementById('featuredNextBtn');
  const documentGrid = document.getElementById('featuredDocuments');
  const documentCards = documentGrid.querySelectorAll('.document-card');
  
  if (!prevBtn || !nextBtn || !documentGrid) return;
  
  let currentPosition = 0;
  let cardsPerView = getCardsPerView();
  const totalCards = documentCards.length;
  const maxPosition = Math.max(0, totalCards - cardsPerView);
  
  // Cập nhật số lượng thẻ hiển thị khi kích thước màn hình thay đổi
  window.addEventListener('resize', function() {
    cardsPerView = getCardsPerView();
    updateSliderButtons();
  });
  
  // Xử lý nút Previous
  prevBtn.addEventListener('click', function() {
    if (currentPosition > 0) {
      currentPosition--;
      updateSliderPosition();
    }
  });
  
  // Xử lý nút Next
  nextBtn.addEventListener('click', function() {
    if (currentPosition < maxPosition) {
      currentPosition++;
      updateSliderPosition();
    }
  });
  
  // Cập nhật vị trí slider
  function updateSliderPosition() {
    // Tính toán vị trí dịch chuyển
    const cardWidth = documentCards[0].offsetWidth;
    const gap = parseInt(getComputedStyle(documentGrid).columnGap);
    const translateX = -(currentPosition * (cardWidth + gap));
    
    // Áp dụng transform cho mỗi thẻ
    documentCards.forEach(card => {
      card.style.transform = `translateX(${translateX}px)`;
    });
    
    // Cập nhật trạng thái nút
    updateSliderButtons();
  }
  
  // Cập nhật trạng thái nút
  function updateSliderButtons() {
    prevBtn.disabled = currentPosition === 0;
    nextBtn.disabled = currentPosition >= maxPosition;
    
    prevBtn.style.opacity = prevBtn.disabled ? '0.5' : '1';
    nextBtn.style.opacity = nextBtn.disabled ? '0.5' : '1';
  }
  
  // Xác định số lượng thẻ hiển thị dựa trên kích thước màn hình
  function getCardsPerView() {
    const viewportWidth = window.innerWidth;
    if (viewportWidth >= 1200) return 3;
    if (viewportWidth >= 768) return 2;
    return 1;
  }
  
  // Khởi tạo
  updateSliderButtons();
}

// Xử lý phần Upload Document
function initUploadForm() {
  // Xử lý animation cho upload-promo
  const uploadPromo = document.querySelector('.upload-promo');
  const uploadStats = document.querySelectorAll('.stat-item');
  const benefitItems = document.querySelectorAll('.benefit-item');
  const uploadCta = document.querySelector('.upload-cta-btn');
  
  if (uploadPromo) {
    // Thêm hiệu ứng hover cho các stat-item
    uploadStats.forEach(stat => {
      stat.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-10px)';
        this.style.backgroundColor = 'rgba(255, 255, 255, 0.25)';
      });
      
      stat.addEventListener('mouseleave', function() {
        this.style.transform = '';
        this.style.backgroundColor = '';
      });
    });
    
    // Thêm hiệu ứng hover cho các benefit-item
    benefitItems.forEach(item => {
      item.addEventListener('mouseenter', function() {
        const icon = this.querySelector('.benefit-icon');
        if (icon) {
          icon.style.transform = 'scale(1.1) rotate(5deg)';
          icon.style.backgroundColor = 'rgba(255, 255, 255, 0.25)';
        }
      });
      
      item.addEventListener('mouseleave', function() {
        const icon = this.querySelector('.benefit-icon');
        if (icon) {
          icon.style.transform = '';
          icon.style.backgroundColor = '';
        }
      });
    });
    
    // Hiệu ứng cho nút CTA
    if (uploadCta) {
      uploadCta.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-5px)';
        this.style.boxShadow = '0 10px 20px rgba(67, 97, 238, 0.3)';
      });
      
      uploadCta.addEventListener('mouseleave', function() {
        this.style.transform = '';
        this.style.boxShadow = '';
      });
    }
  }
  
  // Chuyển đến trang upload đầy đủ khi nhấn nút upload
  const uploadBtn = document.querySelector('.upload-cta-btn');
  if (uploadBtn) {
    uploadBtn.addEventListener('click', function(e) {
      // Hiệu ứng khi nhấn nút
      this.classList.add('clicked');
      
      // Xóa hiệu ứng sau khi chuyển trang
      setTimeout(() => {
        this.classList.remove('clicked');
      }, 300);
    });
  }
}

// Form đăng ký nhận thông báo
function initNewsletterForm() {
  const newsletterForm = document.querySelector('.newsletter-form');
  
  if (newsletterForm) {
    newsletterForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const emailInput = this.querySelector('input[type="email"]');
      const email = emailInput.value.trim();
      
      if (!email) {
        // Hiển thị lỗi nếu email trống
        showNewsletterMessage('Vui lòng nhập địa chỉ email', 'error');
        return;
      }
      
      if (!isValidEmail(email)) {
        // Hiển thị lỗi nếu email không hợp lệ
        showNewsletterMessage('Địa chỉ email không hợp lệ', 'error');
        return;
      }
      
      // Giả lập gửi form thành công
      emailInput.value = '';
      showNewsletterMessage('Đăng ký thành công! Cảm ơn bạn đã đăng ký.', 'success');
    });
  }
  
  // Kiểm tra email hợp lệ
  function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
  
  // Hiển thị thông báo
  function showNewsletterMessage(message, type) {
    const newsletterForm = document.querySelector('.newsletter-form');
    
    // Tìm thông báo hiện có hoặc tạo mới
    let messageEl = document.querySelector('.newsletter-message');
    if (!messageEl) {
      messageEl = document.createElement('div');
      messageEl.className = 'newsletter-message';
      newsletterForm.insertAdjacentElement('afterend', messageEl);
    }
    
    // Thiết lập loại và nội dung thông báo
    messageEl.textContent = message;
    messageEl.className = `newsletter-message ${type}`;
    messageEl.style.display = 'block';
    
    // Tự động ẩn sau 3 giây
    setTimeout(function() {
      messageEl.style.opacity = '0';
      setTimeout(function() {
        messageEl.style.display = 'none';
      }, 300);
    }, 3000);
  }
}