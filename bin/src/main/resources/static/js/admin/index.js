document.addEventListener('DOMContentLoaded', function () {
    // Admin dropdown toggle
    const adminProfile = document.querySelector('.admin-profile');
    if (adminProfile) {
        adminProfile.addEventListener('click', function () {
            this.classList.toggle('active');
        });
    }

    // Task checkboxes
    const taskCheckboxes = document.querySelectorAll('.task-checkbox input');
    taskCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            const taskItem = this.closest('.task-item');
            if (this.checked) {
                taskItem.classList.add('completed');
            } else {
                taskItem.classList.remove('completed');
            }
        });
    });

    // Chart filter buttons
    const chartFilterBtns = document.querySelectorAll('.chart-filter-btn');
    chartFilterBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            chartFilterBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Initialize charts with backend data
    initializeActivityChart();
    initializeCategoryChart();
});

// Initialize Activity Chart
function initializeActivityChart() {
    const ctx = document.getElementById('activityChart');
    if (!ctx) return;

    // Use data from backend if available, otherwise use mock data
    const chartData = window.activityData && window.activityData.length > 0 ? 
        processActivityData(window.activityData) : 
        getMockActivityData();

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: chartData.labels,
            datasets: [{
                label: 'Hoạt động',
                data: chartData.data,
                backgroundColor: 'rgba(67, 97, 238, 0.8)',
                borderColor: 'rgba(67, 97, 238, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

// Initialize Category Chart
function initializeCategoryChart() {
    const ctx = document.getElementById('categoryChart');
    if (!ctx) return;

    // Use data from backend if available, otherwise use mock data
    const chartData = window.categoryData && window.categoryData.length > 0 ? 
        processCategoryData(window.categoryData) : 
        getMockCategoryData();

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: chartData.labels,
            datasets: [{
                data: chartData.data,
                backgroundColor: [
                    '#4361ee',
                    '#f72585',
                    '#10b981',
                    '#f59e0b',
                    '#8b5cf6'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        usePointStyle: true
                    }
                }
            }
        }
    });
}

// Process activity data from backend
function processActivityData(activityData) {
    const labels = [];
    const data = [];
    
    activityData.forEach(item => {
        labels.push(formatDate(item.date));
        data.push(item.count);
    });
    
    return { labels, data };
}

// Process category data from backend
function processCategoryData(categoryData) {
    const labels = [];
    const data = [];
    
    categoryData.forEach(item => {
        labels.push(item.categoryName);
        data.push(item.documentCount);
    });
    
    return { labels, data };
}

// Format date for chart labels
function formatDate(dateString) {
    const date = new Date(dateString);
    const days = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    return days[date.getDay()];
}

// Mock data for fallback
function getMockActivityData() {
    return {
        labels: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'],
        data: [30, 50, 70, 60, 75, 85, 60]
    };
}

function getMockCategoryData() {
    return {
        labels: ['Công nghệ thông tin', 'Kinh tế', 'Ngoại ngữ', 'Kỹ thuật', 'Khác'],
        data: [30, 25, 20, 15, 10]
    };
}

// Document action functions
function viewDocument(documentId) {
    window.open(`/admin/documents/${documentId}`, '_blank');
}

function approveDocument(documentId) {
    if (confirm('Bạn có chắc chắn muốn duyệt tài liệu này?')) {
        fetch(`/admin/documents/${documentId}/approve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert('Có lỗi xảy ra: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi duyệt tài liệu');
        });
    }
}

function rejectDocument(documentId) {
    const reason = prompt('Lý do từ chối:');
    if (reason !== null) {
        fetch(`/admin/documents/${documentId}/reject`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
            },
            body: JSON.stringify({ reason: reason })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert('Có lỗi xảy ra: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi từ chối tài liệu');
        });
    }
}

function editDocument(documentId) {
    window.location.href = `/admin/documents/${documentId}/edit`;
}

function deleteDocument(documentId) {
    if (confirm('Bạn có chắc chắn muốn xóa tài liệu này?')) {
        fetch(`/admin/documents/${documentId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert('Có lỗi xảy ra: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Có lỗi xảy ra khi xóa tài liệu');
        });
    }
}
