document.addEventListener('DOMContentLoaded', function() {
    // Initialize dashboard
    initializeDashboard();
    
    // Load charts
    loadActivityChart();
    loadCategoryChart();
    
    // Load recent activities
    loadRecentActivities();
    
    // Setup event listeners
    setupEventListeners();
});

function initializeDashboard() {
    // Animate stat cards on load
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 100);
        }, index * 100);
    });
}

function loadActivityChart() {
    fetch('/admin/api/activity-chart')
        .then(response => response.json())
        .then(data => {
            createActivityChart(data);
        })
        .catch(error => {
            console.error('Error loading activity chart:', error);
        });
}

function loadCategoryChart() {
    fetch('/admin/api/category-chart')
        .then(response => response.json())
        .then(data => {
            createCategoryChart(data);
        })
        .catch(error => {
            console.error('Error loading category chart:', error);
        });
}

function loadRecentActivities() {
    fetch('/admin/api/recent-activities')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateActivityList(data.activities);
            } else {
                console.error('Error loading activities:', data.error);
            }
        })
        .catch(error => {
            console.error('Error loading recent activities:', error);
        });
}

function updateActivityList(activities) {
    const activityList = document.querySelector('.activity-list');
    if (!activityList) return;
    
    // Clear existing activities
    activityList.innerHTML = '';
    
    if (activities.length === 0) {
        // Show empty state
        activityList.innerHTML = `
            <li class="activity-item">
                <div class="activity-icon">
                    <i class="fas fa-info-circle"></i>
                </div>
                <div class="activity-content">
                    <div class="activity-text" style="color: #6b7280; font-style: italic;">
                        Chưa có hoạt động nào gần đây
                    </div>
                    <div class="activity-time">
                        <i class="far fa-clock"></i>
                        <span>—</span>
                    </div>
                </div>
            </li>
        `;
        return;
    }
    
    // Add activities
    activities.forEach(activity => {
        const activityItem = document.createElement('li');
        activityItem.className = 'activity-item';
        
        activityItem.innerHTML = `
            <div class="activity-icon ${activity.action}">
                <i class="${activity.iconClass}"></i>
            </div>
            <div class="activity-content">
                <div class="activity-text">
                    ${activity.description}
                </div>
                <div class="activity-time">
                    <i class="far fa-clock"></i>
                    <span>${activity.timeAgo}</span>
                </div>
            </div>
        `;
        
        activityList.appendChild(activityItem);
    });
}

function createActivityChart(data) {
    const canvas = document.getElementById('activityChart');
    if (!canvas) {
        console.log('Canvas element not found');
        return;
    }
    
    // Validate data
    if (!data || !data.data || !data.labels) {
        console.error('Invalid chart data:', data);
        return;
    }
    
    const ctx = canvas.getContext('2d');
    
    // Clear existing chart
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Set canvas size
    canvas.width = canvas.offsetWidth;
    canvas.height = 300;
    
    const padding = 40;
    const chartWidth = canvas.width - (padding * 2);
    const chartHeight = canvas.height - (padding * 2);
    
    const maxValue = Math.max(...data.data);
    if (maxValue === 0) {
        // Draw empty state
        ctx.fillStyle = '#6b7280';
        ctx.font = '14px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('Chưa có dữ liệu', canvas.width / 2, canvas.height / 2);
        return;
    }
    
    const barWidth = chartWidth / data.labels.length;
    
    // Draw grid lines
    ctx.strokeStyle = '#e5e7eb';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
        const y = padding + (chartHeight / 4) * i;
        ctx.beginPath();
        ctx.moveTo(padding, y);
        ctx.lineTo(canvas.width - padding, y);
        ctx.stroke();
    }
    
    // Draw bars
    data.data.forEach((value, index) => {
        const barHeight = (value / maxValue) * chartHeight;
        const x = padding + (index * barWidth) + (barWidth * 0.2);
        const y = canvas.height - padding - barHeight;
        const width = barWidth * 0.6;
        
        // Create gradient
        const gradient = ctx.createLinearGradient(0, y, 0, y + barHeight);
        gradient.addColorStop(0, '#4361ee');
        gradient.addColorStop(1, '#3f37c9');
        
        ctx.fillStyle = gradient;
        ctx.fillRect(x, y, width, barHeight);
        
        // Draw value on top of bar
        ctx.fillStyle = '#4b5563';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(value, x + width/2, y - 5);
        
        // Draw label
        ctx.fillStyle = '#6b7280';
        ctx.font = '11px Arial';
        ctx.fillText(data.labels[index], x + width/2, canvas.height - padding + 15);
    });
}

