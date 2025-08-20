

document.addEventListener('DOMContentLoaded', function () {

	let url = '/admin/statistics/summary';

	fetch(url)
		.then(res => res.json())
		.then(data => {
			document.getElementById('userCount').textContent = data.userCount.toLocaleString();
			document.getElementById('documentCount').textContent = data.documentCount.toLocaleString();

			const revenue = new Intl.NumberFormat('vi-VN').format(data.revenueAmount);
			document.getElementById('revenueAmount').textContent = `${revenue}`;
		})
		.catch(err => {
			console.error("Lỗi khi load thống kê:", err);
		});

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
	let trafficChart;

	function fetchUserTraffic(year, month) {
		fetch(`/admin/statistics/traffic?year=${year}&month=${month}`)
			.then(res => res.json())
			.then(data => {
				const labels = Array.from({ length: data.length }, (_, i) => (i + 1).toString());

				if (trafficChart) {
					trafficChart.data.labels = labels;
					trafficChart.data.datasets[0].data = data;
					trafficChart.update();
				} else {
					trafficChart = new Chart(trafficChartCtx, {
						type: 'line',
						data: {
							labels: labels,
							datasets: [{
								label: 'Người dùng mới',
								data: data,
								borderColor: '#10b981',
								backgroundColor: 'rgba(16, 185, 129, 0.1)',
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
										text: `Ngày (tháng ${month})`
									}
								},
								y: {
									grid: {
										color: '#e5e7eb' // hoặc dùng biến gridColor nếu có
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
				}
			})
			.catch(error => {
				console.error("❌ Lỗi khi tải dữ liệu traffic:", error);
			});
	}

	const today = new Date();
	fetchUserTraffic(today.getFullYear(), today.getMonth() + 1);

	// User Growth
	document.getElementById('userGrowthTimeRange').addEventListener('change', function (event) {
		const selectedDays = event.target.value;
		loadUserGrowthData(selectedDays);
	});

	// Function to load user growth data for a specific time range
	let userGrowthChart; // Declare a variable to hold the chart instance

	function loadUserGrowthData(days = 180) {
		fetch(`/admin/statistics/user-growth?days=${days}`)
			.then(response => response.json())
			.then(data => {
				const labels = Object.keys(data);
				const values = Object.values(data);

				const userGrowthChartCtx = document.getElementById('userGrowthChart').getContext('2d');

				// Destroy the previous chart if it exists
				if (userGrowthChart) {
					userGrowthChart.destroy();
				}

				// Create the new chart
				userGrowthChart = new Chart(userGrowthChartCtx, {
					type: 'line',
					data: {
						labels: labels,
						datasets: [{
							label: 'Người dùng mới',
							data: values,
							borderColor: '#4361ee',
							backgroundColor: 'rgba(67, 97, 238, 0.1)',
							tension: 0.4,
							fill: true
						}]
					},
					options: {
						plugins: {
							legend: { display: false },
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
								grid: { display: false }
							},
							y: {
								grid: { color: '#e0e0e0' },
								beginAtZero: true,
								ticks: {
									callback: function (value) {
										return value >= 1000 ? (value / 1000).toFixed(1) + 'K' : value;
									}
								}
							}
						}
					}
				});
			})
			.catch(error => {
				console.error('Lỗi khi load dữ liệu người dùng:', error);
			});
	}

	loadUserGrowthData(180); // Default to 30 days



	// Category Distribution Chart
	const categoryDistributionChartCtx = document.getElementById('categoryDistributionChart').getContext('2d');

	fetch('/admin/statistics/category-distribution')
		.then(response => response.json())
		.then(data => {
			const labels = Object.keys(data);
			const values = Object.values(data);
			const total = values.reduce((a, b) => a + b, 0);

			const percentages = values.map(v => ((v / total) * 100).toFixed(1));

			new Chart(categoryDistributionChartCtx, {
				type: 'pie',
				data: {
					labels: labels,
					datasets: [{
						data: percentages,
						backgroundColor: [
							'#4361ee', '#f72585', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444',
							'#14b8a6', '#3b82f6', '#f43f5e', '#ec4899' // thêm màu nếu cần
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
								font: { size: 11 }
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
		})
		.catch(error => {
			console.error("Lỗi khi load biểu đồ phân bố danh mục:", error);
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

	const revenueChartCtx = document.getElementById('revenueChart').getContext('2d');

	let revenueChart = new Chart(revenueChartCtx, {
		type: 'bar',
		data: {
			labels: [],
			datasets: [{
				label: 'Doanh thu',
				data: [],
				backgroundColor: 'rgba(67, 97, 238, 0.8)',
				borderColor: '#4361ee',
				borderWidth: 1,
				borderRadius: 4
			}]
		},
		options: {
			plugins: {
				legend: { display: false },
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
					grid: { display: false }
				},
				y: {
					grid: { color: gridColor },
					beginAtZero: true,
					ticks: {
						callback: function (value) {
							if (value >= 1000) return (value / 1000).toFixed(0) + 'K';
							return value;
						}
					}
				}
			}
		}
	});

	document.addEventListener('DOMContentLoaded', () => {
		const dropdownToggle = document.querySelector('.analytics-dropdown .dropdown-toggle');
		const dropdownMenu = document.querySelector('.analytics-dropdown .dropdown-menu');
		const dropdownLabel = dropdownToggle.querySelector('span');

		dropdownToggle.addEventListener('click', (e) => {
			e.stopPropagation();
			dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
		});

		dropdownMenu.querySelectorAll('li').forEach(item => {
			item.addEventListener('click', () => {
				dropdownLabel.textContent = item.textContent;

				dropdownMenu.querySelectorAll('li').forEach(li => li.classList.remove('active'));
				item.classList.add('active');

				dropdownMenu.style.display = 'none';

				console.log('Chọn khoảng thời gian:', item.getAttribute('data-months'), 'tháng');
			});
		});

		document.addEventListener('click', () => {
			dropdownMenu.style.display = 'none';
		});
	});

	document.getElementById('revenueTimeRange').addEventListener('change', function (event) {
		const days = event.target.value;
		console.log(days)
		loadRevenueData(days);
	});

	function loadRevenueData(days = 30) {
		fetch(`/admin/statistics/revenue-monthly?days=${days}`)
			.then(res => res.json())
			.then(data => {
				revenueChart.data.labels = data.labels;
				revenueChart.data.datasets[0].data = data.data;
				revenueChart.update();

				document.querySelector('.chart-stats .stat-item:nth-child(1) .stat-value')
					.textContent = new Intl.NumberFormat('vi-VN').format(data.total) + ' xu';

				document.querySelector('.chart-stats .stat-item:nth-child(2) .stat-value')
					.textContent = new Intl.NumberFormat('vi-VN').format(data.max) + ' xu';

				document.querySelector('.chart-stats .stat-item:nth-child(2) .stat-label')
					.textContent = `Doanh thu cao nhất (${data.maxLabel})`;

				document.querySelector('.chart-stats .stat-item:nth-child(3) .stat-value')
					.textContent = new Intl.NumberFormat('vi-VN').format(data.average) + ' xu';
			})
			.catch(err => {
				console.error('Lỗi khi load doanh thu:', err);
			});
	}

	loadRevenueData(30);


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


	document.querySelector('.apply-filter-btn').addEventListener('click', () => {
		const startDate = document.getElementById('startDate').value;
		const endDate = document.getElementById('endDate').value;

		if (!startDate || !endDate) return alert("Vui lòng chọn khoảng ngày.");

		fetch(`/admin/statistics/summary?start=${startDate}&end=${endDate}`)
			.then(res => res.json())
			.then(data => {
				document.getElementById('userCount').textContent = data.userCount.toLocaleString();
				document.getElementById('documentCount').textContent = data.documentCount.toLocaleString();

				const revenue = new Intl.NumberFormat('vi-VN').format(data.revenueAmount);
				document.getElementById('revenueAmount').textContent = `${revenue}`;
			})
			.catch(err => {
				console.error("❌ Lỗi khi load thống kê:", err);
			});
	});


	document.querySelector('.time-preset[data-days="30"]').click();

	document.querySelectorAll('.time-preset').forEach(btn => {
		btn.addEventListener('click', () => {
			document.querySelectorAll('.time-preset').forEach(b => b.classList.remove('active'));
			btn.classList.add('active');

			const days = parseInt(btn.getAttribute('data-days'));
			const end = new Date();
			const start = new Date();
			start.setDate(end.getDate() - days + 1);

			document.getElementById('startDate').value = formatDate(start);
			document.getElementById('endDate').value = formatDate(end);
		});
	});

	function formatDate(date) {
		return date.toISOString().split('T')[0];
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
