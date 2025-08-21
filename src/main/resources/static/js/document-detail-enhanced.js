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

  // Cho phép bật/tắt hiệu ứng lắc khi rê chuột trên hero
  const enableHeroHoverParallax = false;

  if (enableHeroHoverParallax) {
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
  }

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
      const offset = Math.max(0, 1 - (elementPosition / window.innerHeight));
      element.style.transform = `translateY(${offset * 10}px)`;
      element.style.transition = 'transform 0.2s ease-out';
    });
  });
}

/**
 * Hiệu ứng chuyển tab mượt mà
 */
function initTabTransitions() {
  const tabs = document.querySelectorAll('.content-tab');
  const contents = document.querySelectorAll('.tab-content');

  tabs.forEach(tab => {
    tab.addEventListener('click', function() {
      const target = this.getAttribute('data-tab');

      tabs.forEach(t => t.classList.remove('active'));
      contents.forEach(c => c.classList.remove('active'));

      this.classList.add('active');
      document.getElementById(`${target}-content`).classList.add('active');
    });
  });
}

/**
 * Hiệu ứng cho nút tải xuống
 */
function initDownloadButtonEffects() {
  const downloadButton = document.querySelector('.download-button');
  if (!downloadButton) return;

  downloadButton.addEventListener('mouseenter', function() {
    this.style.transform = 'translateY(-2px)';
    this.style.boxShadow = '0 8px 20px rgba(0,0,0,0.15)';
  });

  downloadButton.addEventListener('mouseleave', function() {
    this.style.transform = 'translateY(0)';
    this.style.boxShadow = 'none';
  });
}

/**
 * Cải tiến hiển thị trang xem trước
 */
function initPreviewPageEnhancements() {
  const previewPages = document.querySelectorAll('.preview-page');
  if (!previewPages.length) return;

  previewPages.forEach((page, index) => {
    // Thêm số trang như watermark
    const pageNumber = document.createElement('div');
    pageNumber.className = 'page-number-watermark';
    pageNumber.textContent = `Trang ${index + 1}`;
    page.appendChild(pageNumber);

    // Hiệu ứng hover nhẹ nhàng
    page.addEventListener('mouseenter', function() {
      page.style.transform = 'translateY(-4px)';
      page.style.boxShadow = '0 10px 25px rgba(0,0,0,0.12)';
    });

    page.addEventListener('mouseleave', function() {
      page.style.transform = 'translateY(0)';
      page.style.boxShadow = 'var(--shadow)';
    });
  });
}
