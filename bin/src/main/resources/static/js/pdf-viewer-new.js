/**
 * Document Viewer Functionality
 * Xử lý các tương tác trong trang xem trước tài liệu
 */

/**
 * Cập nhật tiêu đề tài liệu dựa trên ID
 */
function updateDocumentTitle(docId) {
  let title = "Tài liệu xem trước";
  let author = "Tác giả";
  
  // Định nghĩa thông tin cho các tài liệu khác nhau
  if (docId === 'master-spring-boot') {
    title = "Master Spring & Spring Boot với Hibernate & React";
    author = "Ranga Karnan";
    
    // Cập nhật tiêu đề trên header
    const docTitle = document.querySelector('.document-title');
    if (docTitle) docTitle.textContent = title;
    
    // Cập nhật thông tin tác giả
    const docAuthor = document.querySelector('.document-author');
    if (docAuthor) docAuthor.textContent = author;
    
    // Cập nhật tiêu đề trang
    document.title = title + " - Xem trước | EduShare";
  }
}

document.addEventListener('DOMContentLoaded', function() {
  initDocumentViewer();
  initToolbarActions();
  initSidebarThumbActions();
  initNotesPanel();
  initZoomControls();
  initPageNavigation();
  initDownloadDocument();
});

/**
 * Khởi tạo chức năng chính của Document Viewer
 */
function initDocumentViewer() {
  // Hiển thị loading với hiệu ứng mượt mà
  const loader = document.getElementById('documentLoader');
  if (loader) {
    // Hiển thị loader với hiệu ứng fade in
    loader.style.opacity = '0';
    loader.style.display = 'flex';
    
    // Hiệu ứng fade in cho loader
    setTimeout(() => {
      loader.style.opacity = '1';
    }, 50);
    
    // Làm mờ các trang hiện tại và thêm hiệu ứng blur
    const pages = document.querySelectorAll('.document-page');
    if (pages.length > 0) {
      pages.forEach(page => {
        page.style.opacity = '0.5';
        page.style.filter = 'blur(2px)';
        page.style.transform = 'scale(0.98)';
        page.style.transition = 'all 0.5s ease';
      });
    }
  }
  
  // Tạo sự kiện cho nút unlock
  const unlockBtn = document.getElementById('unlockDocumentBtn');
  if (unlockBtn) {
    unlockBtn.addEventListener('click', function() {
      showDownloadConfirmation();
    });
  }
  
  // Load PDF document with PDF.js
  loadPdfDocument('../assets/documents/master-spring-and-spring-boot.pdf');
  
  // Hiển thị thông báo giới hạn trang xem trước
  setTimeout(() => {
    showToast('Bạn đang xem bản xem trước. Chỉ có thể xem 5 trang đầu tiên.', 'info', 8000);
  }, 1000);
}

/**
 * Load và hiển thị file PDF
 */
function loadPdfDocument(pdfPath) {
  const loadingTask = pdfjsLib.getDocument(pdfPath);
  
  loadingTask.promise.then(function(pdf) {
    console.log('PDF document loaded successfully!');
    
    // Lấy thông tin từ URL nếu có
    const urlParams = new URLSearchParams(window.location.search);
    const docId = urlParams.get('doc');
    
    // Cập nhật tiêu đề tài liệu dựa trên docId
    if (docId) {
      updateDocumentTitle(docId);
    }
    
    // Load first 5 pages
    for (let i = 1; i <= 5; i++) {
      renderPage(pdf, i);
    }
    
    // Load previews for locked pages (sẽ bị che mờ)
    renderPage(pdf, 6, true);
    renderPage(pdf, 7, true);
    
    // Ẩn loading sau khi tải xong với hiệu ứng mượt mà
    const loader = document.getElementById('documentLoader');
    if (loader) {
      // Ẩn loader từ từ
      loader.style.opacity = '0';
      
      // Hiển thị lại các trang với hiệu ứng đẹp mắt
      const pages = document.querySelectorAll('.document-page');
      if (pages.length > 0) {
        pages.forEach((page, index) => {
          // Áp dụng hiệu ứng staggered để các trang xuất hiện lần lượt
          setTimeout(() => {
            page.style.opacity = '1';
            page.style.filter = 'blur(0)';
            page.style.transform = 'scale(1)';
          }, 100 * index);
        });
      }
      
      // Ẩn loader hoàn toàn sau khi hoàn thành
      setTimeout(() => {
        loader.style.display = 'none';
      }, 500);
      
      // Hiển thị thông báo chuyển trang
      showPageTransitionNotification(1, 45);
    }
  }).catch(function(error) {
    console.error('Error loading PDF:', error);
    showToast('Không thể tải tài liệu PDF. Vui lòng thử lại sau.', 'error');
    const loader = document.getElementById('documentLoader');
    if (loader) {
      loader.style.display = 'none';
    }
  });
}

