// Mock data logs
const mockLogs = Array.from({length: 57}, (_, i) => ({
    id: 1000 + i,
    user: i % 5 === 0 ? 'admin' : 'user' + (i % 7 + 1),
    avatar: 'https://randomuser.me/api/portraits/men/' + (i % 20 + 1) + '.jpg',
    ip: '192.168.1.' + (i % 20 + 1),
    action: ['LOGIN', 'LOGOUT', 'DOCUMENT_VIEW', 'DOCUMENT_DOWNLOAD', 'SECURITY_VIOLATION', 'USER_UPDATE'][i % 6],
    time: new Date(Date.now() - i * 3600 * 1000).toLocaleString('vi-VN'),
    severity: ['INFO', 'WARN', 'ERROR', 'CRITICAL'][i % 4],
    desc: i % 6 === 4 ? 'Phát hiện truy cập bất thường' : 'Hoạt động bình thường',
    detail: {
        id: 1000 + i,
        user: i % 5 === 0 ? 'admin' : 'user' + (i % 7 + 1),
        avatar: 'https://randomuser.me/api/portraits/men/' + (i % 20 + 1) + '.jpg',
        ip: '192.168.1.' + (i % 20 + 1),
        action: ['LOGIN', 'LOGOUT', 'DOCUMENT_VIEW', 'DOCUMENT_DOWNLOAD', 'SECURITY_VIOLATION', 'USER_UPDATE'][i % 6],
        time: new Date(Date.now() - i * 3600 * 1000).toLocaleString('vi-VN'),
        severity: ['INFO', 'WARN', 'ERROR', 'CRITICAL'][i % 4],
        desc: i % 6 === 4 ? 'Phát hiện truy cập bất thường' : 'Hoạt động bình thường',
        url: '/api/login',
        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
        session: 'sess_' + (i % 10 + 1),
        resource: i % 2 === 0 ? 'Document' : 'User',
        resourceId: 200 + (i % 10),
        requestMethod: i % 2 === 0 ? 'GET' : 'POST',
        responseStatus: [200, 401, 500, 403][i % 4],
        metadata: '{"info": "Sample metadata"}'
    },
    checked: false
}));

const PAGE_SIZE = 10;
let currentPage = 1;
let filteredLogs = [...mockLogs];

function renderStats() {
    document.getElementById('stat-total').textContent = filteredLogs.length;
    document.getElementById('stat-error').textContent = filteredLogs.filter(l => l.severity === 'ERROR' || l.severity === 'CRITICAL').length;
    document.getElementById('stat-security').textContent = filteredLogs.filter(l => l.action === 'SECURITY_VIOLATION').length;
    const today = new Date().toLocaleDateString('vi-VN');
    document.getElementById('stat-today').textContent = filteredLogs.filter(l => l.time.startsWith(today)).length;
}

