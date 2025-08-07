document.addEventListener('DOMContentLoaded', function () {
    // Admin dropdown toggle
    const adminProfile = document.querySelector('.admin-profile');
    if (adminProfile) {
        adminProfile.addEventListener('click', function () {
            this.classList.toggle('active');
        });
    }

    // Comments Tabs
    const tabButtons = document.querySelectorAll('.tab-button');

    tabButtons.forEach(button => {
        button.addEventListener('click', function () {
            tabButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            const tab = this.getAttribute('data-tab');
            // Filter comments based on tab
            filterComments(tab);
        });
    });

    function filterComments(tab) {
        const commentItems = document.querySelectorAll('.comment-item');

        commentItems.forEach(item => {
            switch (tab) {
                case 'all':
                    item.style.display = 'block';
                    break;
                case 'recent':
                    // For demo purposes, let's just show all items for now
                    item.style.display = 'block';
                    break;
                case 'reported':
                    if (item.classList.contains('reported')) {
                        item.style.display = 'block';
                    } else {
                        item.style.display = 'none';
                    }
                    break;
                case 'hidden':
                    if (item.classList.contains('hidden')) {
                        item.style.display = 'block';
                    } else {
                        item.style.display = 'none';
                    }
                    break;
                default:
                    item.style.display = 'block';
            }
        });
    }

    // Toggle Replies
    const toggleReplies = document.querySelectorAll('.toggle-replies');

    toggleReplies.forEach(toggle => {
        toggle.addEventListener('click', function () {
            const commentId = this.getAttribute('data-id');
            const repliesContainer = document.getElementById(`${commentId}-replies`);

            if (repliesContainer) {
                if (repliesContainer.style.display === 'none') {
                    repliesContainer.style.display = 'block';
                    this.textContent = 'Ẩn phản hồi';
                } else {
                    repliesContainer.style.display = 'none';
                    this.textContent = 'Xem phản hồi';
                }
            }
        });
    });

    // Report Actions
    const reportApprove = document.querySelectorAll('.report-approve');
    const reportReject = document.querySelectorAll('.report-reject');

    reportApprove.forEach(btn => {
        btn.addEventListener('click', function () {
            const commentItem = this.closest('.comment-item');

            // Simulating action
            alert('Đã chấp nhận báo cáo. Bình luận đã được ẩn.');

            if (commentItem) {
                commentItem.classList.remove('reported');
                commentItem.classList.add('hidden');

                const statusIndicator = commentItem.querySelector('.comment-status');
                if (statusIndicator) {
                    statusIndicator.className = 'comment-status hidden';
                    statusIndicator.innerHTML = '<i class="fas fa-eye-slash"></i> Đã ẩn';
                }

                // Replace comment actions
                const commentActions = commentItem.querySelector('.comment-actions');
                if (commentActions) {
                    commentActions.innerHTML = `
                            <div class="comment-buttons">
                                <button class="action-btn show-btn" title="Hiển thị bình luận"><i class="fas fa-eye"></i></button>
                                <button class="action-btn" title="Xóa bình luận"><i class="fas fa-trash"></i></button>
                            </div>
                        `;
                }
            }
        });
    });

    reportReject.forEach(btn => {
        btn.addEventListener('click', function () {
            const commentItem = this.closest('.comment-item');

            // Simulating action
            alert('Đã từ chối báo cáo. Bình luận vẫn được hiển thị.');

            if (commentItem) {
                commentItem.classList.remove('reported');

                const statusIndicator = commentItem.querySelector('.comment-status');
                if (statusIndicator) {
                    statusIndicator.className = 'comment-status visible';
                    statusIndicator.innerHTML = '<i class="fas fa-eye"></i> Hiển thị';
                }

                // Remove report info
                const reportInfo = commentItem.querySelector('.report-info');
                if (reportInfo) {
                    reportInfo.remove();
                }

                // Replace comment actions
                const commentActions = commentItem.querySelector('.comment-actions');
                if (commentActions) {
                    commentActions.innerHTML = `
                            <div class="comment-reply">
                                <span class="reply-count"><i class="fas fa-reply"></i> 0 phản hồi</span>
                            </div>
                            <div class="comment-buttons">
                                <button class="action-btn" title="Ẩn bình luận"><i class="fas fa-eye-slash"></i></button>
                                <button class="action-btn" title="Xóa bình luận"><i class="fas fa-trash"></i></button>
                            </div>
                        `;
                }
            }
        });
    });

    // Hide/Show Comment Actions
    const actionButtons = document.querySelectorAll('.action-btn');
    const commentActionModal = document.getElementById('commentActionModal');
    const modalOverlay = document.querySelector('.modal-overlay');
    const modalCloseBtn = document.querySelector('.modal-close');
    const cancelBtn = document.querySelector('.cancel-btn');
    const saveBtn = document.querySelector('.save-btn');

    function openModal() {
        if (commentActionModal) {
            commentActionModal.classList.add('open');
            document.body.style.overflow = 'hidden';
        }
    }

    function closeModal() {
        if (commentActionModal) {
            commentActionModal.classList.remove('open');
            document.body.style.overflow = '';
        }
    }

    actionButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const action = this.getAttribute('title');
            const commentItem = this.closest('.comment-item') || this.closest('.reply-item');
            const commentType = commentItem.classList.contains('reply-item') ? 'phản hồi' : 'bình luận';

            if (action === 'Ẩn bình luận' || action === 'Ẩn phản hồi') {
                document.getElementById('actionType').value = 'hide';
                document.getElementById('actionType').disabled = true;
                openModal();
            } else if (action === 'Hiển thị bình luận' || action === 'Hiển thị phản hồi') {
                if (confirm(`Bạn có chắc chắn muốn hiển thị lại ${commentType} này?`)) {
                    alert(`Đã hiển thị lại ${commentType} thành công!`);

                    // Simulate showing comment
                    if (commentItem) {
                        commentItem.classList.remove('hidden');

                        const statusIndicator = commentItem.querySelector('.comment-status');
                        if (statusIndicator) {
                            statusIndicator.className = 'comment-status visible';
                            statusIndicator.innerHTML = '<i class="fas fa-eye"></i> Hiển thị';
                        }
                    }
                }
            } else if (action === 'Xóa bình luận' || action === 'Xóa phản hồi') {
                document.getElementById('actionType').value = 'delete';
                document.getElementById('actionType').disabled = true;
                openModal();
            }
        });
    });

    if (modalOverlay) {
        modalOverlay.addEventListener('click', closeModal);
    }

    if (modalCloseBtn) {
        modalCloseBtn.addEventListener('click', closeModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', function () {
            const action = document.getElementById('actionType').value;
            const reason = document.getElementById('actionReason').value;

            // Simulate action
            alert(`Hành động đã được thực hiện thành công!`);
            closeModal();
        });
    }

    // Toggle notify user
    const notifyUserToggle = document.getElementById('notifyUser');
    const messageGroup = document.querySelector('.message-group');

    if (notifyUserToggle && messageGroup) {
        notifyUserToggle.addEventListener('change', function () {
            if (this.checked) {
                messageGroup.style.display = 'block';
            } else {
                messageGroup.style.display = 'none';
            }
        });
    }

    // Pagination
    const pageButtons = document.querySelectorAll('.page-btn');
    const prevButton = document.querySelector('.prev-btn');
    const nextButton = document.querySelector('.next-btn');

    pageButtons.forEach(button => {
        button.addEventListener('click', function () {
            pageButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            // Update prev/next button states
            if (this.textContent === '1') {
                prevButton.disabled = true;
            } else {
                prevButton.disabled = false;
            }

            if (this.textContent === '128') {
                nextButton.disabled = true;
            } else {
                nextButton.disabled = false;
            }

            // Scroll to top of comments section
            document.querySelector('.comments-list').scrollIntoView({ behavior: 'smooth' });
        });
    });
});
