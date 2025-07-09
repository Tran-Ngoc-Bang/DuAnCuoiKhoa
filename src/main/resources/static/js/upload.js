// Upload Document Functionality

// DOM Elements
const fileDropArea = document.getElementById('fileDropArea');
const fileInput = document.getElementById('fileInput');
const uploadForm = document.getElementById('uploadForm');

// Initialize upload functionality
document.addEventListener('DOMContentLoaded', function() {
  if (fileDropArea && fileInput) {
    initFileUpload();
  }
  
  if (uploadForm) {
    initFormSubmission();
  }
});

// Initialize file upload functionality
function initFileUpload() {
  // Click to select file
  fileDropArea.addEventListener('click', function(e) {
    if (e.target !== fileInput) {
      fileInput.click();
    }
  });
  
  // Handle file selection
  fileInput.addEventListener('change', function() {
    if (fileInput.files.length > 0) {
      updateFilePreview(fileInput.files[0]);
    }
  });
  
  // Handle drag and drop
  ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
    fileDropArea.addEventListener(eventName, preventDefaults, false);
  });
  
  function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
  }
  
  // Highlight drop area when file is dragged over it
  ['dragenter', 'dragover'].forEach(eventName => {
    fileDropArea.addEventListener(eventName, highlight, false);
  });
  
  ['dragleave', 'drop'].forEach(eventName => {
    fileDropArea.addEventListener(eventName, unhighlight, false);
  });
  
  function highlight() {
    fileDropArea.classList.add('active');
  }
  
  function unhighlight() {
    fileDropArea.classList.remove('active');
  }
  
  // Handle dropped files
  fileDropArea.addEventListener('drop', handleDrop, false);
  
  function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;
    
    if (files.length > 0) {
      fileInput.files = files;
      updateFilePreview(files[0]);
    }
  }
}

// Update the file drop area with selected file info
function updateFilePreview(file) {
  // Validate file type
  const acceptedTypes = ['.pdf', '.doc', '.docx', '.ppt', '.pptx'];
  const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
  
  if (!acceptedTypes.includes(fileExtension)) {
    showFileError('Loại tệp không được hỗ trợ. Vui lòng chọn tệp PDF, DOC, DOCX, PPT hoặc PPTX.');
    fileInput.value = '';
    return;
  }
  
  // Validate file size (max 50MB)
  if (file.size > 50 * 1024 * 1024) {
    showFileError('Kích thước tệp vượt quá giới hạn 50MB.');
    fileInput.value = '';
    return;
  }
  
  // Update file drop area with file info
  let icon;
  switch(fileExtension) {
    case '.pdf':
      icon = '<i class="fas fa-file-pdf"></i>';
      break;
    case '.doc':
    case '.docx':
      icon = '<i class="fas fa-file-word"></i>';
      break;
    case '.ppt':
    case '.pptx':
      icon = '<i class="fas fa-file-powerpoint"></i>';
      break;
    default:
      icon = '<i class="fas fa-file"></i>';
  }
  
  const fileSize = formatFileSize(file.size);
  
  fileDropArea.innerHTML = `
    ${icon}
    <p class="drop-text">${file.name}</p>
    <p class="file-types">${fileSize} - Nhấp để thay đổi tệp</p>
  `;
  
  fileDropArea.classList.add('has-file');
}

// Format file size
function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Show file error
function showFileError(message) {
  const errorElement = document.createElement('div');
  errorElement.className = 'upload-error';
  errorElement.innerHTML = `
    <i class="fas fa-exclamation-circle"></i>
    <span>${message}</span>
  `;
  
  // Remove any existing error message
  const existingError = fileDropArea.parentNode.querySelector('.upload-error');
  if (existingError) {
    existingError.remove();
  }
  
  // Insert error message after file drop area
  fileDropArea.parentNode.insertBefore(errorElement, fileDropArea.nextSibling);
  
  // Auto remove after 5 seconds
  setTimeout(() => {
    errorElement.classList.add('fade-out');
    setTimeout(() => {
      errorElement.remove();
    }, 300);
  }, 5000);
}

// Form submission
function initFormSubmission() {
  uploadForm.addEventListener('submit', function(e) {
    e.preventDefault();
    
    // Validate form
    const title = document.getElementById('title').value.trim();
    const category = document.getElementById('category').value;
    
    if (!title) {
      showFormError('Vui lòng nhập tiêu đề tài liệu.');
      return;
    }
    
    if (!category) {
      showFormError('Vui lòng chọn danh mục.');
      return;
    }
    
    if (!fileInput.files.length) {
      showFormError('Vui lòng chọn tệp tài liệu để tải lên.');
      return;
    }
    
    // Show success message
    showUploadSuccess();
    
    // In a real application, you would submit the form data to the server
    // For demo purposes, reset the form after 2 seconds
    setTimeout(() => {
      uploadForm.reset();
      fileDropArea.innerHTML = `
        <i class="fas fa-cloud-upload-alt"></i>
        <p class="drop-text">Kéo thả tệp vào đây hoặc nhấp để chọn</p>
        <p class="file-types">Hỗ trợ: PDF, DOC, DOCX, PPT, PPTX (tối đa 50MB)</p>
      `;
      fileDropArea.classList.remove('has-file');
    }, 2000);
  });
}

// Show form error
function showFormError(message) {
  const errorElement = document.createElement('div');
  errorElement.className = 'form-error';
  errorElement.innerHTML = `
    <i class="fas fa-exclamation-circle"></i>
    <span>${message}</span>
  `;
  
  // Remove any existing error message
  const existingError = document.querySelector('.form-error');
  if (existingError) {
    existingError.remove();
  }
  
  // Insert error message at the top of the form
  uploadForm.insertBefore(errorElement, uploadForm.firstChild);
  
  // Auto remove after 5 seconds
  setTimeout(() => {
    errorElement.classList.add('fade-out');
    setTimeout(() => {
      errorElement.remove();
    }, 300);
  }, 5000);
  
  // Scroll to error message
  errorElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Show upload success message
function showUploadSuccess() {
  const successElement = document.createElement('div');
  successElement.className = 'upload-success';
  successElement.innerHTML = `
    <i class="fas fa-check-circle"></i>
    <span>Tài liệu đã được tải lên thành công!</span>
  `;
  
  // Remove any existing messages
  const existingMessages = document.querySelectorAll('.form-error, .upload-success');
  existingMessages.forEach(el => el.remove());
  
  // Insert success message at the top of the form
  uploadForm.insertBefore(successElement, uploadForm.firstChild);
  
  // Auto remove after 5 seconds
  setTimeout(() => {
    successElement.classList.add('fade-out');
    setTimeout(() => {
      successElement.remove();
    }, 300);
  }, 5000);
}
