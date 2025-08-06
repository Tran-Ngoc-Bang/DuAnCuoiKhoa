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

// Thêm hiệu ứng cuộn tự động sau mỗi 5 giây
let autoScrollInterval;

function startAutoScroll() {
  autoScrollInterval = setInterval(() => {
    scrollSlider('right');
    
    // Kiểm tra nếu đã cuộn đến cuối, quay lại đầu
    const slider = document.querySelector('.featured-document-cards');
    if (slider.scrollLeft + slider.clientWidth >= slider.scrollWidth) {
      // Đã đến cuối, cuộn về đầu
      setTimeout(() => {
        slider.scrollTo({
          left: 0,
          behavior: 'smooth'
        });
      }, 1000);
    }
  }, 5000);
}

function stopAutoScroll() {
  clearInterval(autoScrollInterval);
}

// Bắt đầu auto-scroll khi trang được tải
document.addEventListener('DOMContentLoaded', () => {
  startAutoScroll();
  
  // Dừng auto-scroll khi người dùng hover vào slider
  const slider = document.querySelector('.featured-document-cards');
  if (slider) {
    slider.addEventListener('mouseenter', stopAutoScroll);
    slider.addEventListener('mouseleave', startAutoScroll);
  }
  
  // Dừng auto-scroll khi người dùng chạm vào slider (mobile)
  slider.addEventListener('touchstart', stopAutoScroll, { passive: true });
  slider.addEventListener('touchend', startAutoScroll, { passive: true });
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
