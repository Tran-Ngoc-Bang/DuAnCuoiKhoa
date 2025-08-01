/**
 * JWT Authentication JavaScript
 */

class JWTAuth {
    constructor() {
        this.baseUrl = '/api/auth';
        this.tokenKey = 'jwt_token';
    }

    /**
     * Đăng nhập và lưu token
     */
    async login(username, password) {
        try {
            const response = await fetch(`${this.baseUrl}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();
            
            if (data.success) {
                // Lưu token vào localStorage
                localStorage.setItem(this.tokenKey, data.token);
                localStorage.setItem('user_info', JSON.stringify({
                    username: data.username,
                    fullName: data.fullName,
                    role: data.role
                }));
                
                return { success: true, data };
            } else {
                return { success: false, message: data.message };
            }
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, message: 'Lỗi kết nối' };
        }
    }

    /**
     * Đăng xuất và xóa token
     */
    async logout() {
        try {
            const token = this.getToken();
            if (token) {
                await fetch(`${this.baseUrl}/logout`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Xóa token và user info
            localStorage.removeItem(this.tokenKey);
            localStorage.removeItem('user_info');
            window.location.href = '/login';
        }
    }

    /**
     * Lấy token từ localStorage
     */
    getToken() {
        return localStorage.getItem(this.tokenKey);
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    async validateToken() {
        try {
            const token = this.getToken();
            if (!token) {
                return false;
            }

            const response = await fetch(`${this.baseUrl}/validate`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            return response.ok;
        } catch (error) {
            console.error('Token validation error:', error);
            return false;
        }
    }

    /**
     * Lấy thông tin user
     */
    getUserInfo() {
        const userInfo = localStorage.getItem('user_info');
        return userInfo ? JSON.parse(userInfo) : null;
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    isAuthenticated() {
        return !!this.getToken();
    }

    /**
     * Tạo headers với token cho API calls
     */
    getAuthHeaders() {
        const token = this.getToken();
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        };
    }
}

// Khởi tạo JWT Auth instance
const jwtAuth = new JWTAuth();

// Login form handler
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            const result = await jwtAuth.login(username, password);
            
            if (result.success) {
                // Chuyển hướng đến trang admin
                window.location.href = '/admin/coin-packages';
            } else {
                // Hiển thị lỗi
                alert(result.message);
            }
        });
    }

    // Kiểm tra authentication khi load trang
    if (window.location.pathname.startsWith('/admin')) {
        jwtAuth.validateToken().then(isValid => {
            if (!isValid) {
                window.location.href = '/login';
            }
        });
    }
}); 