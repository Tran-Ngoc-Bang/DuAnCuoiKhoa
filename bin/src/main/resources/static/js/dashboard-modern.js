/**
 * Dashboard Modern JavaScript
 * Xử lý các tương tác và chức năng cho dashboard người dùng
 */

document.addEventListener('DOMContentLoaded', function() {
  // Khởi tạo các chức năng
  initSidebar();
  initTooltips();
  initCharts();
  initSearchFilters();
  initNotifications();
  fadeInContent();
  initUserDropdown();
  initDocumentActions();
});

/**
 * Xử lý sidebar (thu gọn/mở rộng)
 */
function initSidebar() {
  const toggleBtn = document.getElementById('sidebarToggle');
  const dashboardWrapper = document.getElementById('dashboardWrapper');
  const backdrop = document.getElementById('sidebarBackdrop');
  const mobileToggle = document.getElementById('mobileToggle');
  
  if (toggleBtn && dashboardWrapper) {
    toggleBtn.addEventListener('click', function() {
      dashboardWrapper.classList.toggle('sidebar-collapsed');
      localStorage.setItem('sidebar-collapsed', dashboardWrapper.classList.contains('sidebar-collapsed'));
    });
    
    // Khôi phục trạng thái sidebar từ localStorage
    if (localStorage.getItem('sidebar-collapsed') === 'true') {
      dashboardWrapper.classList.add('sidebar-collapsed');
    }
  }
  
  // Xử lý sidebar trên mobile
  if (mobileToggle && dashboardWrapper && backdrop) {
    mobileToggle.addEventListener('click', function() {
      dashboardWrapper.classList.toggle('sidebar-open');
    });
    
    backdrop.addEventListener('click', function() {
      dashboardWrapper.classList.remove('sidebar-open');
    });
  }
  
  // Xử lý click vào item sidebar (thêm class active)
  const navLinks = document.querySelectorAll('.nav-menu-link');
  navLinks.forEach(link => {
    link.addEventListener('click', function() {
      // Xóa class active từ tất cả links
      navLinks.forEach(item => item.classList.remove('active'));
      // Thêm class active cho link được click
      this.classList.add('active');
    });
  });
}

/**
 * Khởi tạo tooltips cho sidebar khi thu gọn
 */
function initTooltips() {
  const menuItems = document.querySelectorAll('.nav-menu-item');
  
  menuItems.forEach(item => {
    const link = item.querySelector('.nav-menu-link');
    if (link) {
      const text = link.querySelector('.nav-menu-text').textContent.trim();
      item.setAttribute('data-title', text);
    }
  });
}

/**
 * Khởi tạo biểu đồ cho dashboard
 */
function initCharts() {
  // Biểu đồ hoạt động
  if (typeof ApexCharts !== 'undefined' && document.getElementById('activityChart')) {
    const activityOptions = {
      series: [{
        name: 'Tải xuống',
        data: [31, 40, 28, 51, 42, 109, 100, 120, 80, 95, 110, 85]
      }, {
        name: 'Lượt xem',
        data: [11, 32, 45, 32, 34, 52, 41, 60, 49, 52, 38, 47]
      }],
      chart: {
        height: 350,
        type: 'area',
        fontFamily: 'Nunito, sans-serif',
        toolbar: {
          show: false
        },
      },
      dataLabels: {
        enabled: false
      },
      stroke: {
        curve: 'smooth',
        width: 2
      },
      xaxis: {
        type: 'category',
        categories: ['Thg 1', 'Thg 2', 'Thg 3', 'Thg 4', 'Thg 5', 'Thg 6', 'Thg 7', 'Thg 8', 'Thg 9', 'Thg 10', 'Thg 11', 'Thg 12'],
        labels: {
          style: {
            colors: '#9ca3af',
            fontSize: '12px'
          }
        }
      },
      yaxis: {
        labels: {
          style: {
            colors: '#9ca3af',
            fontSize: '12px'
          },
          formatter: function (value) {
            return value.toFixed(0);
          }
        }
      },
      tooltip: {
        x: {
          format: 'dd/MM/yy HH:mm'
        },
      },
      colors: ['#4361ee', '#10b981'],
      fill: {
        type: 'gradient',
        gradient: {
          shadeIntensity: 1,
          opacityFrom: 0.7,
          opacityTo: 0.2,
          stops: [0, 90, 100]
        }
      },
      grid: {
        borderColor: '#e5e7eb',
        strokeDashArray: 4,
        yaxis: {
          lines: {
            show: true
          }
        }
      },
      legend: {
        position: 'top',
        horizontalAlign: 'right',
        fontSize: '14px',
        markers: {
          width: 10,
          height: 10,
          radius: 10
        }
      }
    };

    const activityChart = new ApexCharts(document.getElementById('activityChart'), activityOptions);
    activityChart.render();
  }
  
  // Biểu đồ phân loại tài liệu
  if (typeof ApexCharts !== 'undefined' && document.getElementById('documentChart')) {
    const documentOptions = {
      series: [44, 25, 13, 18],
      chart: {
        type: 'donut',
        height: 300,
        fontFamily: 'Nunito, sans-serif',
      },
      labels: ['Công nghệ', 'Giáo dục', 'Kinh tế', 'Khác'],
      colors: ['#4361ee', '#10b981', '#f59e0b', '#6b7280'],
      legend: {
        position: 'bottom',
        fontSize: '14px',
        markers: {
          width: 10,
          height: 10,
          radius: 10
        },
        itemMargin: {
          horizontal: 15,
          vertical: 5
        }
      },
      plotOptions: {
        pie: {
          donut: {
            size: '65%',
            labels: {
              show: true,
              total: {
                show: true,
                label: 'Tổng',
                fontSize: '16px',
                fontWeight: 600,
                color: '#111827'
              }
            }
          }
        }
      },
      dataLabels: {
        enabled: false
      },
      responsive: [{
        breakpoint: 480,
        options: {
          chart: {
            height: 260
          },
          legend: {
            position: 'bottom'
          }
        }
      }],
      tooltip: {
        enabled: true,
        y: {
          formatter: function(value) {
            return value + ' tài liệu';
          }
        }
      }
    };

    const documentChart = new ApexCharts(document.getElementById('documentChart'), documentOptions);
    documentChart.render();
  }
}

