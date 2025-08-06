// Main JavaScript for EduShare

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
  initMobileMenu();
  initDarkMode();
  initBackToTop();
  initSearch();
  initCategoryFilters();
  initFeaturedSlider();
  initUserMenu();
  initHeaderScroll();
});

// Header Scroll Effect
function initHeaderScroll() {
  const header = document.querySelector('.header');
  if (!header) return;
  
  const scrollThreshold = 50;
  
  function handleScroll() {
    if (window.scrollY > scrollThreshold) {
      header.classList.add('scrolled');
    } else {
      header.classList.remove('scrolled');
    }
  }
  
  // Call once on load
  handleScroll();
  
  // Add scroll event listener
  window.addEventListener('scroll', handleScroll);
}

// Mobile Menu
function initMobileMenu() {
  const mobileMenuBtn = document.getElementById('mobileMenuBtn');
  const mobileMenu = document.getElementById('mobileMenu');
  
  if (mobileMenuBtn && mobileMenu) {
    mobileMenuBtn.addEventListener('click', function() {
      mobileMenu.classList.toggle('active');
      document.body.classList.toggle('menu-open');
    });
    
    // Close menu when clicking outside
    document.addEventListener('click', function(event) {
      if (!mobileMenu.contains(event.target) && !mobileMenuBtn.contains(event.target) && mobileMenu.classList.contains('active')) {
        mobileMenu.classList.remove('active');
        document.body.classList.remove('menu-open');
      }
    });
  }
}

// Dark Mode Toggle
function initDarkMode() {
  const darkModeToggle = document.getElementById('darkModeToggle');
  const darkModeHandle = document.getElementById('darkModeHandle');
  
  // Check for saved user preference
  const savedDarkMode = localStorage.getItem('darkMode') === 'true';
  
  // Check for system preference
  const prefersDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  
  // Set initial state
  if (savedDarkMode || (!localStorage.getItem('darkMode') && prefersDarkMode)) {
    document.body.classList.add('dark-mode');
    if (darkModeToggle) {
      darkModeToggle.classList.add('active');
      if (darkModeHandle) {
        darkModeHandle.style.transform = 'translateX(24px)';
      }
    }
  } else {
    // Đảm bảo dark mode handle ở vị trí mặc định
    if (darkModeHandle) {
      darkModeHandle.style.transform = 'translateX(0)';
    }
  }
  
  // Toggle dark mode
  if (darkModeToggle) {
    darkModeToggle.addEventListener('click', function() {
      document.body.classList.toggle('dark-mode');
      this.classList.toggle('active');
      
      // Thêm xử lý cho phần nút trượt với transform thay vì left
      if (darkModeHandle) {
        if (document.body.classList.contains('dark-mode')) {
          darkModeHandle.style.transform = 'translateX(24px)';
        } else {
          darkModeHandle.style.transform = 'translateX(0)';
        }
      }
      
      localStorage.setItem('darkMode', document.body.classList.contains('dark-mode'));
    });
  }
}

// Back To Top Button
function initBackToTop() {
  const backToTopBtn = document.getElementById('backToTop');
  
  if (backToTopBtn) {
    // Show/hide button based on scroll position
    window.addEventListener('scroll', function() {
      if (window.pageYOffset > 300) {
        backToTopBtn.classList.add('visible');
      } else {
        backToTopBtn.classList.remove('visible');
      }
    });
    
    // Scroll to top when clicked
    backToTopBtn.addEventListener('click', function() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    });
  }
}

// Search Functionality
function initSearch() {
  // Header search
  const headerSearchInput = document.getElementById('headerSearchInput');
  const headerSearchBtn = document.getElementById('headerSearchBtn');
  
  if (headerSearchBtn && headerSearchInput) {
    headerSearchBtn.addEventListener('click', function() {
      performSearch(headerSearchInput);
    });
    
    headerSearchInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        performSearch(headerSearchInput);
      }
    });
  }
  
  // Main search on index page
  const searchInput = document.getElementById('searchInput');
  const searchBtn = document.getElementById('searchBtn');
  
  if (searchBtn && searchInput) {
    searchBtn.addEventListener('click', function() {
      performSearch(searchInput);
    });
    
    searchInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        performSearch(searchInput);
      }
    });
  }
}

function performSearch(inputElement) {
  if (!inputElement) return;
  
  const query = inputElement.value.trim();
  if (query) {
    // Check if we're on the index page or elsewhere
    const isHomePage = window.location.pathname === '/' || 
                      window.location.pathname === '/index.html' || 
                      window.location.pathname.endsWith('/');
    
    const searchUrl = isHomePage 
      ? `pages/search.html?q=${encodeURIComponent(query)}`
      : `search.html?q=${encodeURIComponent(query)}`;
      
    window.location.href = searchUrl;
  }
}

