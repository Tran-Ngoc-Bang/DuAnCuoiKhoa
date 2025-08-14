
    document.addEventListener('DOMContentLoaded', function() {
        // Admin dropdown toggle
        const adminProfile = document.querySelector('.admin-profile');
        if(adminProfile) {
            adminProfile.addEventListener('click', function() {
                this.classList.toggle('active');
            });
        }
        
        // Select all checkboxes
        const selectAll = document.getElementById('selectAll');
        const checkboxes = document.querySelectorAll('.checkbox-wrapper input[type="checkbox"]:not(#selectAll)');
        
        if(selectAll) {
            selectAll.addEventListener('change', function() {
                checkboxes.forEach(checkbox => {
                    checkbox.checked = this.checked;
                });
            });
            
            checkboxes.forEach(checkbox => {
                checkbox.addEventListener('change', function() {
                    const allChecked = [...checkboxes].every(c => c.checked);
                    const someChecked = [...checkboxes].some(c => c.checked);
                    
                    selectAll.checked = allChecked;
                    selectAll.indeterminate = someChecked && !allChecked;
                });
            });
        }
        
        // Modal functionality
        const addTagBtn = document.getElementById('addTagBtn');
        const addTagModal = document.getElementById('addTagModal');
        const modalOverlay = document.querySelector('.modal-overlay');
        const modalCloseBtn = document.querySelector('.modal-close');
        const cancelBtn = document.querySelector('.cancel-btn');
        
        function openModal() {
            if(addTagModal) {
                addTagModal.classList.add('open');
                document.body.style.overflow = 'hidden';
            }
        }
        
        function closeModal() {
            if(addTagModal) {
                addTagModal.classList.remove('open');
                document.body.style.overflow = '';
            }
        }
        
        if(addTagBtn) {
            addTagBtn.addEventListener('click', openModal);
        }
        
        if(modalOverlay) {
            modalOverlay.addEventListener('click', closeModal);
        }
        
        if(modalCloseBtn) {
            modalCloseBtn.addEventListener('click', closeModal);
        }
        
        if(cancelBtn) {
            cancelBtn.addEventListener('click', closeModal);
        }
        
        // Tag color picker
        const tagColor = document.getElementById('tagColor');
        const colorSquare = document.querySelector('.color-square');
        const colorHex = document.querySelector('.color-hex');
        const tagPreviewLabel = document.querySelector('.tag-preview-label');
        const tagNameInput = document.getElementById('tagName');
        
        if(tagColor && colorSquare && colorHex && tagPreviewLabel) {
            tagColor.addEventListener('input', function() {
                const selectedColor = this.value;
                colorSquare.style.backgroundColor = selectedColor;
                colorHex.textContent = selectedColor;
                
                // Update tag preview
                tagPreviewLabel.style.backgroundColor = hexToRgba(selectedColor, 0.1);
                tagPreviewLabel.style.color = selectedColor;
            });
        }
        
        if(tagNameInput && tagPreviewLabel) {
            tagNameInput.addEventListener('input', function() {
                const tagName = this.value.trim() || 'Tag Name';
                tagPreviewLabel.textContent = tagName;
                
                // Auto-generate slug
                const tagSlug = document.getElementById('tagSlug');
                if(tagSlug) {
                    tagSlug.value = generateSlug(tagName);
                }
            });
        }
        
        // Helper functions
        function generateSlug(text) {
            return text.toString().toLowerCase()
                .replace(/\s+/g, '-')           // Replace spaces with -
                .replace(/[^\w\-]+/g, '')       // Remove all non-word chars
                .replace(/\-\-+/g, '-')         // Replace multiple - with single -
                .replace(/^-+/, '')             // Trim - from start of text
                .replace(/-+$/, '');            // Trim - from end of text
        }
        
        function hexToRgba(hex, alpha = 1) {
            const [r, g, b] = hex.match(/\w\w/g).map(x => parseInt(x, 16));
            return `rgba(${r}, ${g}, ${b}, ${alpha})`;
        }
        
        // Pagination
        const pageButtons = document.querySelectorAll('.page-btn');
        const prevButton = document.querySelector('.prev-btn');
        const nextButton = document.querySelector('.next-btn');
        
        pageButtons.forEach(button => {
            button.addEventListener('click', function() {
                pageButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');
                
                // Update prev/next button states
                if(this.textContent === '1') {
                    prevButton.disabled = true;
                } else {
                    prevButton.disabled = false;
                }
                
                if(this.textContent === '20') {
                    nextButton.disabled = true;
                } else {
                    nextButton.disabled = false;
                }
            });
        });
        
        // Export functionality
        const exportBtn = document.getElementById('exportTagsBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', exportTagsToCSV);
        }
    });

    // Export Tags to CSV - Fetch all data from server
    async function exportTagsToCSV() {
        console.log('Export tags button clicked'); // Debug
        
        try {
            // Removed loading notification as requested
            
            // Fetch all tags from server (without pagination)
            const response = await fetch('/admin/tags?size=1000&export=true');
            
            if (!response.ok) {
                throw new Error('Không thể tải dữ liệu từ server');
            }
            
            // Parse HTML response to extract data
            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            const data = [];
            
            // Header
            data.push([
                'STT', 'Tên tag', 'Số tài liệu', 'Ngày tạo', 'Trạng thái'
            ]);
            
            // Get all rows from the fetched HTML
            const rows = doc.querySelectorAll('tbody tr');
            console.log('Found total rows:', rows.length); // Debug
            
            rows.forEach((row, index) => {
                const cells = row.querySelectorAll('td');
                
                if (cells.length >= 6) {
                    const stt = (index + 1).toString(); // Sequential number
                    const tagName = cells[2]?.querySelector('.tag-label')?.textContent?.trim() || '';
                    const docCount = cells[3]?.textContent?.trim() || '0';
                    const createdDate = cells[4]?.textContent?.trim() || '';
                    const status = cells[5]?.querySelector('.status-badge')?.textContent?.trim() || '';
                    
                    const rowData = [
                        stt,
                        tagName,
                        docCount,
                        createdDate,
                        status
                    ];
                    
                    data.push(rowData);
                }
            });
            
            console.log('Total data rows:', data.length); // Debug
            
            if (data.length <= 1) {
                showNotification('Không có dữ liệu để xuất!', 'error');
                return;
            }
            
            // Create CSV content
            const csvContent = data.map(row => 
                row.map(cell => `"${cell.toString().replace(/"/g, '""')}"`).join(',')
            ).join('\n');
            
            // Download file
            const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            const url = URL.createObjectURL(blob);
            link.setAttribute('href', url);
            link.setAttribute('download', `tags_export_${new Date().toISOString().split('T')[0]}.csv`);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            showNotification(`Đã xuất ${data.length - 1} tag thành công!`, 'success');
            
        } catch (error) {
            console.error('Export error:', error);
            showNotification('Lỗi khi xuất dữ liệu: ' + error.message, 'error');
        }
    }

    // Helper function for notifications - with inline CSS for Tags page
    function showNotification(message, type = 'info') {
        // Tạo toast notification với inline CSS
        const toast = document.createElement('div');
        toast.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show`;
        
        // Inline CSS để đảm bảo hiển thị đúng
        const bgColor = type === 'success' ? '#d1e7dd' : '#f8d7da';
        const borderColor = type === 'success' ? '#badbcc' : '#f5c2c7';
        const textColor = type === 'success' ? '#0f5132' : '#842029';
        
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            padding: 12px 16px;
            margin-bottom: 1rem;
            border: 1px solid ${borderColor};
            border-radius: 8px;
            background-color: ${bgColor};
            color: ${textColor};
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            font-size: 14px;
            line-height: 1.5;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            display: flex;
            align-items: center;
            gap: 8px;
            animation: slideIn 0.3s ease-out;
        `;
        
        // Thêm keyframe animation
        if (!document.getElementById('toast-animations')) {
            const style = document.createElement('style');
            style.id = 'toast-animations';
            style.textContent = `
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes slideOut {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
            `;
            document.head.appendChild(style);
        }
        
        toast.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}" 
               style="color: ${type === 'success' ? '#0f5132' : '#842029'}; margin-right: 8px;"></i>
            <span style="flex: 1;">${message}</span>
            <button type="button" 
                    style="background: none; border: none; font-size: 18px; color: ${textColor}; cursor: pointer; padding: 0; margin-left: 8px;"
                    onclick="this.parentElement.remove()">&times;</button>
        `;
        
        document.body.appendChild(toast);
        
        // Tự động xóa sau 3 giây với animation
        setTimeout(() => {
            if (toast.parentNode) {
                toast.style.animation = 'slideOut 0.3s ease-in';
                setTimeout(() => {
                    if (toast.parentNode) {
                        toast.parentNode.removeChild(toast);
                    }
                }, 300);
            }
        }, 3000);
    }

