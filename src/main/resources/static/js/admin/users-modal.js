// Modal functionality for Users page
(function() {
    'use strict';

    // Global function to open add user modal
    window.openAddUserModal = function() {
        console.log('openAddUserModal called');
        const modal = document.getElementById('addUserModal');
        if (modal) {
            // Reset form fields
            const nameInput = document.getElementById('userName');
            const emailInput = document.getElementById('userEmail');
            const usernameInput = document.getElementById('userUsername');
            const passwordInput = document.getElementById('userPassword');
            const roleSelect = document.getElementById('userRole');
            const statusSelect = document.getElementById('userStatus');
            
            if (nameInput) nameInput.value = '';
            if (emailInput) emailInput.value = '';
            if (usernameInput) usernameInput.value = '';
            if (passwordInput) passwordInput.value = '';
            if (roleSelect) roleSelect.value = 'user';
            if (statusSelect) statusSelect.value = 'active';
            
            // Set modal title
            const modalTitle = modal.querySelector('.modal-header h3');
            if (modalTitle) modalTitle.textContent = 'Thêm người dùng mới';
            
            // Show modal by adding 'open' class and setting display
            modal.classList.add('open');
            modal.style.display = 'flex';
            modal.style.visibility = 'visible';
            modal.style.opacity = '1';
            console.log('Modal shown with open class and inline styles');
        } else {
            console.error('Modal not found');
        }
    };
    
    // Function to close modal
    window.closeAddUserModal = function() {
        const modal = document.getElementById('addUserModal');
        if (modal) {
            modal.classList.remove('open');
            document.body.style.overflow = 'auto';
            console.log('Modal closed');
        } else {
            console.error('Modal not found');
        }
    };

    // User Detail Modal functionality
    function initializeUserDetailModal() {
        const detailModal = document.getElementById('userDetailModal');
        const detailOverlay = detailModal?.querySelector('.modal-overlay');
        const detailCloseBtn = detailModal?.querySelector('.close-modal');
        const tabButtons = document.querySelectorAll('.user-tab-btn');
        const tabContents = document.querySelectorAll('.tab-content');
        
        // Tab switching
        tabButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const target = this.getAttribute('data-target');
                
                // Remove active class from all tabs and contents
                tabButtons.forEach(b => b.classList.remove('active'));
                tabContents.forEach(c => c.classList.remove('active'));
                
                // Add active class to clicked tab and corresponding content
                this.classList.add('active');
                const targetContent = document.getElementById(target);
                if (targetContent) {
                    targetContent.classList.add('active');
                }
            });
        });
        
        // Close modal events
        detailCloseBtn?.addEventListener('click', closeUserDetailModal);
        detailOverlay?.addEventListener('click', closeUserDetailModal);
        
        // Modal footer buttons
        const editUserBtn = document.getElementById('editUserFromDetail');
        const deleteUserBtn = document.getElementById('deleteUserFromDetail');
        const sendEmailBtn = document.getElementById('sendEmailFromDetail');
        
        editUserBtn?.addEventListener('click', function() {
            console.log('Edit user clicked');
            // TODO: Implement edit user functionality
            if (typeof showToast === 'function') {
                showToast('Chức năng chỉnh sửa sẽ được mở trong modal riêng', 'info');
            }
        });
        
        deleteUserBtn?.addEventListener('click', function() {
            console.log('Delete user clicked');
            if (typeof showConfirm === 'function') {
                showConfirm('Bạn có chắc chắn muốn xóa người dùng này?', function() {
                    if (typeof showToast === 'function') {
                        showToast('Đã xóa người dùng', 'success');
                    }
                    closeUserDetailModal();
                });
            } else {
                if (confirm('Bạn có chắc chắn muốn xóa người dùng này?')) {
                    if (typeof showToast === 'function') {
                        showToast('Đã xóa người dùng', 'success');
                    }
                    closeUserDetailModal();
                }
            }
        });
        
        sendEmailBtn?.addEventListener('click', function() {
            console.log('Send email clicked');
            // TODO: Implement send email functionality
            if (typeof showToast === 'function') {
                showToast('Chức năng gửi email sẽ được mở trong modal riêng', 'info');
            }
        });
        
        // Admin notes functionality
        const saveNotesBtn = document.getElementById('saveAdminNotesBtn');
        const adminNotesTextarea = document.getElementById('adminNotesTextarea');
        
        saveNotesBtn?.addEventListener('click', function() {
            if (!adminNotesTextarea) return;
            const notes = adminNotesTextarea.value.trim();
            const username = document.getElementById('detailUserUsername')?.textContent || '';
            if (!username) {
                if (typeof showToast === 'function') {
                    showToast('Không xác định được người dùng!', 'error');
                }
                return;
            }
            localStorage.setItem('userNotes_' + username, notes);
            if (typeof showToast === 'function') {
                showToast('Đã lưu ghi chú của Admin!', 'success');
            }
        });
        
        // ESC key to close modal
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && detailModal?.classList.contains('open')) {
                closeUserDetailModal();
            }
        });
    }
    
    function closeUserDetailModal() {
        const detailModal = document.getElementById('userDetailModal');
        if (detailModal) {
            detailModal.classList.remove('open');
            detailModal.style.display = 'none';
            document.body.style.overflow = '';
        }
    }
    
    // Global function to open user detail modal
    window.openUserDetailModal = function(row) {
        const detailModal = document.getElementById('userDetailModal');
        if (!detailModal || !row) return;
        
        // Get user data from row
        const userName = row.querySelector('.user-name')?.textContent.trim() || '';
        const userUsername = row.querySelector('.user-username')?.textContent.replace('@', '').trim() || '';
        const userEmail = row.cells[2]?.textContent.trim() || '';
        const userRole = row.querySelector('.role-badge')?.textContent.trim() || '';
        const userStatus = row.cells[7]?.textContent.trim() || '';
        
        // Fill modal with user data
        const detailUserName = document.getElementById('detailUserName');
        const detailUserUsername = document.getElementById('detailUserUsername');
        const detailUserEmail = document.getElementById('detailUserEmail');
        const detailUserRole = document.getElementById('detailUserRole');
        const detailUserStatus = document.getElementById('detailUserStatus');
        const adminNotesTextarea = document.getElementById('adminNotesTextarea');
        
        if (detailUserName) detailUserName.textContent = userName;
        if (detailUserUsername) detailUserUsername.textContent = userUsername;
        if (detailUserEmail) detailUserEmail.textContent = userEmail;
        if (detailUserRole) detailUserRole.textContent = userRole;
        if (detailUserStatus) detailUserStatus.textContent = userStatus;
        
        // Load admin notes from localStorage
        const savedNotes = localStorage.getItem('userNotes_' + userUsername) || '';
        if (adminNotesTextarea) {
            adminNotesTextarea.value = savedNotes;
        }
        
        // Open modal
        detailModal.classList.add('open');
        detailModal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        
        // Initialize modal functionality
        initializeUserDetailModal();
    };

    // Initialize modal functionality when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Users modal JS loaded');
        
        // Add event listeners for modal buttons
        const addUserBtn = document.querySelector('.add-new-btn');
        const modalOverlay = document.querySelector('.modal-overlay');
        const modalCloseBtn = document.querySelector('.modal-close');
        const cancelBtn = document.querySelector('.cancel-btn');

        if (addUserBtn) {
            addUserBtn.addEventListener('click', function() {
                openAddUserModal();
            });
        }

        if (modalOverlay) {
            modalOverlay.addEventListener('click', function() {
                closeAddUserModal();
            });
        }

        if (modalCloseBtn) {
            modalCloseBtn.addEventListener('click', function() {
                closeAddUserModal();
            });
        }

        if (cancelBtn) {
            cancelBtn.addEventListener('click', function() {
                closeAddUserModal();
            });
        }
        
        // Initialize user detail modal
        initializeUserDetailModal();
    });

})(); 