function createCategoryChart(data) {
    const canvas = document.getElementById('categoryChart');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    
    // Clear existing chart
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Set canvas size
    canvas.width = canvas.offsetWidth;
    canvas.height = 300;
    
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(centerX, centerY) - 40;
    
    const total = data.data.reduce((sum, value) => sum + value, 0);
    let currentAngle = -Math.PI / 2; // Start from top
    
    // Draw pie slices
    data.data.forEach((value, index) => {
        const sliceAngle = (value / total) * 2 * Math.PI;
        
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle);
        ctx.closePath();
        
        ctx.fillStyle = data.colors[index];
        ctx.fill();
        
        // Draw label
        const labelAngle = currentAngle + sliceAngle / 2;
        const labelX = centerX + Math.cos(labelAngle) * (radius + 20);
        const labelY = centerY + Math.sin(labelAngle) * (radius + 20);
        
        ctx.fillStyle = '#4b5563';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(data.labels[index], labelX, labelY);
        
        currentAngle += sliceAngle;
    });
    
    // Draw center circle
    ctx.beginPath();
    ctx.arc(centerX, centerY, radius * 0.4, 0, 2 * Math.PI);
    ctx.fillStyle = 'white';
    ctx.fill();
    
    // Draw total in center
    ctx.fillStyle = '#2d3748';
    ctx.font = 'bold 16px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('Tổng: ' + total, centerX, centerY);
}

function setupEventListeners() {
    // Refresh button for activities
    const refreshBtn = document.querySelector('.refresh-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            this.style.transform = 'rotate(360deg)';
            this.style.transition = 'transform 0.5s ease';
            
            // Load fresh activity data instead of full page reload
            loadRecentActivities();
            
            setTimeout(() => {
                this.style.transform = 'rotate(0deg)';
            }, 500);
        });
    }
    
    // Chart period selector
    const periodSelect = document.querySelector('.chart-period-select');
    if (periodSelect) {
        periodSelect.addEventListener('change', function() {
            // Reload chart with new period
            loadActivityChart();
        });
    }
    
    // Chart filter buttons
    const filterBtns = document.querySelectorAll('.chart-filter-btn');
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            filterBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            // Handle document classification filter
            const filterType = this.getAttribute('data-type');
            if (filterType) {
                handleDocumentFilter(filterType);
            } else {
                // Reload category chart with filter
                loadCategoryChart();
            }
        });
    });
    
    // Task checkboxes
    const taskCheckboxes = document.querySelectorAll('.task-checkbox input');
    taskCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const taskItem = this.closest('.task-item');
            if (this.checked) {
                taskItem.style.opacity = '0.6';
                taskItem.style.textDecoration = 'line-through';
            } else {
                taskItem.style.opacity = '1';
                taskItem.style.textDecoration = 'none';
            }
        });
    });
    
    // Stat card hover effects
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 10px 25px rgba(0, 0, 0, 0.1)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.05)';
        });
    });
}

// Auto refresh dashboard every 5 minutes
setInterval(() => {
    loadActivityChart();
    loadCategoryChart();
}, 5 * 60 * 1000);

// Real-time updates simulation
function simulateRealTimeUpdates() {
    const statNumbers = document.querySelectorAll('.stat-number');
    
    setInterval(() => {
        statNumbers.forEach(stat => {
            const currentValue = parseInt(stat.textContent.replace(/[^\d]/g, ''));
            const change = Math.floor(Math.random() * 3) - 1; // -1, 0, or 1
            const newValue = Math.max(0, currentValue + change);
            
            if (change !== 0) {
                stat.style.transform = 'scale(1.1)';
                stat.style.transition = 'transform 0.2s ease';
                
                setTimeout(() => {
                    stat.textContent = newValue.toLocaleString();
                    stat.style.transform = 'scale(1)';
                }, 100);
            }
        });
    }, 30000); // Update every 30 seconds
}

// Handle document classification filter
function handleDocumentFilter(filterType) {
    // Update pie chart display
    updateDocumentPieChart(filterType);
}

function updateDocumentPieChart(filterType) {
    const pieChart = document.querySelector('.document-pie-chart');
    if (!pieChart) return;
    
    // Add visual feedback to show which filter is active
    const legend = pieChart.querySelector('div[style*="margin-top: 1rem"]');
    if (legend) {
        const legendItems = legend.querySelectorAll('div[style*="display: flex"]');
        
        legendItems.forEach((item, index) => {
            const isHighlighted = 
                (filterType === 'free' && index === 0) || 
                (filterType === 'premium' && index === 1) ||
                filterType === 'all';
            
            item.style.opacity = isHighlighted ? '1' : '0.5';
            item.style.transition = 'opacity 0.3s ease';
        });
    }
}

// Initialize real-time updates
simulateRealTimeUpdates();