/**
 * Xử lý tìm kiếm và lọc
 */
function initSearchFilters() {
  const searchInput = document.getElementById('dashboardSearch');
  if (searchInput) {
    searchInput.addEventListener('input', function() {
      // Xử lý tìm kiếm realtime
      const searchTerm = this.value.toLowerCase().trim();
      const documents = document.querySelectorAll('.document-card');
      
      documents.forEach(doc => {
        const title = doc.querySelector('.document-title').textContent.toLowerCase();
        const category = doc.querySelector('.document-category').textContent.toLowerCase();
        
        if (title.includes(searchTerm) || category.includes(searchTerm)) {
          doc.style.display = '';
        } else {
          doc.style.display = 'none';
        }
      });
    });
  }
  
  // Bộ lọc
  const filterBtns = document.querySelectorAll('.section-btn[data-filter]');
  filterBtns.forEach(btn => {
    btn.addEventListener('click', function() {
      // Xóa active từ các nút khác
      filterBtns.forEach(item => item.classList.remove('active'));
      // Thêm active cho nút được click
      this.classList.add('active');
      
      const filter = this.getAttribute('data-filter');
      const documents = document.querySelectorAll('.document-card');
      
      if (filter === 'all') {
        documents.forEach(doc => {
          doc.style.display = '';
        });
      } else {
        documents.forEach(doc => {
          const docType = doc.getAttribute('data-type');
          if (docType === filter) {
            doc.style.display = '';
          } else {
            doc.style.display = 'none';
          }
        });
      }
    });
  });
}

/**
 * Xử lý thông báo
 */
