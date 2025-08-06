/* Account Pages JavaScript */

// Initialize account dashboard
function initAccountDashboard() {
  // User menu toggle
  initUserMenu();
  
  // Format numbers in the stats section
  formatNumberStats();
  
  // Init account sidebar menu
  initAccountSidebar();
  
  // Init document actions
  initDocumentActions();
}

// Initialize the user menu in the header
function initUserMenu() {
  console.log("DOM content loaded, initializing account functionality");

  // Xử lý trực tiếp cho avatar dropdown (code mới)
  console.log("Setting up direct user menu toggle handlers");
  
  const avatarButton = document.getElementById('avatarDropdownButton');
  const userDropdown = document.getElementById('avatarDropdownMenu');
  
  console.log("User menu elements found:", avatarButton && userDropdown ? 1 : 0);
  
  if (avatarButton && userDropdown) {
    // Toggle dropdown khi click vào avatar button
    avatarButton.addEventListener('click', function(e) {
      e.stopPropagation();
      userDropdown.classList.toggle('show');
      // Cập nhật thuộc tính aria-expanded
      const expanded = userDropdown.classList.contains('show');
      avatarButton.setAttribute('aria-expanded', expanded ? 'true' : 'false');
    });
    
    // Đóng dropdown khi click bên ngoài
    document.addEventListener('click', function(e) {
      if (!avatarButton.contains(e.target) && !userDropdown.contains(e.target)) {
        userDropdown.classList.remove('show');
        avatarButton.setAttribute('aria-expanded', 'false');
      }
    });
    
    // Đóng dropdown khi nhấn phím Escape
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape' && userDropdown.classList.contains('show')) {
        userDropdown.classList.remove('show');
        avatarButton.setAttribute('aria-expanded', 'false');
      }
    });
  } else {
    // Code fallback cho phiên bản cũ (tương thích ngược)
    const userMenuToggle = document.querySelector('.user-menu-toggle');
    const userMenu = document.querySelector('.user-menu');
    
    console.log("User menu toggle elements found:", userMenuToggle && userMenu ? 1 : 0);
    
    if (userMenuToggle && userMenu) {
      // Mở dropdown khi click vào avatar
      userMenuToggle.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        // Toggle dropdown
        userMenu.classList.toggle('open');
        
        // Tạo hiệu ứng xoay mũi tên
        const arrow = userMenuToggle.querySelector('.fa-chevron-down');
        if (arrow) {
          if (userMenu.classList.contains('open')) {
            arrow.style.transform = 'rotate(180deg)';
          } else {
            arrow.style.transform = 'rotate(0)';
          }
        }
      });
      
      // Close menu when clicking outside
      document.addEventListener('click', function(e) {
        if (!userMenu.contains(e.target) && !userMenuToggle.contains(e.target)) {
          userMenu.classList.remove('open');
          
          // Reset arrow rotation
          const arrow = userMenuToggle.querySelector('.fa-chevron-down');
          if (arrow) {
            arrow.style.transform = 'rotate(0)';
          }
        }
      });
    } else {
      console.log("No user menus found on page!");
    }
  }
  
  // Xử lý cho mobile menu user
  const mobileUserInfo = document.querySelector('.mobile-user-info');
  if (mobileUserInfo) {
    mobileUserInfo.addEventListener('click', function() {
      window.location.href = 'account/dashboard.html';
    });
  }
}

// Format numbers with commas
function formatNumberStats() {
  const stats = document.querySelectorAll('.stat-value');
  if (stats.length) {
    stats.forEach(stat => {
      let value = parseInt(stat.textContent.replace(/[^0-9]/g, ''));
      stat.textContent = value.toLocaleString();
    });
  }
}

// Initialize the account sidebar
function initAccountSidebar() {
  // Get current page URL
  const currentPath = window.location.pathname;
  
  // Set active menu item based on URL
  const menuLinks = document.querySelectorAll('.account-menu-link');
  if (menuLinks.length) {
    menuLinks.forEach(link => {
      const linkPath = link.getAttribute('href');
      if (currentPath.includes(linkPath) || 
         (currentPath.endsWith('dashboard.html') && linkPath.includes('dashboard.html'))) {
        link.classList.add('active');
      }
      
      // Add ripple effect to links
      link.addEventListener('click', function(e) {
        const rect = this.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        
        const ripple = document.createElement('span');
        ripple.classList.add('link-ripple');
        ripple.style.left = `${x}px`;
        ripple.style.top = `${y}px`;
        
        this.appendChild(ripple);
        
        setTimeout(() => {
          ripple.remove();
        }, 600);
      });
    });
  }
}

