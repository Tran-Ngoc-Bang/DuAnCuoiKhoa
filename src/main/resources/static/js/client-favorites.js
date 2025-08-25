document.addEventListener('DOMContentLoaded', function () {
  // Chart initialization
  const chartOptions = {
    series: [
      {
        name: 'Lượt tải xuống',
        data: [12, 19, 13, 14, 22, 27, 20]
      },
      {
        name: 'Lượt xem',
        data: [45, 52, 38, 45, 65, 71, 60]
      },
      {
        name: 'Xu nhận được',
        data: [5, 8, 7, 10, 12, 15, 10]
      }
    ],
    chart: {
      height: 350,
      type: 'area',
      fontFamily: 'Nunito, sans-serif',
      toolbar: {
        show: false
      }
    },
    colors: ['#4361ee', '#10b981', '#f59e0b'],
    dataLabels: {
      enabled: false
    },
    stroke: {
      curve: 'smooth',
      width: 2
    },
    fill: {
      type: 'gradient',
      gradient: {
        shadeIntensity: 1,
        opacityFrom: 0.7,
        opacityTo: 0.3,
        stops: [0, 90, 100]
      }
    },
    grid: {
      xaxis: {
        lines: {
          show: true
        }
      },
      yaxis: {
        lines: {
          show: true
        }
      },
      padding: {
        top: 0,
        right: 0,
        bottom: 0,
        left: 10
      }
    },
    xaxis: {
      categories: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'],
      labels: {
        style: {
          colors: '#6b7280',
          fontSize: '12px',
          fontFamily: 'Nunito, sans-serif'
        }
      }
    },
    yaxis: {
      labels: {
        style: {
          colors: '#6b7280',
          fontSize: '12px',
          fontFamily: 'Nunito, sans-serif'
        },
        formatter: function (value) {
          return value.toFixed(0);
        }
      }
    },
    tooltip: {
      x: {
        format: 'dd/MM/yy'
      },
      shared: true
    },
    legend: {
      position: 'top',
      horizontalAlign: 'right',
      offsetY: -15,
      markers: {
        width: 12,
        height: 12,
        radius: 12
      },
      itemMargin: {
        horizontal: 10
      }
    }
  };

  const chart = new ApexCharts(document.querySelector("#activityChart"), chartOptions);
  chart.render();

  // Chart time range selector
  document.getElementById('chartTimeRange').addEventListener('change', function () {
    const timeRange = this.value;
    let newData = [];
    let newCategories = [];

    if (timeRange === 'day') {
      newCategories = ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'];
      newData = [
        [3, 5, 2, 4, 8, 10, 5],
        [10, 15, 8, 12, 20, 25, 15],
        [1, 3, 2, 3, 4, 5, 3]
      ];
    } else if (timeRange === 'week') {
      newCategories = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
      newData = [
        [12, 19, 13, 14, 22, 27, 20],
        [45, 52, 38, 45, 65, 71, 60],
        [5, 8, 7, 10, 12, 15, 10]
      ];
    } else if (timeRange === 'month') {
      newCategories = ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4'];
      newData = [
        [50, 65, 80, 95],
        [150, 180, 200, 220],
        [25, 30, 40, 45]
      ];
    } else if (timeRange === 'year') {
      newCategories = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
      newData = [
        [100, 120, 150, 180, 200, 220, 240, 250, 270, 290, 310, 330],
        [300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850],
        [50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160]
      ];
    }

    chart.updateOptions({
      xaxis: {
        categories: newCategories
      }
    });

    chart.updateSeries([
      {
        name: 'Lượt tải xuống',
        data: newData[0]
      },
      {
        name: 'Lượt xem',
        data: newData[1]
      },
      {
        name: 'Xu nhận được',
        data: newData[2]
      }
    ]);
  });

  // Document action buttons
  const actionButtons = document.querySelectorAll('.action-button');
  actionButtons.forEach(button => {
    button.addEventListener('click', function () {
      const action = this.getAttribute('title');
      const documentCard = this.closest('.document-card');
      const documentTitle = documentCard.querySelector('.document-title').textContent;

      if (action === 'Xem chi tiết') {
        alert(`Đang mở tài liệu: ${documentTitle}`);
      } else if (action === 'Chỉnh sửa') {
        alert(`Đang chỉnh sửa tài liệu: ${documentTitle}`);
      } else if (action === 'Xóa') {
        if (confirm(`Bạn có chắc chắn muốn xóa tài liệu: ${documentTitle}?`)) {
          alert(`Đã xóa tài liệu: ${documentTitle}`);
        }
      }
    });
  });

  // Initializing the user menu dropdown
  const userMenuTrigger = document.querySelector('.user-menu-trigger');
  const userDropdown = document.querySelector('.user-dropdown');

  if (userMenuTrigger && userDropdown) {
    userMenuTrigger.addEventListener('click', function (e) {
      e.stopPropagation();
      userDropdown.classList.toggle('show');
    });

    document.addEventListener('click', function (e) {
      if (!userMenuTrigger.contains(e.target) && !userDropdown.contains(e.target)) {
        userDropdown.classList.remove('show');
      }
    });
  }
});