function initNotifications() {
  const notificationBtn = document.querySelector('.notification-btn');
  
  if (notificationBtn) {
    notificationBtn.addEventListener('click', function() {
      // Hiển thị danh sách thông báo
      const hasDropdown = document.querySelector('.notification-dropdown');
      
      if (hasDropdown) {
        hasDropdown.classList.toggle('show');
      } else {
        // Tạo dropdown nếu chưa có
        const dropdown = document.createElement('div');
        dropdown.className = 'notification-dropdown fade-in';
        
        dropdown.innerHTML = `
          <div class="notification-header">
            <h4 class="notification-title">Thông báo</h4>
            <button class="mark-all-read">Đánh dấu đã đọc</button>
          </div>
          
          <div class="notification-list">
            <div class="notification-item unread">
              <div class="notification-icon success">
                <i class="fas fa-download"></i>
              </div>
              <div class="notification-content">
                <div class="notification-text">Tài liệu <strong>React Native Tutorial</strong> đã được tải xuống thành công.</div>
                <div class="notification-time">2 giờ trước</div>
              </div>
              <button class="notification-action"><i class="fas fa-ellipsis-v"></i></button>
            </div>
            
            <div class="notification-item unread">
              <div class="notification-icon primary">
                <i class="fas fa-star"></i>
              </div>
              <div class="notification-content">
                <div class="notification-text">Bạn đã nhận được 15 xu từ hệ thống. <a href="#">Kiểm tra ngay</a></div>
                <div class="notification-time">4 giờ trước</div>
              </div>
              <button class="notification-action"><i class="fas fa-ellipsis-v"></i></button>
            </div>
            
            <div class="notification-item">
              <div class="notification-icon warning">
                <i class="fas fa-exclamation-triangle"></i>
              </div>
              <div class="notification-content">
                <div class="notification-text">Tài khoản của bạn sắp hết hạn Premium. <a href="#">Gia hạn ngay</a></div>
                <div class="notification-time">1 ngày trước</div>
              </div>
              <button class="notification-action"><i class="fas fa-ellipsis-v"></i></button>
            </div>
          </div>
          
          <div class="notification-footer">
            <a href="#" class="view-all-notifications">Xem tất cả thông báo</a>
          </div>
        `;
        
        document.body.appendChild(dropdown);
        
        // Vị trí dropdown
        const btnRect = notificationBtn.getBoundingClientRect();
        dropdown.style.top = btnRect.bottom + 10 + 'px';
        dropdown.style.right = 20 + 'px';
        
        // Đóng dropdown khi click ra ngoài
        document.addEventListener('click', function closeDropdown(e) {
          if (!dropdown.contains(e.target) && e.target !== notificationBtn) {
            dropdown.classList.remove('show');
            dropdown.classList.add('hide');
            
            setTimeout(() => {
              dropdown.remove();
            }, 300);
            
            document.removeEventListener('click', closeDropdown);
          }
        });
        
        // Thêm css cho dropdown
        if (!document.getElementById('notificationStyles')) {
          const style = document.createElement('style');
          style.id = 'notificationStyles';
          style.textContent = `
            .notification-dropdown {
              position: fixed;
              width: 350px;
              background: white;
              border-radius: 0.5rem;
              box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
              z-index: 999;
              border: 1px solid var(--dashboard-border);
              overflow: hidden;
              opacity: 0;
              transform: translateY(10px);
              visibility: hidden;
              transition: opacity 0.3s ease, transform 0.3s ease, visibility 0.3s;
            }
            
            .notification-dropdown.show {
              opacity: 1;
              transform: translateY(0);
              visibility: visible;
            }
            
            .notification-dropdown.hide {
              opacity: 0;
              transform: translateY(10px);
              visibility: hidden;
            }
            
            .notification-header {
              padding: 1rem;
              display: flex;
              justify-content: space-between;
              align-items: center;
              border-bottom: 1px solid var(--dashboard-border);
            }
            
            .notification-title {
              margin: 0;
              font-size: 1rem;
              font-weight: 600;
              color: var(--dashboard-heading);
            }
            
            .mark-all-read {
              background: none;
              border: none;
              color: var(--primary);
              font-size: 0.875rem;
              cursor: pointer;
              padding: 0;
            }
            
            .notification-list {
              max-height: 300px;
              overflow-y: auto;
            }
            
            .notification-item {
              display: flex;
              align-items: flex-start;
              padding: 1rem;
              border-bottom: 1px solid var(--dashboard-border);
              transition: background 0.3s ease;
            }
            
            .notification-item:hover {
              background: var(--gray-50);
            }
            
            .notification-item.unread {
              background: var(--primary-light);
            }
            
            .notification-icon {
              width: 2.5rem;
              height: 2.5rem;
              border-radius: 0.5rem;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-right: 0.75rem;
              background: var(--primary-light);
              color: var(--primary);
              flex-shrink: 0;
            }
            
            .notification-icon.success {
              background: var(--success-light);
              color: var(--success);
            }
            
            .notification-icon.warning {
              background: var(--warning-light);
              color: var(--warning);
            }
            
            .notification-icon.danger {
              background: var(--danger-light);
              color: var(--danger);
            }
            
            .notification-content {
              flex: 1;
            }
            
            .notification-text {
              font-size: 0.875rem;
              margin-bottom: 0.25rem;
              color: var(--gray-700);
            }
            
            .notification-text a {
              color: var(--primary);
              text-decoration: none;
            }
            
            .notification-time {
              font-size: 0.75rem;
              color: var(--gray-500);
            }
            
            .notification-action {
              background: none;
              border: none;
              color: var(--gray-500);
              cursor: pointer;
              width: 2rem;
              height: 2rem;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
              transition: background 0.3s ease;
            }
            
            .notification-action:hover {
              background: var(--gray-200);
              color: var(--gray-700);
            }
            
            .notification-footer {
              padding: 0.75rem;
              text-align: center;
              border-top: 1px solid var(--dashboard-border);
            }
            
            .view-all-notifications {
              color: var(--primary);
              text-decoration: none;
              font-size: 0.875rem;
              font-weight: 500;
            }
          `;
          
          document.head.appendChild(style);
        }
      }
    });
  }
}

/**
 * Hiệu ứng hiển thị nội dung
 */
