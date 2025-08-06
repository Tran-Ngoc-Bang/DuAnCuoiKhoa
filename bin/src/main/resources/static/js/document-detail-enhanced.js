/**
 * Document Detail Enhanced Interactions
 * Cung cấp các hiệu ứng tương tác và animation nâng cao cho trang chi tiết tài liệu
 */

document.addEventListener('DOMContentLoaded', function() {
  // Khởi tạo các hiệu ứng nâng cao
  initEnhancedHeroEffects();
  initScrollReveal();
  initHighlightHover();
  initParallaxEffects();
  initTabTransitions();
  initDownloadButtonEffects();
  initPreviewPageEnhancements();
});

/**
 * Hiệu ứng Hero section với parallax và animation
 */
function initEnhancedHeroEffects() {
  const hero = document.querySelector('.document-hero');
  if (!hero) return;

  // Hiệu ứng parallax nhẹ khi di chuyển chuột
  hero.addEventListener('mousemove', function(e) {
    const moveX = (e.clientX - window.innerWidth / 2) * 0.01;
    const moveY = (e.clientY - window.innerHeight / 2) * 0.01;
    
    const heroContent = this.querySelector('.document-hero-content');
    if (heroContent) {
      heroContent.style.transform = `translate(${moveX}px, ${moveY}px)`;
    }
    
    // Hiệu ứng chuyển động ngược cho background pattern
    this.style.backgroundPosition = `calc(50% + ${moveX * 2}px) calc(50% + ${moveY * 2}px)`;
  });

  // Đặt lại khi rời khỏi hero
  hero.addEventListener('mouseleave', function() {
    const heroContent = this.querySelector('.document-hero-content');
    if (heroContent) {
      heroContent.style.transform = 'translate(0, 0)';
    }
    this.style.backgroundPosition = '50% 50%';
  });

  // Thêm hiệu ứng cho tiêu đề
  const title = hero.querySelector('.document-title');
  if (title) {
    title.classList.add('animate__animated', 'animate__fadeInUp');
  }
}

/**
 * Hiệu ứng hiển thị các phần tử khi cuộn
 */
function initScrollReveal() {
  const highlightItems = document.querySelectorAll('.highlight-item');
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('reveal-element');
        observer.unobserve(entry.target);
      }
    });
  }, {
    threshold: 0.2
  });

  highlightItems.forEach(item => {
    observer.observe(item);
    // Thêm class cho CSS animation
    item.classList.add('reveal-ready');
  });

  // Hiệu ứng cho các tiêu đề
  const headings = document.querySelectorAll('.document-description h2, .document-description h3');
  headings.forEach((heading, index) => {
    heading.style.opacity = '0';
    heading.style.transform = 'translateY(20px)';
    heading.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
    heading.style.transitionDelay = `${index * 0.1}s`;
    
    const headingObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.style.opacity = '1';
          entry.target.style.transform = 'translateY(0)';
          headingObserver.unobserve(entry.target);
        }
      });
    }, {
      threshold: 0.2
    });
    
    headingObserver.observe(heading);
  });
}

/**
 * Hiệu ứng hover cho các điểm nổi bật
 */
function initHighlightHover() {
  const highlightItems = document.querySelectorAll('.highlight-item');
  
  highlightItems.forEach(item => {
    item.addEventListener('mouseenter', function() {
      const icon = this.querySelector('.highlight-icon');
      if (icon) {
        icon.style.transform = 'scale(1.2) rotate(5deg)';
        icon.style.transition = 'transform 0.3s ease';
      }
    });
    
    item.addEventListener('mouseleave', function() {
      const icon = this.querySelector('.highlight-icon');
      if (icon) {
        icon.style.transform = 'scale(1) rotate(0deg)';
      }
    });
  });
}

/**
 * Hiệu ứng parallax cho các phần tử trong trang
 */
function initParallaxEffects() {
  window.addEventListener('scroll', function() {
    const scrollPosition = window.scrollY;
    
    // Parallax cho hình ảnh xem trước
    const previewImage = document.querySelector('.preview-image');
    if (previewImage) {
      previewImage.style.backgroundPosition = `center ${scrollPosition * 0.05}px`;
    }
    
    // Hiệu ứng parallax cho các phần tử khác
    const parallaxElements = document.querySelectorAll('.document-highlights, .document-description blockquote');
    parallaxElements.forEach(element => {
      const elementPosition = element.getBoundingClientRect().top;
      const offset = elementPosition * 0.08;
      element.style.transform = `translateY(${offset}px)`;
    });
  });
}

/**
 * Cải thiện chuyển đổi giữa các tab
 */
