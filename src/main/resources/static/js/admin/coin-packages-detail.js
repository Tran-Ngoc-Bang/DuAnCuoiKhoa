/**
 * Coin Packages Detail Page JavaScript
 */

document.addEventListener("DOMContentLoaded", function () {
  console.log("Coin package detail page loaded");

  // Initialize charts if needed
  initializeCharts();

  // Setup action buttons
  setupActionButtons();
});

function initializeCharts() {
  // Revenue chart
  const revenueChart = document.getElementById("revenueChart");
  if (revenueChart) {
    new Chart(revenueChart, {
      type: "line",
      data: {
        labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
        datasets: [
          {
            label: "Doanh thu",
            data: [12, 19, 3, 5, 2, 3],
            borderColor: "rgb(75, 192, 192)",
            tension: 0.1,
          },
        ],
      },
      options: {
        responsive: true,
        scales: {
          y: {
            beginAtZero: true,
          },
        },
      },
    });
  }

  // Usage chart
  const usageChart = document.getElementById("usageChart");
  if (usageChart) {
    new Chart(usageChart, {
      type: "doughnut",
      data: {
        labels: ["Đã sử dụng", "Còn lại"],
        datasets: [
          {
            data: [65, 35],
            backgroundColor: ["rgb(255, 99, 132)", "rgb(54, 162, 235)"],
          },
        ],
      },
      options: {
        responsive: true,
      },
    });
  }
}

function setupActionButtons() {
  // Edit button
  const editBtn = document.querySelector(".edit-package-btn");
  if (editBtn) {
    editBtn.addEventListener("click", function () {
      const packageId = this.getAttribute("data-package-id");
      window.location.href = `/admin/coin-packages/${packageId}/edit`;
    });
  }

  // Delete button
  const deleteBtn = document.querySelector(".delete-package-btn");
  if (deleteBtn) {
    deleteBtn.addEventListener("click", function () {
      const packageId = this.getAttribute("data-package-id");
      const packageName = this.getAttribute("data-package-name");

      if (confirm(`Bạn có chắc chắn muốn xóa gói xu "${packageName}"?`)) {
        // Create form and submit
        const form = document.createElement("form");
        form.method = "POST";
        form.action = `/admin/coin-packages/${packageId}/delete`;

        document.body.appendChild(form);
        form.submit();
      }
    });
  }
}