/**
 * Render một trang PDF cụ thể
 */
function renderPage(pdf, pageNumber, isLocked = false) {
  pdf.getPage(pageNumber).then(function(page) {
    // Canvas element cho trang này
    const canvas = document.getElementById('pdf-canvas' + pageNumber);
    if (!canvas) return;
    
    const context = canvas.getContext('2d');
    
    // Tạo wrapper cho canvas nếu chưa có
    let wrapper = canvas.parentNode;
    if (!wrapper || !wrapper.classList.contains('canvas-wrapper')) {
      // Tạo wrapper mới
      wrapper = document.createElement('div');
      wrapper.className = 'canvas-wrapper';
      
      // Tìm container
      const container = document.getElementById('pdf-preview-container');
      
      // Di chuyển canvas vào wrapper
      if (canvas.parentNode) {
        canvas.parentNode.replaceChild(wrapper, canvas);
      }
      wrapper.appendChild(canvas);
      
      // Thêm số trang vào wrapper
      const pageNumberDiv = document.createElement('div');
      pageNumberDiv.className = 'page-number';
      pageNumberDiv.textContent = pageNumber + ' / 45';
      wrapper.appendChild(pageNumberDiv);
    }
    
    // Lấy kích thước viewport
    const viewport = page.getViewport({ scale: 1.0 });
    
    // Tính tỷ lệ để vừa với chiều rộng container
    const containerWidth = 700; // Giảm kích thước để nội dung hiển thị rõ ràng hơn
    const scale = containerWidth / viewport.width;
    const scaledViewport = page.getViewport({ scale: scale });
    
    // Thiết lập kích thước canvas
    canvas.height = scaledViewport.height;
    canvas.width = scaledViewport.width;
    
    // Render PDF page vào canvas
    const renderContext = {
      canvasContext: context,
      viewport: scaledViewport
    };
    
    page.render(renderContext).promise.then(function() {
      console.log('Page ' + pageNumber + ' rendered successfully');
      
      // Cập nhật thumbnail cho trang này
      updatePageThumbnail(canvas, pageNumber);
      
      if (isLocked) {
        // Nếu là trang bị khóa, thêm lớp mờ
        canvas.classList.add('locked-content');
      }
    });
  }).catch(function(error) {
    console.error('Error rendering page ' + pageNumber + ':', error);
  });
}

/**
 * Tạo và cập nhật hình thumbnail cho trang
 */
function updatePageThumbnail(canvas, pageNumber) {
  // Tìm thumbnail tương ứng
  const thumbItems = document.querySelectorAll('.thumb-item');
  if (thumbItems.length >= pageNumber) {
    const thumbImg = thumbItems[pageNumber-1].querySelector('.thumb-img');
    if (!thumbImg) return;
    
    // Tạo thumbnail từ canvas
    const thumbCanvas = document.createElement('canvas');
    const thumbContext = thumbCanvas.getContext('2d');
    
    // Kích thước thumbnail
    thumbCanvas.width = 160;
    thumbCanvas.height = 200;
    
    // Scale và vẽ lại từ canvas gốc
    thumbContext.drawImage(canvas, 0, 0, canvas.width, canvas.height, 0, 0, 160, 200);
    
    // Thay thế hình ảnh thumbnail
    thumbImg.src = thumbCanvas.toDataURL();
  }
}

/**
 * Khởi tạo các nút công cụ trên thanh toolbar
 */
