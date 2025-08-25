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

  document.getElementById('chartTimeRange').addEventListener('change', function () {
    const timeRange = this.value;

    fetch(`/account/stats?range=${timeRange}`)
      .then(response => response.json())
      .then(data => {
        chart.updateOptions({
          xaxis: {
            categories: data.categories
          }
        });

        chart.updateSeries([
          {
            name: 'Lượt tải xuống',
            data: data.downloads
          },
          {
            name: 'Lượt xem',
            data: data.views
          },
          {
            name: 'Xu nhận được',
            data: data.coins
          }
        ]);
      });
  });


});