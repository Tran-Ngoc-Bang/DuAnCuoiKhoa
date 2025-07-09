// Admin JS Functions

// Xử lý menu sidebar và submenu
document.addEventListener('DOMContentLoaded', function() {
    // Xử lý submenu
    const submenuItems = document.querySelectorAll('.admin-menu-item.has-submenu');
    
    submenuItems.forEach(item => {
        const link = item.querySelector('a');
        const submenu = item.querySelector('.submenu');
        
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Toggle submenu visibility
            item.classList.toggle('open');
            submenu.classList.toggle('visible');
            
            // Close other submenus when opening a new one
            submenuItems.forEach(other => {
                if (other !== item && other.classList.contains('open')) {
                    other.classList.remove('open');
                    other.querySelector('.submenu').classList.remove('visible');
                }
            });
        });
    });
    
    // Xử lý dark mode toggle
    // const darkModeToggle = document.getElementById('darkModeToggle');
    // if (darkModeToggle) {
    //     darkModeToggle.addEventListener('click', function() {
    //         document.body.classList.toggle('dark-mode');
    //         localStorage.setItem('darkMode', document.body.classList.contains('dark-mode'));
    //     });
        
    //     // Kiểm tra trạng thái dark mode từ localStorage
    //     if (localStorage.getItem('darkMode') === 'true') {
    //         document.body.classList.add('dark-mode');
    //     }
    // }
    
    // Thêm menu active cho trang hiện tại
    const currentPage = window.location.pathname.split("/").pop();
    const menuLinks = document.querySelectorAll('.admin-menu-item > a');
    
    menuLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPage) {
            link.parentElement.classList.add('active');
            
            // Nếu link này nằm trong submenu, mở submenu đó
            const parentSubmenuItem = link.closest('.submenu-item');
            if (parentSubmenuItem) {
                const parentMenuItem = parentSubmenuItem.closest('.admin-menu-item.has-submenu');
                if (parentMenuItem) {
                    parentMenuItem.classList.add('open');
                    const submenu = parentMenuItem.querySelector('.submenu');
                    if (submenu) {
                        submenu.classList.add('visible');
                    }
                }
            }
        }
    });
    
    // Highlight submenu item nếu đang ở trang đó
    const submenuLinks = document.querySelectorAll('.submenu-item > a');
    submenuLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPage) {
            link.parentElement.classList.add('active');
            
            // Mở menu cha
            const parentMenuItem = link.closest('.admin-menu-item.has-submenu');
            if (parentMenuItem) {
                parentMenuItem.classList.add('open');
                const submenu = parentMenuItem.querySelector('.submenu');
                if (submenu) {
                    submenu.classList.add('visible');
                }
            }
        }
    });
});

// Hàm hiển thị toast message
function showToast(message, type = 'info') {
    // Tạo container nếu chưa tồn tại
    let container = document.querySelector('.toast-container');
    
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    // Tạo toast
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    // Icon phù hợp với loại toast
    let icon = 'info-circle';
    if (type === 'success') icon = 'check-circle';
    if (type === 'warning') icon = 'exclamation-circle';
    if (type === 'error') icon = 'times-circle';
    
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas fa-${icon}"></i>
            <div class="toast-message">${message}</div>
        </div>
        <button class="toast-close"><i class="fas fa-times"></i></button>
    `;
    
    // Thêm vào container
    container.appendChild(toast);
    
    // Xử lý nút đóng
    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.addEventListener('click', function() {
        toast.classList.add('toast-hiding');
        setTimeout(() => {
            container.removeChild(toast);
        }, 300);
    });
    
    // Tự động ẩn sau 5s
    setTimeout(() => {
        if (toast.parentNode === container) {
            toast.classList.add('toast-hiding');
            setTimeout(() => {
                if (toast.parentNode === container) {
                    container.removeChild(toast);
                }
            }, 300);
        }
    }, 5000);
    
    // Hiển thị toast với hiệu ứng
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);
}