function fadeInContent() {
  const elements = document.querySelectorAll('.stat-card, .activity-card, .document-card');
  
  elements.forEach((el, index) => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    el.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    el.style.transitionDelay = (index * 0.05) + 's';
    
    setTimeout(() => {
      el.style.opacity = '1';
      el.style.transform = 'translateY(0)';
    }, 100);
  });
}

/**
 * Xử lý dropdown người dùng
 */
function initUserDropdown() {
  const userDropdown = document.querySelector('.user-dropdown');
  
  if (userDropdown) {
    userDropdown.addEventListener('click', function(e) {
      e.stopPropagation();
      
      // Tạo menu dropdown
      const hasMenu = document.querySelector('.user-menu-dropdown');
      
      if (hasMenu) {
        hasMenu.classList.toggle('show');
      } else {
        const menu = document.createElement('div');
        menu.className = 'user-menu-dropdown fade-in';
        
        menu.innerHTML = `
          <ul class="user-menu-list">
            <li><a href="profile.html"><i class="fas fa-user"></i> Trang cá nhân</a></li>
            <li><a href="settings.html"><i class="fas fa-cog"></i> Cài đặt tài khoản</a></li>
            <li><a href="#"><i class="fas fa-question-circle"></i> Trợ giúp</a></li>
            <li class="divider"></li>
            <li><a href="../login.html" class="logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
          </ul>
        `;
        
        document.body.appendChild(menu);
        
        // Vị trí menu
        const buttonRect = userDropdown.getBoundingClientRect();
        menu.style.top = buttonRect.bottom + 10 + 'px';
        menu.style.left = (buttonRect.left - 150) + 'px';
        
        // Đóng menu khi click ra ngoài
        document.addEventListener('click', function closeMenu(e) {
          if (!menu.contains(e.target) && e.target !== userDropdown) {
            menu.classList.remove('show');
            menu.classList.add('hide');
            
            setTimeout(() => {
              menu.remove();
            }, 300);
            
            document.removeEventListener('click', closeMenu);
          }
        });
        
        // Thêm css cho menu
        if (!document.getElementById('userMenuStyles')) {
          const style = document.createElement('style');
          style.id = 'userMenuStyles';
          style.textContent = `
            .user-menu-dropdown {
              position: fixed;
              width: 200px;
              background: white;
              border-radius: 0.5rem;
              box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
              z-index: 999;
              border: 1px solid var(--dashboard-border);
              overflow: hidden;
              opacity: 0;
              transform: translateY(10px);
              visibility: hidden;
              transition: opacity 0.3s ease, transform 0.3s ease, visibility 0.3s;
            }
            
            .user-menu-dropdown.show {
              opacity: 1;
              transform: translateY(0);
              visibility: visible;
            }
            
            .user-menu-dropdown.hide {
              opacity: 0;
              transform: translateY(10px);
              visibility: hidden;
            }
            
            .user-menu-list {
              list-style: none;
              padding: 0.5rem 0;
              margin: 0;
            }
            
            .user-menu-list li a {
              display: flex;
              align-items: center;
              padding: 0.75rem 1rem;
              color: var(--gray-700);
              text-decoration: none;
              font-size: 0.875rem;
              transition: background 0.3s ease;
            }
            
            .user-menu-list li a:hover {
              background: var(--gray-100);
              color: var(--gray-900);
            }
            
            .user-menu-list li a i {
              margin-right: 0.5rem;
              width: 1.25rem;
              text-align: center;
            }
            
            .user-menu-list li.divider {
              height: 1px;
              background: var(--dashboard-border);
              margin: 0.5rem 0;
            }
            
            .user-menu-list li a.logout {
              color: var(--danger);
            }
            
            .user-menu-list li a.logout:hover {
              background: var(--danger-light);
            }
          `;
          
          document.head.appendChild(style);
        }
      }
    });
  }
}

/**
 * Xử lý các tương tác với tài liệu
 */
function initDocumentActions() {
  // Nút thích
  const favoriteBtns = document.querySelectorAll('.document-action-btn.favorite');
  favoriteBtns.forEach(btn => {
    btn.addEventListener('click', function() {
      this.classList.toggle('active');
      const icon = this.querySelector('i');
      
      if (this.classList.contains('active')) {
        icon.className = 'fas fa-heart';
      } else {
        icon.className = 'far fa-heart';
      }
    });
  });
  
  // Nút đánh dấu
  const bookmarkBtns = document.querySelectorAll('.document-action-btn.bookmark');
  bookmarkBtns.forEach(btn => {
    btn.addEventListener('click', function() {
      this.classList.toggle('active');
      const icon = this.querySelector('i');
      
      if (this.classList.contains('active')) {
        icon.className = 'fas fa-bookmark';
      } else {
        icon.className = 'far fa-bookmark';
      }
    });
  });
}
