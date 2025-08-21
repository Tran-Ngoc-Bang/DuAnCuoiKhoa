document.addEventListener('DOMContentLoaded', function() {
	const pdfSourceEl = document.getElementById('pdfSource');
	if (!pdfSourceEl || typeof pdfjsLib === 'undefined') return;

	const originalUrl = pdfSourceEl.getAttribute('data-src');
	if (!originalUrl) return;

	console.log('Original URL from data-src:', originalUrl);

	// Chuẩn hóa URL và tạo fallback
	const primaryUrl = originalUrl;
	const fallbackUrl = originalUrl.replace('/documents/uploads/documents/', '/uploads/documents/');
	let activeUrl = primaryUrl;

	console.log('Primary URL:', primaryUrl);
	console.log('Fallback URL:', fallbackUrl);

	const previewContainer = document.querySelector('.preview-container');
	if (!previewContainer) return;

	const prevPageBtn = document.getElementById('prevPage');
	const nextPageBtn = document.getElementById('nextPage');
	const pageInput = document.getElementById('pageInput');
	const goToPageBtn = document.getElementById('goToPage');
	const currentPageDisplay = document.querySelector('.current-page');
	const totalPagesDisplay = document.querySelector('.total-pages');

	// Phân loại theo phần mở rộng
	const ext = (originalUrl.split('?')[0].split('#')[0].split('.').pop() || '').toLowerCase();
	const isPdf = ext === 'pdf';
	const isImage = ['png','jpg','jpeg','webp','gif','bmp','svg','ico'].includes(ext);
	const isText = ['txt','md','csv','json','xml','html','css','js','py','java','cpp','c','php','sql','log'].includes(ext);
	const isOffice = ['docx','xlsx','pptx','doc','xls','ppt','odt','ods','odp'].includes(ext);
	const isArchive = ['zip','rar','7z','tar','gz','bz2'].includes(ext);
	const isVideo = ['mp4','avi','mov','wmv','flv','webm','mkv'].includes(ext);
	const isAudio = ['mp3','wav','flac','aac','ogg','wma'].includes(ext);

	console.log('File extension:', ext, 'isPdf:', isPdf);

	// Kiểm tra sẵn sàng của primaryUrl; nếu lỗi sẽ dùng fallback
	function ensureUrlAccessible(url) {
		console.log('Testing URL accessibility:', url);
		return fetch(url, { method: 'HEAD' }).then(res => {
			console.log('HEAD response for', url, ':', res.status, res.statusText);
			if (!res.ok) throw new Error('HEAD not ok');
			return url;
		}).catch((error) => {
			console.log('HEAD failed for', url, ':', error.message);
			return url === primaryUrl ? fallbackUrl : url;
		});
	}

	// Nếu không phải PDF, xử lý nhánh tương ứng và dừng
	if (!isPdf) {
		console.log('Not a PDF file, handling as:', isImage ? 'image' : isText ? 'text' : isOffice ? 'office' : isArchive ? 'archive' : isVideo ? 'video' : isAudio ? 'audio' : 'unsupported');
		// Ẩn placeholders cũ nếu có
		const placeholders = previewContainer.querySelectorAll('.preview-page-view');
		placeholders.forEach(el => el.style.display = 'none');

		// Vô hiệu hóa điều hướng
		disableControls();

		if (isImage) {
			const imgWrap = document.createElement('div');
			imgWrap.className = 'non-pdf-preview image-preview';
			const img = document.createElement('img');
			img.alt = 'Xem trước ảnh';
			img.style.maxWidth = '100%';
			img.style.borderRadius = '8px';
			// Fallback khi ảnh 404
			img.onerror = function() {
				console.log('Image failed to load, trying fallback');
				if (img.dataset.fallback !== '1') {
					img.dataset.fallback = '1';
					img.src = fallbackUrl;
				}
			};
			ensureUrlAccessible(primaryUrl).then(url => {
				console.log('Setting image src to:', url);
				img.src = url;
			});
			imgWrap.appendChild(img);
			previewContainer.prepend(imgWrap);
			if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
			if (currentPageDisplay) currentPageDisplay.textContent = 1;
			return;
		}

		if (isText) {
			const box = document.createElement('div');
			box.className = 'non-pdf-preview text-preview';
			const pre = document.createElement('pre');
			pre.style.whiteSpace = 'pre-wrap';
			pre.style.background = '#f8f9fa';
			pre.style.padding = '16px';
			pre.style.borderRadius = '8px';
			pre.style.maxHeight = '600px';
			pre.style.overflow = 'auto';
			pre.style.fontFamily = 'monospace';
			pre.style.fontSize = '14px';
			box.appendChild(pre);
			previewContainer.prepend(box);
			const max = 50 * 1024;
			ensureUrlAccessible(primaryUrl).then(url => fetch(url)).then(r => r.ok ? r.text() : Promise.reject())
				.then(text => {
					pre.textContent = text.length > max ? (text.slice(0, max) + '\n\n... (đã rút gọn) ...') : text;
				})
				.catch(() => {
					pre.textContent = 'Không thể tải nội dung văn bản để xem trước.';
				});
			if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
			if (currentPageDisplay) currentPageDisplay.textContent = 1;
			return;
		}

		if (isOffice) {
			const msg = document.createElement('div');
			msg.className = 'non-pdf-preview office-preview';
			msg.style.background = '#e3f2fd';
			msg.style.border = '1px solid #2196f3';
			msg.style.color = '#1976d2';
			msg.style.padding = '20px';
			msg.style.borderRadius = '8px';
			msg.style.textAlign = 'center';
			msg.innerHTML = `
				<div style="font-size: 48px; margin-bottom: 16px;">
					<i class="fas fa-file-word" style="color: #2196f3;"></i>
				</div>
				<h3 style="margin: 0 0 12px 0;">Tài liệu Office</h3>
				<p style="margin: 0 0 16px 0; color: #666;">
					Định dạng .${ext.toUpperCase()} không hỗ trợ xem trước trực tiếp.<br>
					Vui lòng tải xuống để xem đầy đủ nội dung.
				</p>
				<button class="download-button-small" data-premium="true" style="background: #2196f3; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
					<i class="fas fa-download"></i> Tải xuống ngay
				</button>
			`;
			previewContainer.prepend(msg);
			if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
			if (currentPageDisplay) currentPageDisplay.textContent = 1;
			return;
		}

		if (isArchive) {
			const msg = document.createElement('div');
			msg.className = 'non-pdf-preview archive-preview';
			msg.style.background = '#fff3e0';
			msg.style.border = '1px solid #ff9800';
			msg.style.color = '#f57c00';
			msg.style.padding = '20px';
			msg.style.borderRadius = '8px';
			msg.style.textAlign = 'center';
			msg.innerHTML = `
				<div style="font-size: 48px; margin-bottom: 16px;">
					<i class="fas fa-file-archive" style="color: #ff9800;"></i>
				</div>
				<h3 style="margin: 0 0 12px 0;">File nén</h3>
				<p style="margin: 0 0 16px 0; color: #666;">
					Định dạng .${ext.toUpperCase()} là file nén.<br>
					Tải xuống và giải nén để xem nội dung bên trong.
				</p>
				<button class="download-button-small" data-premium="true" style="background: #ff9800; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
					<i class="fas fa-download"></i> Tải xuống
				</button>
			`;
			previewContainer.prepend(msg);
			if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
			if (currentPageDisplay) currentPageDisplay.textContent = 1;
			return;
		}

		if (isVideo) {
			const msg = document.createElement('div');
			msg.className = 'non-pdf-preview video-preview';
			msg.style.background = '#f3e5f5';
			msg.style.border = '1px solid #9c27b0';
			msg.style.color = '#7b1fa2';
			msg.style.padding = '20px';
			msg.style.borderRadius = '8px';
			msg.style.textAlign = 'center';
			msg.innerHTML = `
				<div style="font-size: 48px; margin-bottom: 16px;">
					<i class="fas fa-file-video" style="color: #9c27b0;"></i>
				</div>
				<h3 style="margin: 0 0 12px 0;">File video</h3>
				<p style="margin: 0 0 16px 0; color: #666;">
					Định dạng .${ext.toUpperCase()} là file video.<br>
					Tải xuống để xem video đầy đủ.
				</p>
				<button class="download-button-small" data-premium="true" style="background: #9c27b0; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
					<i class="fas fa-download"></i> Tải xuống
				</button>
			`;
			previewContainer.prepend(msg);
			if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
			if (currentPageDisplay) currentPageDisplay.textContent = 1;
			return;
		}

		if (isAudio) {
			const msg = document.createElement('div');
			msg.className = 'non-pdf-preview audio-preview';
			msg.style.background = '#e8f5e8';
			msg.style.border = '1px solid #4caf50';
			msg.style.color = '#388e3c';
			msg.style.padding = '20px';
			msg.style.borderRadius = '8px';
			msg.style.textAlign = 'center';
			msg.innerHTML = `
				<div style="font-size: 48px; margin-bottom: 16px;">
					<i class="fas fa-file-audio" style="color: #4caf50;"></i>
				</div>
				<h3 style="margin: 0 0 12px 0;">File âm thanh</h3>
				<p style="margin: 0 0 16px 0; color: #666;">
					Định dạng .${ext.toUpperCase()} là file âm thanh.<br>
					Tải xuống để nghe audio đầy đủ.
				</p>
				<button class="download-button-small" data-premium="true" style="background: #4caf50; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
					<i class="fas fa-download"></i> Tải xuống
				</button>
			`;
			previewContainer.prepend(msg);
			if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
			if (currentPageDisplay) currentPageDisplay.textContent = 1;
			return;
		}

		// Fallback cho định dạng chưa hỗ trợ
		const msg = document.createElement('div');
		msg.className = 'non-pdf-preview unsupported-preview';
		msg.style.background = '#fff3cd';
		msg.style.border = '1px solid #ffeeba';
		msg.style.color = '#856404';
		msg.style.padding = '20px';
		msg.style.borderRadius = '8px';
		msg.style.textAlign = 'center';
		msg.innerHTML = `
			<div style="font-size: 48px; margin-bottom: 16px;">
				<i class="fas fa-file" style="color: #856404;"></i>
			</div>
			<h3 style="margin: 0 0 12px 0;">Định dạng không hỗ trợ</h3>
			<p style="margin: 0 0 16px 0; color: #666;">
				Định dạng .${ext.toUpperCase()} chưa hỗ trợ xem trước.<br>
				Vui lòng tải xuống để xem đầy đủ.
			</p>
			<button class="download-button-small" data-premium="true" style="background: #856404; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
				<i class="fas fa-download"></i> Tải xuống
			</button>
		`;
		previewContainer.prepend(msg);
		if (totalPagesDisplay) totalPagesDisplay.textContent = 1;
		if (currentPageDisplay) currentPageDisplay.textContent = 1;
		return;
	}

	console.log('Processing PDF file...');
	let totalPages = 0;
	let maxPreviewPages = 0;
	let currentPageIndex = 0; // 0-based

	ensureUrlAccessible(primaryUrl).then(url => {
		console.log('Using URL for PDF:', url);
		activeUrl = url;
		const loadingTask = pdfjsLib.getDocument(activeUrl);
		return loadingTask.promise;
	}).then(function(pdf) {
		console.log('PDF loaded successfully, pages:', pdf.numPages);
		// Ẩn các placeholder cũ nếu có
		const placeholders = previewContainer.querySelectorAll('.preview-page-view');
		placeholders.forEach(el => el.style.display = 'none');

		// Cập nhật tổng số trang nếu có phần hiển thị
		totalPages = pdf.numPages;
		if (totalPagesDisplay) totalPagesDisplay.textContent = totalPages;
        const totalPagesFooter = document.getElementById('preview-total-pages');
        if (totalPagesFooter) totalPagesFooter.textContent = totalPages;

		// Tạo container preview PDF động
		let pdfPreview = document.getElementById('pdf-preview-container');
		if (!pdfPreview) {
			pdfPreview = document.createElement('div');
			pdfPreview.id = 'pdf-preview-container';
			previewContainer.prepend(pdfPreview);
		}

		maxPreviewPages = Math.min(5, totalPages);
		console.log('Rendering', maxPreviewPages, 'preview pages');
		
		// Tạo canvas cho tất cả trang preview (nhưng chỉ hiển thị 1 trang)
		for (let i = 1; i <= maxPreviewPages; i++) {
			appendCanvas(pdfPreview, i);
		}
		
        const limitFooter = document.getElementById('preview-limit-count');
        if (limitFooter) limitFooter.textContent = maxPreviewPages;

		// Khởi tạo trang hiện tại (từ sessionStorage nếu có)
		const savedPage = sessionStorage.getItem('previewCurrentPage');
		currentPageIndex = savedPage ? parseInt(savedPage, 10) : 0;
		if (isNaN(currentPageIndex) || currentPageIndex < 0) currentPageIndex = 0;
		if (currentPageIndex >= maxPreviewPages) currentPageIndex = maxPreviewPages - 1;
		
		// Render trang đầu tiên
		renderPage(pdf, currentPageIndex + 1);
		updateUiForPage();
		scrollToCanvas(currentPageIndex + 1);

		// Gắn sự kiện điều hướng
		if (prevPageBtn) {
			prevPageBtn.disabled = currentPageIndex === 0;
			prevPageBtn.addEventListener('click', () => {
				if (currentPageIndex > 0) {
					currentPageIndex -= 1;
					renderPage(pdf, currentPageIndex + 1);
					updateUiForPage(true);
					scrollToCanvas(currentPageIndex + 1);
				}
			});
		}
		if (nextPageBtn) {
			nextPageBtn.disabled = currentPageIndex >= maxPreviewPages - 1;
			nextPageBtn.addEventListener('click', () => {
				if (currentPageIndex < maxPreviewPages - 1) {
					currentPageIndex += 1;
					renderPage(pdf, currentPageIndex + 1);
					updateUiForPage(true);
					scrollToCanvas(currentPageIndex + 1);
				} else {
					showPreviewLimitToast();
				}
			});
		}
		if (goToPageBtn && pageInput) {
			goToPageBtn.addEventListener('click', () => {
				const val = parseInt(pageInput.value, 10);
				if (!isNaN(val) && val >= 1 && val <= totalPages) {
					const targetIndex = Math.min(val, maxPreviewPages) - 1;
					if (val > maxPreviewPages) {
						showPreviewLimitToast();
					}
					currentPageIndex = targetIndex;
					renderPage(pdf, currentPageIndex + 1);
					updateUiForPage(true);
					scrollToCanvas(currentPageIndex + 1);
				} else {
					pageInput.value = (currentPageIndex + 1).toString();
				}
			});
			pageInput.max = totalPages;
			pageInput.value = (currentPageIndex + 1).toString();
			pageInput.addEventListener('keydown', (e) => {
				if (e.key === 'Enter') {
					e.preventDefault();
					goToPageBtn.click();
				}
			});
		}
	}).catch(function(err) {
		console.error('Failed to load PDF for preview:', err);
	});

	function updateUiForPage(save = false) {
		if (currentPageDisplay) currentPageDisplay.textContent = (currentPageIndex + 1).toString();
		if (pageInput) pageInput.value = (currentPageIndex + 1).toString();
		if (prevPageBtn) prevPageBtn.disabled = currentPageIndex === 0;
		if (nextPageBtn) nextPageBtn.disabled = currentPageIndex >= maxPreviewPages - 1;
		if (save) sessionStorage.setItem('previewCurrentPage', currentPageIndex);
	}

	function scrollToCanvas(pageNumber) {
		const canvas = document.getElementById('pdf-canvas' + pageNumber);
		if (canvas) {
			canvas.scrollIntoView({ behavior: 'smooth', block: 'start' });
		}
	}

	function showPreviewLimitToast() {
		if (typeof showToast === 'function') {
			showToast('Phiên bản xem trước chỉ hiển thị 5 trang đầu tiên.', 'warning');
		}
	}

	function disableControls() {
		if (prevPageBtn) prevPageBtn.disabled = true;
		if (nextPageBtn) nextPageBtn.disabled = true;
		if (pageInput) pageInput.disabled = true;
		if (goToPageBtn) goToPageBtn.disabled = true;
		if (currentPageDisplay) currentPageDisplay.textContent = '1';
		if (totalPagesDisplay) totalPagesDisplay.textContent = '1';
	}

	function appendCanvas(container, pageNumber, isLocked = false) {
		const canvas = document.createElement('canvas');
		canvas.id = 'pdf-canvas' + pageNumber;
		canvas.className = 'pdf-canvas';
		canvas.style.display = 'none'; // Ẩn tất cả canvas ban đầu
		if (isLocked) canvas.classList.add('locked-content');
		container.appendChild(canvas);
	}

	function renderPage(pdf, pageNumber, isLocked = false) {
		// Ẩn tất cả canvas trước
		const allCanvases = document.querySelectorAll('.pdf-canvas');
		allCanvases.forEach(canvas => {
			canvas.style.display = 'none';
		});
		
		pdf.getPage(pageNumber).then(function(page) {
			const canvas = document.getElementById('pdf-canvas' + pageNumber);
			if (!canvas) return;
			const ctx = canvas.getContext('2d');

			const baseViewport = page.getViewport({ scale: 1.0 });
			const containerWidth = 700;
			const scale = containerWidth / baseViewport.width;
			const viewport = page.getViewport({ scale });

			canvas.width = viewport.width;
			canvas.height = viewport.height;

			page.render({ canvasContext: ctx, viewport }).promise.then(function() {
				// Hiển thị canvas đã render
				canvas.style.display = 'block';
				if (isLocked) canvas.classList.add('locked-content');
			}).catch(function(e) {
				console.error('Render page error', pageNumber, e);
			});
		}).catch(function(e) {
			console.error('Get page error', pageNumber, e);
		});
	}
}); 