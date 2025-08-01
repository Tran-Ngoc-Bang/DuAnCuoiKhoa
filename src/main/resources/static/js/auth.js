/**
 * Authentication UI Helper cho Thymeleaf + Spring Security
 * Xử lý các tương tác UI đơn giản, không xử lý JWT
 */
document.addEventListener('DOMContentLoaded', function() {
    // Initialize password toggle functionality
    initPasswordToggle();
    
    // Initialize form validation
    initFormValidation();
    
    // Initialize mobile menu
    initMobileMenu();
    
    // Initialize dark mode toggle
    initDarkModeToggle();
});

/**
 * Toggle password visibility
 */
function initPasswordToggle() {
    const toggleButtons = document.querySelectorAll('.password-toggle');
    
    toggleButtons.forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');
            
            // Toggle input type
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
}

/**
 * Form validation
 */
function initFormValidation() {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            
            // Basic validation
            if (!username) {
                e.preventDefault();
                showError('Vui lòng nhập tên đăng nhập');
                return;
            }
            
            if (!password) {
                e.preventDefault();
                showError('Vui lòng nhập mật khẩu');
                return;
            }
            
            // Show loading state
            const submitBtn = loginForm.querySelector('.login-submit-btn');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
                submitBtn.disabled = true;
            }
        });
    }
}

/**
 * Mobile menu functionality
 */
function initMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const mobileMenu = document.getElementById('mobileMenu');
    
    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            mobileMenu.classList.toggle('active');
            this.classList.toggle('active');
        });
        
        // Close menu when clicking outside
        document.addEventListener('click', function(e) {
            if (!mobileMenuBtn.contains(e.target) && !mobileMenu.contains(e.target)) {
                mobileMenu.classList.remove('active');
                mobileMenuBtn.classList.remove('active');
            }
        });
    }
}

/**
 * Dark mode toggle
 */
function initDarkModeToggle() {
    const darkModeToggle = document.getElementById('darkModeToggle');
    const darkModeHandle = document.getElementById('darkModeHandle');
    
    if (darkModeToggle && darkModeHandle) {
        // Load saved preference
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-mode');
            darkModeHandle.classList.add('active');
        }
        
        darkModeToggle.addEventListener('click', function() {
            document.body.classList.toggle('dark-mode');
            darkModeHandle.classList.toggle('active');
            
            // Save preference
            const isDark = document.body.classList.contains('dark-mode');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');
        });
    }
}

/**
 * Show error message
 */
function showError(message) {
    const errorElement = document.querySelector('.auth-error');
    if (errorElement) {
        const errorMessage = errorElement.querySelector('.error-message');
        if (errorMessage) {
            errorMessage.textContent = message;
            errorElement.style.display = 'flex';
            
            // Hide after 5 seconds
            setTimeout(() => {
                errorElement.style.display = 'none';
            }, 5000);
        }
    }
}

/**
 * Show success message
 */
function showSuccess(message) {
    const successElement = document.querySelector('.auth-success');
    if (successElement) {
        const successMessage = successElement.querySelector('.success-message');
        if (successMessage) {
            successMessage.textContent = message;
            successElement.style.display = 'flex';
            
            // Hide after 5 seconds
            setTimeout(() => {
                successElement.style.display = 'none';
            }, 5000);
        }
    }
}

/**
 * Utility function to validate email format
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Logout function (for API calls if needed)
 */
async function logout() {
    try {
        const response = await fetch('/logout', {
            method: 'POST',
            credentials: 'include'
        });
        
        if (response.ok) {
            window.location.href = '/login?logout';
        }
    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = '/login';
    }
}