

document.addEventListener('DOMContentLoaded', function () {
    // Admin dropdown toggle
    const adminProfile = document.querySelector('.admin-profile');
    if (adminProfile) {
        adminProfile.addEventListener('click', function () {
            this.classList.toggle('active');
        });
    }

    // Chart.js - Default Configuration
    Chart.defaults.font.family = '"Nunito", sans-serif';
    Chart.defaults.color = '#64748b';
    Chart.defaults.responsive = true;
    Chart.defaults.maintainAspectRatio = false;

    // Check if dark mode is active
    const isDarkMode = document.body.classList.contains('dark-mode');
    const gridColor = isDarkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)';

    // Traffic Chart
    const trafficChartCtx = document.getElementById('trafficChart').getContext('2d');
    const trafficChart = new Chart(trafficChartCtx, {
        type: 'line',
        data: {
            labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30'],
            datasets: [{
                label: 'Tổng lượt truy cập',
                data: [1200, 1300, 1250, 1420, 1650, 1700, 1800, 1750, 1900, 2100, 2200, 2300, 2400, 2350, 2500, 2600, 2700, 2650, 2800, 2900, 3100, 3200, 3300, 3250, 3400, 3500, 3600, 3650, 3800, 4000],
                borderColor: '#4361ee',
                backgroundColor: 'rgba(67, 97, 238, 0.1)',
                tension: 0.4,
                fill: true
            },
            {
                label: 'Người dùng mới',
                data: [200, 220, 210, 240, 250, 260, 280, 270, 290, 310, 330, 350, 370, 360, 380, 400, 420, 410, 430, 450, 470, 490, 510, 500, 520, 540, 560, 550, 580, 600],
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                tension: 0.4,
                fill: true
            },
            {
                label: 'Lượt tải',
                data: [500, 520, 510, 530, 550, 570, 590, 580, 600, 620, 640, 660, 680, 670, 690, 710, 730, 720, 750, 780, 810, 840, 870, 860, 890, 920, 950, 940, 970, 1000],
                borderColor: '#f59e0b',
                backgroundColor: 'rgba(245, 158, 11, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            interaction: {
                mode: 'index',
                intersect: false,
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.dataset.label + ': ' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    title: {
                        display: true,
                        text: 'Ngày (tháng 4)'
                    }
                },
                y: {
                    grid: {
                        color: gridColor
                    },
                    beginAtZero: true,
                    ticks: {
                        callback: function (value) {
                            if (value >= 1000) {
                                return (value / 1000).toFixed(1) + 'K';
                            }
                            return value;
                        }
                    }
                }
            }
        }
    });

    // User Growth Chart
    const userGrowthChartCtx = document.getElementById('userGrowthChart').getContext('2d');
    const userGrowthChart = new Chart(userGrowthChartCtx, {
        type: 'line',
        data: {
            labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
            datasets: [{
                label: 'Người dùng mới',
                data: [850, 920, 980, 1245, 1400, 1520, 1650, 1780, 1900, 2150, 2350, 2500],
                borderColor: '#4361ee',
                backgroundColor: 'rgba(67, 97, 238, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.dataset.label + ': ' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    }
                },
                y: {
                    grid: {
                        color: gridColor
                    },
                    beginAtZero: true,
                    ticks: {
                        callback: function (value) {
                            if (value >= 1000) {
                                return (value / 1000).toFixed(1) + 'K';
                            }
                            return value;
                        }
                    }
                }
            }
        }
    });

    // Category Distribution Chart
    const categoryDistributionChartCtx = document.getElementById('categoryDistributionChart').getContext('2d');
    const categoryDistributionChart = new Chart(categoryDistributionChartCtx, {
        type: 'pie',
        data: {
            labels: ['Công nghệ thông tin', 'Kinh tế - Quản trị', 'Ngoại ngữ', 'Kỹ thuật - Công nghệ', 'Khoa học xã hội', 'Khoa học tự nhiên'],
            datasets: [{
                data: [35, 25, 15, 10, 8, 7],
                backgroundColor: [
                    '#4361ee', '#f72585', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444'
                ]
            }]
        },
        options: {
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        boxWidth: 12,
                        font: {
                            size: 11
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ': ' + context.parsed + '%';
                        }
                    }
                }
            }
        }
    });

    // Document Type Chart
    const documentTypeChartCtx = document.getElementById('documentTypeChart').getContext('2d');
    const documentTypeChart = new Chart(documentTypeChartCtx, {
        type: 'doughnut',
        data: {
            labels: ['PDF', 'Word', 'PowerPoint', 'Excel', 'Khác'],
            datasets: [{
                data: [45, 25, 15, 10, 5],
                backgroundColor: [
                    '#ef4444', '#4361ee', '#f59e0b', '#10b981', '#8b5cf6'
                ]
            }]
        },
        options: {
            cutout: '60%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        boxWidth: 12,
                        font: {
                            size: 11
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ': ' + context.parsed + '%';
                        }
                    }
                }
            }
        }
    });

    // Revenue Chart
    const revenueChartCtx = document.getElementById('revenueChart').getContext('2d');
    const revenueChart = new Chart(revenueChartCtx, {
        type: 'bar',
        data: {
            labels: ['T11/2022', 'T12/2022', 'T1/2023', 'T2/2023', 'T3/2023', 'T4/2023'],
            datasets: [{
                label: 'Doanh thu',
                data: [85400, 97600, 112500, 98500, 168800, 85600],
                backgroundColor: 'rgba(67, 97, 238, 0.8)',
                borderColor: '#4361ee',
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.dataset.label + ': ' + context.parsed.y.toLocaleString() + ' xu';
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    }
                },
                y: {
                    grid: {
                        color: gridColor
                    },
                    beginAtZero: true,
                    ticks: {
                        callback: function (value) {
                            if (value >= 1000) {
                                return (value / 1000).toFixed(0) + 'K';
                            }
                            return value;
                        }
                    }
                }
            }
        }
    });

    // Chart Type Toggle
    const chartTypeButtons = document.querySelectorAll('.chart-type');

    chartTypeButtons.forEach(button => {
        button.addEventListener('click', function () {
            const parent = this.closest('.analytics-header');
            if (!parent) return;

            const buttons = parent.querySelectorAll('.chart-type');
            buttons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            const chartType = this.getAttribute('data-type');
            const chartContainer = this.closest('.analytics-card').querySelector('.chart-container canvas');

            let chartInstance;
            if (chartContainer.id === 'trafficChart') {
                chartInstance = trafficChart;
            } else if (chartContainer.id === 'revenueChart') {
                chartInstance = revenueChart;
            } else {
                return;
            }

            chartInstance.config.type = chartType;
            chartInstance.update();
        });
    });

    // Date Range Presets
    const timePresets = document.querySelectorAll('.time-preset');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    timePresets.forEach(preset => {
        preset.addEventListener('click', function () {
            timePresets.forEach(p => p.classList.remove('active'));
            this.classList.add('active');

            // Calculate date range based on preset
            const days = parseInt(this.getAttribute('data-days'));
            const endDate = new Date();
            const startDate = new Date();
            startDate.setDate(endDate.getDate() - days);

            // Update date inputs
            startDateInput.value = formatDate(startDate);
            endDateInput.value = formatDate(endDate);
        });
    });

    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    // Handle dark mode toggle for charts
    const darkModeToggle = document.getElementById('darkModeToggle');
    if (darkModeToggle) {
        darkModeToggle.addEventListener('click', function () {
            // Update chart colors when dark mode changes
            setTimeout(() => {
                const isDarkMode = document.body.classList.contains('dark-mode');
                const gridColor = isDarkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)';

                // Update grid colors for each chart
                [trafficChart, userGrowthChart, revenueChart].forEach(chart => {
                    if (chart.config.options.scales.y) {
                        chart.config.options.scales.y.grid.color = gridColor;
                        chart.update();
                    }
                });

                // Update font colors for all charts
                Chart.defaults.color = isDarkMode ? '#94a3b8' : '#64748b';
                [trafficChart, userGrowthChart, categoryDistributionChart, documentTypeChart, revenueChart].forEach(chart => {
                    chart.update();
                });
            }, 100);
        });
    }
});