function initTabTransitions() {
  const tabs = document.querySelectorAll('.content-tab');
  
  tabs.forEach(tab => {
    tab.addEventListener('click', function() {
      // Thêm hiệu ứng ripple khi click
      const ripple = document.createElement('span');
      ripple.classList.add('tab-ripple');
      this.appendChild(ripple);
      
      const rect = this.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      ripple.style.width = ripple.style.height = `${size}px`;
      ripple.style.left = `${event.clientX - rect.left - size/2}px`;
      ripple.style.top = `${event.clientY - rect.top - size/2}px`;
      
      // Xóa ripple sau khi animation hoàn thành
      setTimeout(() => {
        ripple.remove();
      }, 600);
      
      // Lấy nội dung tab hiện tại và tab mới
      const currentTab = document.querySelector('.tab-content.active');
      const targetId = this.getAttribute('data-tab');
      const newTab = document.getElementById(`${targetId}-content`);
      
      if (currentTab && newTab) {
        // Thêm hiệu ứng transition
        currentTab.style.opacity = '0';
        currentTab.style.transform = 'translateX(-10px)';
        
        setTimeout(() => {
          currentTab.classList.remove('active');
          newTab.classList.add('active');
          
          // Reset và thêm hiệu ứng cho tab mới
          newTab.style.opacity = '0';
          newTab.style.transform = 'translateX(10px)';
          
          setTimeout(() => {
            newTab.style.opacity = '1';
            newTab.style.transform = 'translateX(0)';
          }, 50);
        }, 300);
      }
    });
  });
  
  // Thêm CSS cho các hiệu ứng
  const style = document.createElement('style');
  style.textContent = `
    .tab-content {
      transition: opacity 0.3s ease, transform 0.3s ease;
    }
    
    .tab-ripple {
      position: absolute;
      border-radius: 50%;
      background-color: rgba(255, 255, 255, 0.5);
      transform: scale(0);
      animation: ripple 0.6s linear;
      pointer-events: none;
    }
    
    @keyframes ripple {
      to {
        transform: scale(2);
        opacity: 0;
      }
    }
    
    .reveal-ready {
      opacity: 0;
      transform: translateY(20px);
      transition: opacity 0.6s ease, transform 0.6s ease;
    }
    
    .reveal-element {
      opacity: 1;
      transform: translateY(0);
    }
  `;
  document.head.appendChild(style);
}

/**
 * Hiệu ứng cho nút tải xuống
 */
function initDownloadButtonEffects() {
  const downloadBtn = document.querySelector('.download-button');
  if (!downloadBtn) return;
  
  downloadBtn.addEventListener('click', function(e) {
    // Ngăn hành động mặc định nếu là nút premium
    if (this.getAttribute('data-premium') === 'true') {
      e.preventDefault();
      
      // Hiệu ứng rung khi click vào nút premium
      this.classList.add('shake-button');
      
      // Hiển thị thông báo premium
      const premiumToast = document.createElement('div');
      premiumToast.classList.add('premium-toast');
      premiumToast.innerHTML = `
        <i class="fas fa-lock"></i>
        <span>Đây là tài liệu premium. Vui lòng đăng nhập để mua.</span>
      `;
      document.body.appendChild(premiumToast);
      
      // Hiệu ứng hiển thị và ẩn toast
      setTimeout(() => {
        premiumToast.classList.add('show-toast');
      }, 100);
      
      setTimeout(() => {
        premiumToast.classList.remove('show-toast');
        setTimeout(() => {
          premiumToast.remove();
        }, 300);
      }, 3000);
      
      // Xóa hiệu ứng rung
      setTimeout(() => {
        this.classList.remove('shake-button');
      }, 600);
    }
  });
  
  // Thêm CSS cho hiệu ứng
  const style = document.createElement('style');
  style.textContent = `
    .shake-button {
      animation: shake 0.6s cubic-bezier(.36,.07,.19,.97) both;
    }
    
    @keyframes shake {
      10%, 90% { transform: translate3d(-1px, 0, 0); }
      20%, 80% { transform: translate3d(2px, 0, 0); }
      30%, 50%, 70% { transform: translate3d(-3px, 0, 0); }
      40%, 60% { transform: translate3d(3px, 0, 0); }
    }
    
    .premium-toast {
      position: fixed;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%) translateY(100px);
      background-color: #343a40;
      color: white;
      padding: 15px 25px;
      border-radius: 8px;
      box-shadow: 0 5px 15px rgba(0,0,0,0.2);
      display: flex;
      align-items: center;
      gap: 10px;
      z-index: 1000;
      opacity: 0;
      transition: transform 0.3s ease, opacity 0.3s ease;
    }
    
    .premium-toast i {
      color: #FFD700;
    }
    
    .show-toast {
      transform: translateX(-50%) translateY(0);
      opacity: 1;
    }
  `;
  document.head.appendChild(style);
}

/**
 * Cải thiện các phần tử xem trước trang
 */
function initPreviewPageEnhancements() {
  const previewPages = document.querySelectorAll('.preview-page');
  
  previewPages.forEach((page, index) => {
    // Thêm hiệu ứng hover và staggered animation
    page.style.transitionDelay = `${index * 0.05}s`;
    
    page.addEventListener('click', function() {
      // Xóa class active từ tất cả các trang
      previewPages.forEach(p => p.classList.remove('active'));
      
      // Thêm class active cho trang được click
      this.classList.add('active');
      
      // Cập nhật hình ảnh preview chính
      const mainPreview = document.querySelector('.preview-image img');
      const thisImage = this.querySelector('img');
      
      if (mainPreview && thisImage) {
        // Hiệu ứng fade
        mainPreview.style.opacity = '0';
        
        setTimeout(() => {
          mainPreview.src = thisImage.src;
          mainPreview.style.opacity = '1';
        }, 300);
      }
    });
  });
  
  // Thêm CSS cho hiệu ứng
  const style = document.createElement('style');
  style.textContent = `
    .preview-image img {
      transition: opacity 0.3s ease;
    }
    
    .preview-page {
      transition: transform 0.3s ease, opacity 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
      transition-delay: var(--delay, 0s);
    }
  `;
  document.head.appendChild(style);
}
