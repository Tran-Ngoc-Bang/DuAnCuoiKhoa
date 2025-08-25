/**
 * Document Create Form JavaScript
 * Handles form functionality for admin document creation
 */

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("documentForm");
    const documentFile = document.getElementById("documentFile");
    const fileInfo = document.getElementById("fileInfo");

    // Tab switching functionality
    const tabBtns = document.querySelectorAll(".tab-btn");
    const tabPanels = document.querySelectorAll(".tab-panel");

    tabBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
            const targetTab = this.getAttribute("data-tab");

            // Remove active class from all tabs and panels
            tabBtns.forEach((b) => b.classList.remove("active"));
            tabPanels.forEach((p) => p.classList.remove("active"));

            // Add active class to clicked tab and corresponding panel
            this.classList.add("active");
            document
                .getElementById(
                    "tab" + targetTab.charAt(0).toUpperCase() + targetTab.slice(1)
                )
                .classList.add("active");
        });
    });

    // File upload handling
    if (documentFile) {
        documentFile.addEventListener("change", function () {
            const file = this.files[0];
            if (file) {
                const fileName = file.name;
                const fileSize = (file.size / 1024 / 1024).toFixed(2) + " MB";

                fileInfo.querySelector(".file-name").textContent = fileName;
                fileInfo.querySelector(".file-size").textContent = fileSize;
                fileInfo.style.display = "flex";

                // Update file icon based on extension
                const extension = fileName.split(".").pop().toLowerCase();
                const icon = fileInfo.querySelector("i");
                icon.className = "fas fa-file";

                switch (extension) {
                    case "pdf":
                        icon.className = "fas fa-file-pdf";
                        break;
                    case "doc":
                    case "docx":
                        icon.className = "fas fa-file-word";
                        break;
                    case "ppt":
                    case "pptx":
                        icon.className = "fas fa-file-powerpoint";
                        break;
                    case "xls":
                    case "xlsx":
                        icon.className = "fas fa-file-excel";
                        break;
                }
            } else {
                fileInfo.style.display = "none";
            }
        });

        // Remove file button
        const removeBtn = fileInfo.querySelector(".btn-remove");
        if (removeBtn) {
            removeBtn.addEventListener("click", function () {
                documentFile.value = "";
                fileInfo.style.display = "none";
            });
        }
    }

    // Form validation
    if (form) {
        form.addEventListener("submit", function (e) {
            let isValid = true;

            // Validate title
            const title = document.getElementById("documentTitle");
            const titleError = document.getElementById("titleError");
            if (title && !title.value.trim()) {
                if (titleError) titleError.style.display = "block";
                title.classList.add("is-invalid");
                isValid = false;
            } else if (title && titleError) {
                titleError.style.display = "none";
                title.classList.remove("is-invalid");
            }

            // Validate file
            const fileError = document.getElementById("fileError");
            if (!documentFile || !documentFile.files || documentFile.files.length === 0) {
                if (fileError) fileError.style.display = "block";
                if (documentFile) documentFile.classList.add("is-invalid");
                isValid = false;
            } else if (fileError) {
                fileError.style.display = "none";
                if (documentFile) documentFile.classList.remove("is-invalid");
            }

            if (!isValid) {
                e.preventDefault();
                // Switch to basic tab if validation fails
                const basicTab = document.querySelector('[data-tab="basic"]');
                if (basicTab) basicTab.click();
            }
        });
    }

    // Drag and drop for file upload
    const fileUpload = document.querySelector(".file-upload");
    if (fileUpload) {
        ["dragenter", "dragover", "dragleave", "drop"].forEach(
            (eventName) => {
                fileUpload.addEventListener(eventName, preventDefaults, false);
            }
        );

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        ["dragenter", "dragover"].forEach((eventName) => {
            fileUpload.addEventListener(eventName, highlight, false);
        });

        ["dragleave", "drop"].forEach((eventName) => {
            fileUpload.addEventListener(eventName, unhighlight, false);
        });

        function highlight(e) {
            fileUpload.classList.add("drag-over");
        }

        function unhighlight(e) {
            fileUpload.classList.remove("drag-over");
        }

        fileUpload.addEventListener("drop", handleDrop, false);

        function handleDrop(e) {
            const dt = e.dataTransfer;
            const files = dt.files;

            if (files.length > 0) {
                documentFile.files = files;
                documentFile.dispatchEvent(new Event("change"));
            }
        }
    }
});

// Category API functions
async function loadSubcategories(parentId, targetSelectId) {
    try {
        const response = await fetch(`/admin/documents/categories/${parentId}/subcategories`);
        const subcategories = await response.json();
        
        const targetSelect = document.getElementById(targetSelectId);
        if (targetSelect) {
            targetSelect.innerHTML = '<option value="">Chọn danh mục con...</option>';
            
            subcategories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.id;
                option.textContent = `📁 ${category.name}`;
                option.setAttribute('data-subcategories', JSON.stringify(category.subcategories || []));
                targetSelect.appendChild(option);
            });
            
            // Show the target select
            const targetDiv = document.getElementById(targetSelectId.replace('Select', ''));
            if (targetDiv) targetDiv.style.display = 'block';
        }
        
    } catch (error) {
        console.error('Error loading subcategories:', error);
    }
}

function onLevelChange(level) {
    const currentSelect = document.getElementById(`level${level}Select`);
    if (!currentSelect) return;
    
    const selectedValue = currentSelect.value;
    
    // Hide and reset subsequent levels
    for (let i = level + 1; i <= 3; i++) {
        const levelDiv = document.getElementById(`level${i}`);
        const levelSelect = document.getElementById(`level${i}Select`);
        if (levelDiv) levelDiv.style.display = 'none';
        if (levelSelect) levelSelect.innerHTML = '<option value="">Chọn...</option>';
    }
    
    // Update category IDs string with selected path
    updateCategoryIdsString();
    
    // Update final parent ID (for backward compatibility)
    const finalParentId = document.getElementById('finalParentId');
    if (finalParentId) finalParentId.value = selectedValue || 0;
    
    // Load subcategories if a category is selected
    if (selectedValue && selectedValue !== '0') {
        loadSubcategories(selectedValue, `level${level + 1}Select`);
    }
    
    updateBreadcrumb();
}

function updateCategoryIdsString() {
    const categoryIds = [];
    
    // Collect all selected category IDs from level selects
    for (let i = 1; i <= 3; i++) {
        const select = document.getElementById(`level${i}Select`);
        if (select && select.value && select.value !== '0' && select.value !== '') {
            categoryIds.push(select.value);
        }
    }
    
    // Update the hidden input
    const categoryIdsStringInput = document.getElementById('categoryIdsString');
    if (categoryIdsStringInput) {
        categoryIdsStringInput.value = categoryIds.join(',');
        console.log('Updated categoryIdsString:', categoryIdsStringInput.value);
    }
}

function updateBreadcrumb() {
    const breadcrumb = document.getElementById('categoryBreadcrumb');
    const breadcrumbPath = document.getElementById('breadcrumbPath');
    
    if (!breadcrumb || !breadcrumbPath) return;
    
    let path = [];
    
    // Build path from selected values
    for (let i = 1; i <= 3; i++) {
        const select = document.getElementById(`level${i}Select`);
        if (select && select.value && select.value !== '0') {
            const selectedOption = select.options[select.selectedIndex];
            path.push(selectedOption.textContent);
        } else {
            break;
        }
    }
    
    if (path.length > 0) {
        breadcrumbPath.textContent = path.join(' → ');
        breadcrumb.style.display = 'block';
    } else {
        breadcrumb.style.display = 'none';
    }
}