// JavaScript for Categories page
document.addEventListener('DOMContentLoaded', function() {
  // Initialize category filter tabs
  initCategoryFilterTabs();
  
  // Initialize search in hero section
  initHeroSearch();
  
  // Add animation to stats on scroll
  initStatsAnimation();
  
  // Add hover animations
  initHoverEffects();
  
  // Add animation to floating cards
  initFloatingCards();
});

// Category Filter Tabs
function initCategoryFilterTabs() {
  const filterTabs = document.querySelectorAll('.category-tab');
  const categoryCards = document.querySelectorAll('.category-card');
  
  if (filterTabs.length === 0 || categoryCards.length === 0) return;
  
  filterTabs.forEach(tab => {
    tab.addEventListener('click', function() {
      // Remove active class from all tabs
      filterTabs.forEach(t => t.classList.remove('active'));
      
      // Add active class to clicked tab
      this.classList.add('active');
      
      const filter = this.getAttribute('data-filter');
      
      if (filter === 'all') {
        // Show all categories
        categoryCards.forEach(card => {
          card.style.display = 'flex';
          setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
          }, 50);
        });
      } else {
        // Filter categories by group
        categoryCards.forEach(card => {
          if (card.getAttribute('data-group') === filter) {
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

// Hero section functionality
function initHeroSearch() {
  // Đã loại bỏ phần search form theo yêu cầu
  return;
}

// Stats Animation
function initStatsAnimation() {
  const statNumbers = document.querySelectorAll('.stat-number');
  
  if (statNumbers.length === 0) return;
  
  const options = {
    threshold: 0.5
  };
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const statEl = entry.target;
        const target = parseInt(statEl.getAttribute('data-value'));
        const duration = 2000; // 2 seconds
        
        animateNumber(statEl, 0, target, duration);
        
        // Unobserve after animation starts
        observer.unobserve(statEl);
      }
    });
  }, options);
  
  statNumbers.forEach(stat => {
    // Store the target value as a data attribute
    const currentText = stat.textContent;
    let targetValue = parseInt(currentText.replace(/[^0-9]/g, ''));
    
    // Handle "K" and "M" suffixes
    if (currentText.includes('K')) {
      targetValue *= 1000;
    } else if (currentText.includes('M')) {
      targetValue *= 1000000;
    }
    
    stat.setAttribute('data-value', targetValue);
    observer.observe(stat);
  });
  
  function animateNumber(element, start, end, duration) {
    let startTimestamp = null;
    const step = (timestamp) => {
      if (!startTimestamp) startTimestamp = timestamp;
      const progress = Math.min((timestamp - startTimestamp) / duration, 1);
      let currentValue = Math.floor(progress * (end - start) + start);
      
      // Format the number (add K or M for thousands/millions)
      let formattedValue;
      if (end >= 1000000) {
        formattedValue = (currentValue / 1000000).toFixed(1) + 'M';
      } else if (end >= 1000) {
        formattedValue = (currentValue / 1000).toFixed(1) + 'K';
      } else {
        formattedValue = currentValue.toString();
      }
      
      // Remove ".0" from values like "5.0K"
      formattedValue = formattedValue.replace('.0', '');
      
      element.textContent = formattedValue;
      
      if (progress < 1) {
        window.requestAnimationFrame(step);
      }
    };
    
    window.requestAnimationFrame(step);
  }
}

// Hover Effects
function initHoverEffects() {
  // Enhance category cards with hover effects
  const categoryCards = document.querySelectorAll('.category-card');
  
  categoryCards.forEach(card => {
    const iconContainer = card.querySelector('.category-icon-container');
    const title = card.querySelector('.category-title');
    
    card.addEventListener('mouseenter', function() {
      if (iconContainer) {
        iconContainer.style.transform = 'scale(1.1) rotate(5deg)';
      }
      if (title) {
        title.style.color = 'var(--primary)';
      }
    });
    
    card.addEventListener('mouseleave', function() {
      if (iconContainer) {
        iconContainer.style.transform = '';
      }
      if (title) {
        title.style.color = '';
      }
    });
  });
  
  // Enhance topic tags with hover effects
  const topicTags = document.querySelectorAll('.topic-tag');
  
  topicTags.forEach(tag => {
    tag.addEventListener('mouseenter', function() {
      this.style.transform = 'translateY(-3px)';
      this.style.backgroundColor = 'var(--primary)';
      this.style.color = 'var(--white)';
      this.style.boxShadow = '0 4px 10px rgba(67, 97, 238, 0.2)';
    });
    
    tag.addEventListener('mouseleave', function() {
      this.style.transform = '';
      this.style.backgroundColor = '';
      this.style.color = '';
      this.style.boxShadow = '';
    });
  });
  
  // Enhance CTA buttons with hover effects
  const ctaButtons = document.querySelectorAll('.cta-btn');
  
  ctaButtons.forEach(button => {
    button.addEventListener('mouseenter', function() {
      this.style.transform = 'translateY(-3px)';
      
      if (this.classList.contains('cta-btn-primary')) {
        this.style.boxShadow = '0 5px 15px rgba(255, 255, 255, 0.3)';
      } else {
        this.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
      }
    });
    
    button.addEventListener('mouseleave', function() {
      this.style.transform = '';
      this.style.boxShadow = '';
      this.style.backgroundColor = '';
    });
  });
}

// Add interactive movement to the floating cards
function initFloatingCards() {
  const heroCards = document.querySelectorAll('.hero-card');
  const heroSection = document.querySelector('.categories-hero');
  
  if (heroCards.length === 0 || !heroSection) return;
  
  // Add mouse movement parallax effect
  heroSection.addEventListener('mousemove', (e) => {
    const { left, top, width, height } = heroSection.getBoundingClientRect();
    const x = (e.clientX - left) / width - 0.5;
    const y = (e.clientY - top) / height - 0.5;
    
    heroCards.forEach((card, index) => {
      const factor = (index + 1) * 10; // Different factor for each card
      
      requestAnimationFrame(() => {
        card.style.transform = `translateX(${x * factor}px) translateY(${y * factor}px)`;
      });
    });
  });
  
  // Reset position when mouse leaves
  heroSection.addEventListener('mouseleave', () => {
    heroCards.forEach(card => {
      card.style.transform = '';
    });
  });
  
  // Add touch events for mobile
  heroSection.addEventListener('touchmove', (e) => {
    if (e.touches.length !== 1) return;
    
    const touch = e.touches[0];
    const { left, top, width, height } = heroSection.getBoundingClientRect();
    const x = (touch.clientX - left) / width - 0.5;
    const y = (touch.clientY - top) / height - 0.5;
    
    heroCards.forEach((card, index) => {
      const factor = (index + 1) * 5; // Smaller factor for touch
      
      requestAnimationFrame(() => {
        card.style.transform = `translateX(${x * factor}px) translateY(${y * factor}px)`;
      });
    });
  });
}