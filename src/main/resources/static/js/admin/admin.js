/**
 * Admin Common JavaScript
 */

document.addEventListener("DOMContentLoaded", function () {
  console.log("Admin panel loaded");

  // Dark mode toggle
  const darkModeToggle = document.getElementById("darkModeToggle");
  if (darkModeToggle) {
    darkModeToggle.addEventListener("click", function () {
      document.body.classList.toggle("dark-mode");
      localStorage.setItem(
        "darkMode",
        document.body.classList.contains("dark-mode")
      );
    });

    // Load saved dark mode preference
    if (localStorage.getItem("darkMode") === "true") {
      document.body.classList.add("dark-mode");
    }
  }

  // Submenu toggle
  const submenuItems = document.querySelectorAll(".has-submenu > a");

  submenuItems.forEach((item) => {
    item.addEventListener("click", function (e) {
      e.preventDefault();

      const parent = this.parentElement;

      // Close other open submenus
      document.querySelectorAll(".has-submenu.open").forEach((openItem) => {
        if (openItem !== parent) {
          openItem.classList.remove("open");
        }
      });

      // Toggle current submenu
      parent.classList.toggle("open");
    });
  });
});
