
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
    });