function initToolbarActions() {
  // Nút tìm kiếm trong tài liệu
  const searchDocument = document.getElementById('searchDocument');
  if (searchDocument) {
    searchDocument.addEventListener('click', function() {
      showToast('Tính năng tìm kiếm trong tài liệu chỉ có sẵn cho tài liệu đầy đủ.', 'warning');
    });
  }
  
  // Nút in tài liệu
  const printDocument = document.getElementById('printDocument');
  if (printDocument) {
    printDocument.addEventListener('click', function() {
      showToast('Tính năng in tài liệu chỉ có sẵn cho tài liệu đầy đủ.', 'warning');
    });
  }
  
  // Nút tải xuống trên toolbar
  const downloadButton = document.getElementById('downloadButton');
  if (downloadButton) {
    downloadButton.addEventListener('click', function() {
      showDownloadConfirmation();
    });
  }
}

/**
 * Khởi tạo tương tác với sidebar thumbnails
 */
function initSidebarThumbActions() {
  // Nút hiển thị sidebar thumbnails
  const toggleSidebar = document.getElementById('toggleSidebar');
  const documentSidebar = document.getElementById('documentSidebar');
  const notesPanel = document.getElementById('notesPanel');
  const toggleNotes = document.getElementById('toggleNotes');
  
  if (toggleSidebar && documentSidebar) {
    toggleSidebar.addEventListener('click', function() {
      documentSidebar.classList.toggle('visible');
      this.classList.toggle('active');
      
      // Đóng panel notes nếu đang mở
      if (notesPanel && toggleNotes) {
        notesPanel.classList.remove('visible');
        toggleNotes.classList.remove('active');
      }
    });
    
    // Nút đóng sidebar
    const closeSidebar = document.getElementById('closeSidebar');
    if (closeSidebar) {
      closeSidebar.addEventListener('click', function() {
        documentSidebar.classList.remove('visible');
        toggleSidebar.classList.remove('active');
      });
    }
  }
  
  // Xử lý click vào thumbnail
  const thumbnails = document.querySelectorAll('.thumb-item');
  thumbnails.forEach((thumb) => {
    thumb.addEventListener('click', function() {
      // Xóa trạng thái active cho tất cả thumbnails
      thumbnails.forEach(t => t.classList.remove('active'));
      
      // Thêm trạng thái active cho thumbnail được chọn
      this.classList.add('active');
      
      // Lấy số trang từ data attribute
      const pageNum = parseInt(this.getAttribute('data-page'));
      
      // Cập nhật trang hiện tại
      const currentPage = document.getElementById('currentPage');
      if (currentPage) {
        currentPage.value = pageNum;
      }
      
      // Chuyển đến trang tương ứng
      changePage(pageNum);
    });
  });
}

/**
 * Khởi tạo tương tác với panel ghi chú
 */
function initNotesPanel() {
  // Nút hiển thị panel ghi chú
  const toggleNotes = document.getElementById('toggleNotes');
  const notesPanel = document.getElementById('notesPanel');
  const documentSidebar = document.getElementById('documentSidebar');
  const toggleSidebar = document.getElementById('toggleSidebar');
  
  if (toggleNotes && notesPanel) {
    toggleNotes.addEventListener('click', function() {
      notesPanel.classList.toggle('visible');
      this.classList.toggle('active');
      
      // Đóng sidebar nếu đang mở
      if (documentSidebar && toggleSidebar) {
        documentSidebar.classList.remove('visible');
        toggleSidebar.classList.remove('active');
      }
    });
    
    // Nút đóng panel ghi chú
    const closeNotes = document.getElementById('closeNotes');
    if (closeNotes) {
      closeNotes.addEventListener('click', function() {
        notesPanel.classList.remove('visible');
        toggleNotes.classList.remove('active');
      });
    }
  }
  
  // Xử lý nút lưu ghi chú
  const saveNote = document.getElementById('saveNote');
  const noteTextarea = document.getElementById('noteTextarea');
  
  if (saveNote && noteTextarea) {
    saveNote.addEventListener('click', function() {
      const noteText = noteTextarea.value.trim();
      if (noteText) {
        addNewNote(noteText);
        noteTextarea.value = '';
      } else {
        showToast('Vui lòng nhập nội dung ghi chú.', 'warning');
      }
    });
  }
}

/**
 * Khởi tạo điều khiển zoom
 */
