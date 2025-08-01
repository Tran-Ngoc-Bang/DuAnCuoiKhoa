/**
 * Toast Notification System
 */

function showToast(message, type = "info", duration = 5000) {
  // Create toast container if it doesn't exist
  let toastContainer = document.getElementById("toastContainer");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.id = "toastContainer";
    toastContainer.className = "toast-container";
    document.body.appendChild(toastContainer);
  }

  // Create toast element
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
        <div class="toast-content">
            <i class="fas fa-${getToastIcon(type)}"></i>
            <span>${message}</span>
        </div>
        <button class="toast-close">
            <i class="fas fa-times"></i>
        </button>
    `;

  // Add to container
  toastContainer.appendChild(toast);

  // Show toast
  setTimeout(() => {
    toast.classList.add("show");
  }, 100);

  // Auto remove
  setTimeout(() => {
    removeToast(toast);
  }, duration);

  // Close button
  toast.querySelector(".toast-close").addEventListener("click", () => {
    removeToast(toast);
  });

  return toast;
}

function removeToast(toast) {
  toast.classList.remove("show");
  setTimeout(() => {
    if (toast.parentNode) {
      toast.remove();
    }
  }, 300);
}

function getToastIcon(type) {
  switch (type) {
    case "success":
      return "check-circle";
    case "error":
      return "exclamation-circle";
    case "warning":
      return "exclamation-triangle";
    default:
      return "info-circle";
  }
}

// Show flash messages from server
document.addEventListener("DOMContentLoaded", function () {
  // Check for flash messages in URL parameters or data attributes
  const urlParams = new URLSearchParams(window.location.search);
  const successMessage = urlParams.get("success");
  const errorMessage = urlParams.get("error");

  if (successMessage) {
    showToast(decodeURIComponent(successMessage), "success");
  }

  if (errorMessage) {
    showToast(decodeURIComponent(errorMessage), "error");
  }
});