function renderTable() {
    const tbody = document.getElementById('logs-tbody');
    tbody.innerHTML = '';
    const start = (currentPage - 1) * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    filteredLogs.slice(start, end).forEach((log, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><input type="checkbox" class="log-checkbox" data-index="${start + idx}" ${log.checked ? 'checked' : ''}></td>
            <td>${log.id}</td>
            <td><div class="user-info"><img class="user-avatar" src="${log.avatar}" alt="avatar"><div class="user-details"><span class="user-name">${log.user}</span></div></div></td>
            <td>${log.ip}</td>
            <td data-tooltip="${log.action}"><i class="fas fa-bolt"></i> ${log.action}</td>
            <td>${log.time}</td>
            <td><span class="badge badge-severity badge-${log.severity}" data-tooltip="${log.severity}">${log.severity}</span></td>
            <td data-tooltip="${log.desc}">${log.desc.length > 30 ? log.desc.slice(0, 30) + '...' : log.desc}</td>
            <td><button class="action-btn btn-sm" onclick="showDetail(${log.id})" data-tooltip="Xem chi tiết"><i class="fas fa-eye"></i></button></td>
        `;
        tbody.appendChild(tr);
    });
    // Checkbox event
    document.querySelectorAll('.log-checkbox').forEach(cb => {
        cb.addEventListener('change', function() {
            const idx = parseInt(this.getAttribute('data-index'));
            filteredLogs[idx].checked = this.checked;
        });
    });
}

function renderPagination() {
    const totalPages = Math.ceil(filteredLogs.length / PAGE_SIZE);
    const pag = document.getElementById('pagination');
    pag.innerHTML = '';
    for (let i = 1; i <= totalPages; i++) {
        const li = document.createElement('li');
        li.className = 'page-item' + (i === currentPage ? ' active' : '');
        li.innerHTML = `<a class="page-link pagination-btn" href="#" onclick="gotoPage(${i});return false;">${i}</a>`;
        pag.appendChild(li);
    }
}

function gotoPage(page) {
    currentPage = page;
    renderTable();
    renderPagination();
}

window.showDetail = function(id) {
    const log = mockLogs.find(l => l.id === id);
    if (!log) return;
    const d = log.detail;
    document.getElementById('log-detail-body').innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <div class="detail-row"><span class="detail-label">ID:</span> ${d.id}</div>
                <div class="detail-row"><span class="detail-label">User:</span> <div class="user-info"><img class="user-avatar" src="${d.avatar}" alt="avatar"><div class="user-details"><span class="user-name">${d.user}</span></div></div></div>
                <div class="detail-row"><span class="detail-label">IP:</span> ${d.ip}</div>
                <div class="detail-row"><span class="detail-label">Hành động:</span> ${d.action}</div>
                <div class="detail-row"><span class="detail-label">Thời gian:</span> ${d.time}</div>
                <div class="detail-row"><span class="detail-label">Mức độ:</span> <span class="badge badge-severity badge-${d.severity}">${d.severity}</span></div>
                <div class="detail-row"><span class="detail-label">Mô tả:</span> ${d.desc}</div>
            </div>
            <div class="col-md-6">
                <div class="detail-row"><span class="detail-label">URL:</span> ${d.url}</div>
                <div class="detail-row"><span class="detail-label">User Agent:</span> ${d.userAgent}</div>
                <div class="detail-row"><span class="detail-label">Session:</span> ${d.session}</div>
                <div class="detail-row"><span class="detail-label">Resource:</span> ${d.resource} #${d.resourceId}</div>
                <div class="detail-row"><span class="detail-label">Request Method:</span> ${d.requestMethod}</div>
                <div class="detail-row"><span class="detail-label">Response Status:</span> ${d.responseStatus}</div>
                <div class="detail-row"><span class="detail-label">Metadata:</span> <pre>${d.metadata}</pre></div>
            </div>
        </div>
    `;
    const modal = new bootstrap.Modal(document.getElementById('logDetailModal'));
    modal.show();
}

function applyFilter() {
    const user = document.getElementById('filter-user').value.trim().toLowerCase();
    const ip = document.getElementById('filter-ip').value.trim();
    const action = document.getElementById('filter-action').value;
    const severity = document.getElementById('filter-severity').value;
    const date = document.getElementById('filter-date').value;
    const keyword = document.getElementById('filter-keyword').value.trim().toLowerCase();
    // Advanced filter
    const logDateFrom = document.getElementById('logDateFrom').value;
    const logDateTo = document.getElementById('logDateTo').value;
    const onlySecurityLogs = document.getElementById('onlySecurityLogs').checked;
    const onlyErrorLogs = document.getElementById('onlyErrorLogs').checked;
    const userAgentSearch = document.getElementById('userAgentSearch').value.trim().toLowerCase();
    const urlSearch = document.getElementById('urlSearch').value.trim().toLowerCase();
    filteredLogs = mockLogs.filter(l => {
        let ok = true;
        if (user && !l.user.toLowerCase().includes(user)) ok = false;
        if (ip && !l.ip.includes(ip)) ok = false;
        if (action && l.action !== action) ok = false;
        if (severity && l.severity !== severity) ok = false;
        if (date && !l.time.startsWith(new Date(date).toLocaleDateString('vi-VN'))) ok = false;
        if (keyword && !(l.desc.toLowerCase().includes(keyword) || l.action.toLowerCase().includes(keyword))) ok = false;
        if (logDateFrom) {
            const logDate = new Date(l.time.split(',')[0].split(' ')[0].split('/').reverse().join('-'));
            if (logDate < new Date(logDateFrom)) ok = false;
        }
        if (logDateTo) {
            const logDate = new Date(l.time.split(',')[0].split(' ')[0].split('/').reverse().join('-'));
            if (logDate > new Date(logDateTo)) ok = false;
        }
        if (onlySecurityLogs && l.action !== 'SECURITY_VIOLATION') ok = false;
        if (onlyErrorLogs && l.severity !== 'ERROR' && l.severity !== 'CRITICAL') ok = false;
        if (userAgentSearch && !l.detail.userAgent.toLowerCase().includes(userAgentSearch)) ok = false;
        if (urlSearch && !l.detail.url.toLowerCase().includes(urlSearch)) ok = false;
        return ok;
    });
    currentPage = 1;
    renderStats();
    renderTable();
    renderPagination();
}

document.getElementById('btn-filter').addEventListener('click', applyFilter);
document.getElementById('btn-export-csv').addEventListener('click', function() {
    alert('Chức năng xuất CSV chỉ là mockup!');
});
document.getElementById('btn-reset-filter').addEventListener('click', function() {
    document.querySelectorAll('.search-input, .filter-select, .date-filter, .text-filter').forEach(el => el.value = '');
    document.getElementById('onlySecurityLogs').checked = false;
    document.getElementById('onlyErrorLogs').checked = false;
    applyFilter();
});
document.getElementById('advancedFilterToggle').addEventListener('click', function() {
    const panel = document.getElementById('advancedFiltersPanel');
    panel.classList.toggle('show');
    this.classList.toggle('active');
});
document.getElementById('selectAllLogs').addEventListener('change', function() {
    const checked = this.checked;
    filteredLogs.forEach(l => l.checked = checked);
    renderTable();
});

// Khởi tạo giao diện
renderStats();
renderTable();
renderPagination(); 