function initZoomControls() {
  const zoomIn = document.getElementById('zoomIn');
  const zoomOut = document.getElementById('zoomOut');
  const zoomLevel = document.getElementById('zoomLevel');
  const documentPages = document.querySelector('.document-pages');
  
  let currentZoom = 100;
  
  if (zoomIn && zoomOut && zoomLevel && documentPages) {
    zoomIn.addEventListener('click', function() {
      if (currentZoom < 200) {
        currentZoom += 10;
        updateZoom();
      }
    });
    
    zoomOut.addEventListener('click', function() {
      if (currentZoom > 50) {
        currentZoom -= 10;
        updateZoom();
      }
    });
    
    function updateZoom() {
      zoomLevel.textContent = `${currentZoom}%`;
      documentPages.style.transform = `scale(${currentZoom / 100})`;
      documentPages.style.transformOrigin = 'top center';
    }
  }
}

/**
 * Khởi tạo điều khiển chuyển trang
 */
function initPageNavigation() {
  const prevPage = document.getElementById('prevPage');
  const nextPage = document.getElementById('nextPage');
  const currentPage = document.getElementById('currentPage');
  const totalPages = document.querySelector('.total-pages');
  
  let totalPagesNum = 45; // Tổng số trang của tài liệu
  let maxPreviewPages = 5; // Số trang tối đa được xem trước
  let currentPageNum = 1;
  
  if (prevPage && nextPage && currentPage) {
    prevPage.addEventListener('click', function() {
      let pageNum = parseInt(currentPage.value);
      if (pageNum > 1) {
        pageNum--;
        currentPage.value = pageNum;
        changePage(pageNum);
      }
    });
    
    nextPage.addEventListener('click', function() {
      let pageNum = parseInt(currentPage.value);
      if (pageNum < totalPagesNum) {
        pageNum++;
        currentPage.value = pageNum;
        
        if (pageNum > maxPreviewPages) {
          showToast('Phiên bản xem trước chỉ cho phép xem ' + maxPreviewPages + ' trang đầu tiên. Vui lòng tải xuống tài liệu đầy đủ.', 'warning');
          return;
        }
        
        changePage(pageNum);
      }
    });
    
    currentPage.addEventListener('change', function() {
      let pageNum = parseInt(currentPage.value);
      
      // Kiểm tra giá trị hợp lệ
      if (isNaN(pageNum) || pageNum < 1) {
        pageNum = 1;
      } else if (pageNum > totalPagesNum) {
        pageNum = totalPagesNum;
      }
      
      currentPage.value = pageNum;
      
      if (pageNum > maxPreviewPages) {
        showToast('Phiên bản xem trước chỉ cho phép xem ' + maxPreviewPages + ' trang đầu tiên. Vui lòng tải xuống tài liệu đầy đủ.', 'warning');
        return;
      }
      
      changePage(pageNum);
    });
  }
}

/**
 * Thay đổi trang hiện tại
 */