// Initialize document actions
function initDocumentActions() {
  const actionButtons = document.querySelectorAll('.document-action-btn');
  
  if (actionButtons.length) {
    actionButtons.forEach(btn => {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        const action = this.getAttribute('data-action');
        const documentId = this.closest('.document-item').getAttribute('data-id');
        
        switch(action) {
          case 'view':
            window.location.href = `../document-detail.html?id=${documentId}`;
            break;
          case 'edit':
            window.location.href = `edit-document.html?id=${documentId}`;
            break;
          case 'delete':
            if (confirm('Bạn có chắc chắn muốn xóa tài liệu này không?')) {
              // API call would go here
              this.closest('.document-item').remove();
              showToast('Đã xóa tài liệu thành công!', 'success');
            }
            break;
        }
      });
    });
  }
}

// Toast notification function
function showToast(message, type = 'info') {
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  const icon = document.createElement('i');
  switch(type) {
    case 'success':
      icon.className = 'fas fa-check-circle';
      break;
    case 'error':
      icon.className = 'fas fa-exclamation-circle';
      break;
    case 'warning':
      icon.className = 'fas fa-exclamation-triangle';
      break;
    default:
      icon.className = 'fas fa-info-circle';
  }
  
  const textSpan = document.createElement('span');
  textSpan.textContent = message;
  
  toast.appendChild(icon);
  toast.appendChild(textSpan);
  
  // Add toast to container or create one if it doesn't exist
  let toastContainer = document.querySelector('.toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container';
    document.body.appendChild(toastContainer);
  }
  
  toastContainer.appendChild(toast);
  
  // Show the toast
  setTimeout(() => {
    toast.classList.add('show');
  }, 10);
  
  // Remove toast after 3 seconds
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => {
      toast.remove();
      
      // Remove container if empty
      if (toastContainer.children.length === 0) {
        toastContainer.remove();
      }
    }, 300);
  }, 3000);
}

// Trực tiếp xử lý dropdown khi click vào avatar trên header
function handleUserMenuToggle() {
  console.log("Setting up direct user menu toggle handlers");
  // Tìm tất cả các menu trên trang
  const userMenus = document.querySelectorAll('.user-menu');
  
  if (userMenus.length) {
    userMenus.forEach(menu => {
      const toggle = menu.querySelector('.user-menu-toggle');
      if (!toggle) return;
      
      // Xóa listener cũ nếu có để tránh trùng lặp
      toggle.removeEventListener('click', handleToggleClick);
      // Thêm listener mới
      toggle.addEventListener('click', handleToggleClick);
    });
    
    // Xử lý đóng dropdown khi click ra ngoài
    document.removeEventListener('click', handleOutsideClick);
    document.addEventListener('click', handleOutsideClick);
  } else {
    console.log("No user menus found on page!");
  }
}

// Handle toggle click event
function handleToggleClick(e) {
  e.preventDefault();
  e.stopPropagation();
  
  console.log("User menu toggle clicked");
  
  const userMenu = this.closest('.user-menu');
  if (!userMenu) return;
  
  // Toggle class open
  userMenu.classList.toggle('open');
  
  // Rotate arrow
  const arrow = this.querySelector('.fa-chevron-down');
  if (arrow) {
    arrow.style.transform = userMenu.classList.contains('open') ? 'rotate(180deg)' : 'rotate(0)';
  }
  
  // Create ripple effect
  const rect = this.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  
  const ripple = document.createElement('span');
  ripple.classList.add('avatar-ripple');
  ripple.style.left = `${x}px`;
  ripple.style.top = `${y}px`;
  
  this.appendChild(ripple);
  
  setTimeout(() => {
    ripple.remove();
  }, 600);
}

// Handle outside click to close dropdown
function handleOutsideClick(e) {
  const userMenus = document.querySelectorAll('.user-menu');
  
  userMenus.forEach(menu => {
    const toggle = menu.querySelector('.user-menu-toggle');
    if (!toggle) return;
    
    if (!menu.contains(e.target)) {
      menu.classList.remove('open');
      
      const arrow = toggle.querySelector('.fa-chevron-down');
      if (arrow) {
        arrow.style.transform = 'rotate(0)';
      }
    }
  });
}

// Initialize all functionality when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  console.log("DOM content loaded, initializing account functionality");
  initAccountDashboard();
  initDocumentActions();
  
  // Trực tiếp khởi tạo dropdown user menu
  handleUserMenuToggle();
  
  // Log kết quả sau khi thiết lập
  console.log("User menu elements found:", document.querySelectorAll('.user-menu').length);
  console.log("User menu toggle elements found:", document.querySelectorAll('.user-menu-toggle').length);
});