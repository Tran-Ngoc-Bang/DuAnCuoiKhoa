/**
 * Tính năng điều khiển slider tài liệu nổi bật
 */

// Hàm cuộn slider trái phải
function scrollSlider(direction) {
  const slider = document.querySelector('.featured-document-cards');
  const cardWidth = document.querySelector('.featured-document-card').offsetWidth;
  const gap = 24; // Gap giữa các thẻ (1.5rem = 24px)
  const scrollAmount = cardWidth + gap;
  
  if (direction === 'left') {
    slider.scrollBy({
      left: -scrollAmount,
      behavior: 'smooth'
    });
  } else {
    slider.scrollBy({
      left: scrollAmount,
      behavior: 'smooth'
    });
  }
}

// Tối ưu auto-scroll bằng requestAnimationFrame và IntersectionObserver
let autoScrollRunning = false;
let autoScrollHover = false;
let autoScrollTouch = false;
let autoScrollInView = true;
let lastTimestamp = 0;
const AUTO_SCROLL_INTERVAL_MS = 5000;

function rafAutoScroll(timestamp) {
  if (!autoScrollRunning) return;
  if (!lastTimestamp) lastTimestamp = timestamp;

  const elapsed = timestamp - lastTimestamp;
  if (elapsed >= AUTO_SCROLL_INTERVAL_MS && autoScrollInView && !autoScrollHover && !autoScrollTouch) {
    scrollSlider('right');
    const slider = document.querySelector('.featured-document-cards');
    if (slider && slider.scrollLeft + slider.clientWidth >= slider.scrollWidth - 2) {
      setTimeout(() => {
        slider.scrollTo({ left: 0, behavior: 'smooth' });
      }, 1000);
    }
    lastTimestamp = timestamp;
  }
  requestAnimationFrame(rafAutoScroll);
}

function startAutoScrollRaf() {
  if (autoScrollRunning) return;
  autoScrollRunning = true;
  lastTimestamp = 0;
  requestAnimationFrame(rafAutoScroll);
}

function stopAutoScrollRaf() {
  autoScrollRunning = false;
}

// Khởi tạo khi DOM sẵn sàng
document.addEventListener('DOMContentLoaded', () => {
  const slider = document.querySelector('.featured-document-cards');
  if (!slider) return;

  // Quan sát hiển thị trong viewport để bật/tắt auto-scroll
  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        autoScrollInView = entry.isIntersecting && entry.intersectionRatio > 0.1;
        if (autoScrollInView) {
          startAutoScrollRaf();
        } else {
          stopAutoScrollRaf();
        }
      });
    }, { threshold: [0, 0.1, 0.25] });
    observer.observe(slider);
  } else {
    // Fallback: luôn chạy
    startAutoScrollRaf();
  }

  // Tạm dừng khi hover
  slider.addEventListener('mouseenter', () => { autoScrollHover = true; stopAutoScrollRaf(); });
  slider.addEventListener('mouseleave', () => { autoScrollHover = false; if (autoScrollInView) startAutoScrollRaf(); });

  // Tạm dừng khi chạm (mobile)
  slider.addEventListener('touchstart', () => { autoScrollTouch = true; stopAutoScrollRaf(); }, { passive: true });
  slider.addEventListener('touchend', () => { autoScrollTouch = false; if (autoScrollInView) startAutoScrollRaf(); }, { passive: true });

  // Nút điều hướng giữ nguyên
});

// Xử lý hiệu ứng phụ cho các nút điều hướng
document.addEventListener('DOMContentLoaded', () => {
  const navButtons = document.querySelectorAll('.slider-nav-button');
  
  navButtons.forEach(button => {
    button.addEventListener('click', function() {
      // Thêm hiệu ứng nhấn cho nút
      this.classList.add('active');
      setTimeout(() => {
        this.classList.remove('active');
      }, 200);
    });
  });
});