function changePage(pageNum) {
  // Cập nhật thumbnail active trong sidebar
  const thumbnails = document.querySelectorAll('.thumb-item');
  thumbnails.forEach((thumb) => {
    const thumbPage = parseInt(thumb.getAttribute('data-page'));
    if (thumbPage === pageNum) {
      thumb.classList.add('active');
    } else {
      thumb.classList.remove('active');
    }
  });
  
  // Cuộn đến canvas của trang được chọn
  const canvas = document.getElementById('pdf-canvas' + pageNum);
  if (canvas) {
    // Thêm hiệu ứng loading
    const loader = document.getElementById('documentLoader');
    if (loader) {
      loader.style.display = 'flex';
      
      // Ẩn loader sau 1 giây
      setTimeout(() => {
        loader.style.display = 'none';
        canvas.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 500);
    } else {
      canvas.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}

/**
 * Thêm ghi chú mới
 */
function addNewNote(noteText) {
  const notesList = document.getElementById('notesList');
  const currentPage = document.getElementById('currentPage');
  const pageNum = currentPage ? currentPage.value : 1;
  
  if (notesList) {
    // Tạo cấu trúc HTML cho ghi chú mới
    const today = new Date();
    const dateStr = `${today.getDate()}/${today.getMonth() + 1}/${today.getFullYear()}`;
    
    const noteItem = document.createElement('div');
    noteItem.className = 'note-item';
    noteItem.innerHTML = `
      <div class="note-meta">
        <span class="note-page">Trang ${pageNum}</span>
        <span class="note-date">${dateStr}</span>
      </div>
      <div class="note-text">
        ${noteText}
      </div>
    `;
    
    // Thêm vào đầu danh sách
    notesList.insertBefore(noteItem, notesList.firstChild);
    
    showToast('Ghi chú đã được lưu thành công.', 'success');
  }
}

/**
 * Khởi tạo sự kiện tải xuống tài liệu
 */
function initDownloadDocument() {
  // Nút tải xuống trên header
  const downloadDocument = document.getElementById('downloadDocument');
  if (downloadDocument) {
    downloadDocument.addEventListener('click', function() {
      showDownloadConfirmation();
    });
  }
}

/**
 * Hiển thị hộp thoại xác nhận tải xuống
 */
function showDownloadConfirmation() {
  // Tạo phần tử modal
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal-container">
      <div class="modal-header">
        <h3>Xác nhận tải xuống</h3>
        <button class="modal-close" id="modalClose">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="modal-body">
        <p>Bạn sắp tải xuống tài liệu <strong>Master Spring & Spring Boot với Hibernate & React</strong>.</p>
        <p>Tài liệu này có phí <strong>25 xu</strong>. Số dư hiện tại của bạn: <strong>250 xu</strong>.</p>
        
        <div class="document-preview-info">
          <div class="preview-image">
            <img src="https://via.placeholder.com/100x120/f8f9fa/6c757d?text=Document" alt="Document Preview">
          </div>
          <div class="preview-details">
            <div class="preview-author">Tác giả: Ranga Karnan</div>
            <div class="preview-format">Định dạng: PDF</div>
            <div class="preview-size">Kích thước: 2.5 MB</div>
            <div class="preview-pages">Số trang: 45 trang</div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="modal-button cancel" id="cancelDownload">Hủy bỏ</button>
        <button class="modal-button primary" id="confirmDownload">
          <i class="fas fa-download"></i> Xác nhận tải xuống
        </button>
      </div>
    </div>
  `;
  
  // Thêm CSS inline cho modal
  const style = document.createElement('style');
  style.textContent = `
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      animation: fadeIn 0.3s ease;
    }
    
    .modal-container {
      background-color: white;
      border-radius: var(--radius-lg);
      width: 100%;
      max-width: 500px;
      box-shadow: var(--shadow-lg);
      animation: slideIn 0.3s ease;
    }
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: var(--spacing-4) var(--spacing-6);
      border-bottom: 1px solid var(--gray-200);
    }
    
    .modal-header h3 {
      margin: 0;
      font-weight: 600;
      color: var(--gray-800);
    }
    
    .modal-close {
      background: transparent;
      border: none;
      color: var(--gray-500);
      cursor: pointer;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: all 0.2s ease;
    }
    
    .modal-close:hover {
      background-color: var(--gray-100);
      color: var(--gray-800);
    }
    
    .modal-body {
      padding: var(--spacing-6);
    }
    
    .document-preview-info {
      display: flex;
      gap: var(--spacing-4);
      margin-top: var(--spacing-4);
      background-color: var(--gray-50);
      padding: var(--spacing-3);
      border-radius: var(--radius);
    }
    
    .preview-image img {
      border-radius: var(--radius-sm);
      border: 1px solid var(--gray-300);
    }
    
    .preview-details {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-2);
      font-size: 0.875rem;
      color: var(--gray-700);
    }
    
    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: var(--spacing-3);
      padding: var(--spacing-4) var(--spacing-6);
      border-top: 1px solid var(--gray-200);
    }
    
    .modal-button {
      padding: var(--spacing-2) var(--spacing-4);
      border-radius: var(--radius);
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    
    .modal-button.cancel {
      background-color: var(--white);
      border: 1px solid var(--gray-300);
      color: var(--gray-700);
    }
    
    .modal-button.cancel:hover {
      background-color: var(--gray-100);
    }
    
    .modal-button.primary {
      background-color: var(--primary);
      border: 1px solid var(--primary);
      color: white;
      display: flex;
      align-items: center;
      gap: var(--spacing-2);
    }
    
    .modal-button.primary:hover {
      background-color: var(--secondary);
    }
    
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    
    @keyframes slideIn {
      from { transform: translateY(-20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
  `;
  
  document.head.appendChild(style);
  document.body.appendChild(modal);
  
  // Xử lý đóng modal
  const closeModal = () => {
    modal.classList.add('fade-out');
    setTimeout(() => {
      document.body.removeChild(modal);
    }, 300);
  };
  
  // Xử lý các nút
  document.getElementById('modalClose').addEventListener('click', closeModal);
  document.getElementById('cancelDownload').addEventListener('click', closeModal);
  
  document.getElementById('confirmDownload').addEventListener('click', function() {
    closeModal();
    simulateDownload();
  });
}

/**
 * Giả lập tải xuống tài liệu
 */
function simulateDownload() {
  showToast('Đang chuẩn bị tải xuống tài liệu...', 'info');
  
  // Giả lập quá trình tải xuống
  setTimeout(() => {
    showToast('Tài liệu đã được tải xuống thành công! Bạn đã sử dụng 25 xu.', 'success');
    
    // Tạo link tải file
    const pdfFile = '../assets/documents/course-presentation-master-spring-and-spring-boot.pdf';
    const a = document.createElement('a');
    a.href = pdfFile;
    a.download = 'master-spring-and-spring-boot.pdf';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }, 2000);
}

/**
 * Hiển thị thông báo toast
 */
function showToast(message, type = 'info', duration = 5000) {
  const toastContainer = document.getElementById('toastContainer');
  
  if (!toastContainer) return;
  
  // Tạo toast
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  
  // Icon dựa vào type
  let icon = '';
  switch (type) {
    case 'success':
      icon = '<i class="fas fa-check-circle"></i>';
      break;
    case 'error':
      icon = '<i class="fas fa-exclamation-circle"></i>';
      break;
    case 'warning':
      icon = '<i class="fas fa-exclamation-triangle"></i>';
      break;
    default:
      icon = '<i class="fas fa-info-circle"></i>';
  }
  
  // Nội dung toast
  toast.innerHTML = `
    <div class="toast-content">
      <div class="toast-icon">${icon}</div>
      <div class="toast-message">${message}</div>
    </div>
    <button class="toast-close"><i class="fas fa-times"></i></button>
  `;
  
  // Thêm vào container
  toastContainer.appendChild(toast);
  
  // Xử lý nút đóng
  const closeBtn = toast.querySelector('.toast-close');
  if (closeBtn) {
    closeBtn.addEventListener('click', function() {
      removeToast(toast);
    });
  }
  
  // Hiệu ứng hiển thị
  setTimeout(() => {
    toast.classList.add('show');
  }, 10);
  
  // Tự động ẩn sau duration ms
  setTimeout(() => {
    removeToast(toast);
  }, duration);
}

/**
 * Xóa toast thông báo
 */
function removeToast(toast) {
  setTimeout(() => {
    toast.classList.remove('show');
    
    setTimeout(() => {
      toast.remove();
    }, 300);
  }, 300);
}

/**
 * Hiển thị thông báo chuyển trang với hiệu ứng đẹp mắt
 */
function showPageTransitionNotification(pageNum, totalPages) {
  // Tạo phần tử hiển thị thông báo
  const pageInfo = document.createElement('div');
  pageInfo.className = 'page-transition-info';
  pageInfo.innerHTML = `
    <div class="page-number-display">
      <span class="current">${pageNum}</span>
      <span class="separator">/</span>
      <span class="total">${totalPages}</span>
    </div>
  `;
  document.body.appendChild(pageInfo);
  
  // Hiệu ứng fade in
  setTimeout(() => {
    pageInfo.style.opacity = '1';
    pageInfo.style.transform = 'translate(-50%, -50%) scale(1)';
  }, 50);
  
  // Tự động ẩn sau vài giây
  setTimeout(() => {
    pageInfo.style.opacity = '0';
    pageInfo.style.transform = 'translate(-50%, -50%) scale(0.8)';
    
    setTimeout(() => {
      pageInfo.remove();
    }, 300);
  }, 1500);
}