// Category Filters
function initCategoryFilters() {
  const categoryPills = document.querySelectorAll('.category-pill');
  const categoryCards = document.querySelectorAll('.category-card');
  
  if (categoryPills.length && categoryCards.length) {
    categoryPills.forEach(pill => {
      pill.addEventListener('click', function() {
        // Update active state
        categoryPills.forEach(p => p.classList.remove('active'));
        this.classList.add('active');
        
        const category = this.getAttribute('data-category');
        
        // Filter cards
        if (category === 'all') {
          categoryCards.forEach(card => {
            card.style.display = 'block';
          });
        } else {
          categoryCards.forEach(card => {
            if (card.getAttribute('data-category') === category) {
              card.style.display = 'block';
            } else {
              card.style.display = 'none';
            }
          });
        }
      });
    });
  }
}

// Featured Documents Navigation
function initFeaturedSlider() {
  const featuredPrevBtn = document.getElementById('featuredPrevBtn');
  const featuredNextBtn = document.getElementById('featuredNextBtn');
  const documentsGrid = document.querySelector('.documents-grid');
  
  if (featuredPrevBtn && featuredNextBtn && documentsGrid) {
    let currentPosition = 0;
    
    // Calculate the number of cards visible
    const cardWidth = 320; // Approx width of a card + gap
    const gridWidth = documentsGrid.offsetWidth;
    const visibleCards = Math.floor(gridWidth / cardWidth);
    
    // Get all cards
    const cards = documentsGrid.querySelectorAll('.document-card');
    
    // Function to slide the grid
    function slideFeatured(direction) {
      const totalCards = cards.length;
      
      if (direction === 'prev') {
        currentPosition = Math.max(currentPosition - 1, 0);
      } else {
        currentPosition = Math.min(currentPosition + 1, totalCards - visibleCards);
      }
      
      // Update grid position
      const translateValue = -currentPosition * cardWidth;
      documentsGrid.style.transform = `translateX(${translateValue}px)`;
      
      // Update button states
      featuredPrevBtn.disabled = currentPosition === 0;
      featuredNextBtn.disabled = currentPosition >= totalCards - visibleCards;
    }
    
    // Add event listeners
    featuredPrevBtn.addEventListener('click', () => slideFeatured('prev'));
    featuredNextBtn.addEventListener('click', () => slideFeatured('next'));
    
    // Initial button state
    featuredPrevBtn.disabled = currentPosition === 0;
    featuredNextBtn.disabled = cards.length <= visibleCards;
    
    // Make grid scrollable
    documentsGrid.style.display = 'flex';
    documentsGrid.style.flexWrap = 'nowrap';
    documentsGrid.style.overflow = 'hidden';
    documentsGrid.style.transition = 'transform 0.3s ease';
    
    // Set min-width for cards
    cards.forEach(card => {
      card.style.minWidth = `${cardWidth - 30}px`; // Subtract gap
      card.style.marginRight = '30px';
    });
  }
}

// User Menu Avatar Functionality
function initUserMenu() {
  const userMenu = document.querySelector('.user-menu');
  const userAvatar = document.querySelector('.user-menu .user-avatar');
  
  if (userMenu && userAvatar) {
    // Toggle dropdown on avatar click
    userAvatar.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      
      // Toggle active class
      userMenu.classList.toggle('active');
      
      // Add animation effect
      userAvatar.style.transform = 'scale(1.1)';
      setTimeout(() => {
        userAvatar.style.transform = '';
      }, 300);
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
      if (!userMenu.contains(e.target)) {
        userMenu.classList.remove('active');
      }
    });
    
    // Make dropdown links clickable and add ripple effect
    const dropdownLinks = document.querySelectorAll('.user-dropdown-links a');
    dropdownLinks.forEach(link => {
      link.addEventListener('click', function(e) {
        // Create ripple effect
        const ripple = document.createElement('span');
        ripple.classList.add('link-ripple');
        
        // Set ripple position
        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = `${size}px`;
        ripple.style.left = `${x}px`;
        ripple.style.top = `${y}px`;
        
        this.appendChild(ripple);
        
        // Remove ripple after animation completes
        setTimeout(() => {
          ripple.remove();
        }, 500);
      });
    });
    
    // Add ripple effect to logout button 
    const logoutBtn = document.querySelector('.dropdown-logout-btn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', function(e) {
        // Create ripple effect
        const ripple = document.createElement('span');
        ripple.classList.add('link-ripple');
        ripple.style.background = 'rgba(239, 68, 68, 0.2)'; // Red color for logout
        
        // Set ripple position
        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = `${size}px`;
        ripple.style.left = `${x}px`;
        ripple.style.top = `${y}px`;
        
        this.appendChild(ripple);
        
        // Remove ripple after animation completes
        setTimeout(() => {
          ripple.remove();
        }, 500);
      });
    }
  }
}
