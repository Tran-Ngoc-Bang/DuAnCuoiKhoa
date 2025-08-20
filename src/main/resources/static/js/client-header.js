// Client Header JavaScript
document.addEventListener("DOMContentLoaded", function () {
  // Prevent conflicts with other scripts
  try {
    // User Profile Dropdown
    const adminProfile = document.querySelector(".admin-profile");
    const dropdownMenu = document.querySelector(".dropdown-menu");

    if (adminProfile && dropdownMenu) {
      let isDropdownOpen = false;

      // Toggle dropdown on click
      adminProfile.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();

        isDropdownOpen = !isDropdownOpen;

        if (isDropdownOpen) {
          adminProfile.classList.add("active");
        } else {
          adminProfile.classList.remove("active");
        }
      });

      // Close dropdown when clicking outside
      document.addEventListener("click", function (e) {
        if (!adminProfile.contains(e.target) && isDropdownOpen) {
          adminProfile.classList.remove("active");
          isDropdownOpen = false;
        }
      });

      // Prevent dropdown from closing when clicking inside dropdown menu
      dropdownMenu.addEventListener("click", function (e) {
        e.stopPropagation();
      });

      // Close dropdown when clicking on dropdown links (except logout form)
      dropdownMenu.querySelectorAll("a.dropdown-item").forEach((link) => {
        link.addEventListener("click", function () {
          adminProfile.classList.remove("active");
          isDropdownOpen = false;
        });
      });
    }

    // Mobile Menu Toggle
    const mobileMenuBtn = document.getElementById("mobileMenuBtn");
    const mobileMenu = document.getElementById("mobileMenu");

    if (mobileMenuBtn && mobileMenu) {
      mobileMenuBtn.addEventListener("click", function () {
        mobileMenu.classList.toggle("active");
        mobileMenuBtn.classList.toggle("active");
      });
    }

    // Dark Mode Toggle
    const darkModeToggle = document.getElementById("darkModeToggle");
    const body = document.body;

    if (darkModeToggle) {
      // Check for saved dark mode preference
      const isDarkMode = localStorage.getItem("darkMode") === "true";
      if (isDarkMode) {
        body.classList.add("dark-mode");
      }

      darkModeToggle.addEventListener("click", function () {
        body.classList.toggle("dark-mode");
        const isDark = body.classList.contains("dark-mode");
        localStorage.setItem("darkMode", isDark);
      });
    }

    // Search functionality
    const searchInput = document.querySelector(".hero-search input");
    const searchBtn = document.querySelector(".search-btn");

    if (searchInput && searchBtn) {
      searchBtn.addEventListener("click", function (e) {
        e.preventDefault();
        const query = searchInput.value.trim();
        if (query) {
          window.location.href = `/search?q=${encodeURIComponent(query)}`;
        }
      });

      searchInput.addEventListener("keypress", function (e) {
        if (e.key === "Enter") {
          e.preventDefault();
          const query = this.value.trim();
          if (query) {
            window.location.href = `/search?q=${encodeURIComponent(query)}`;
          }
        }
      });
    }

    // Mobile search
    const mobileSearchInput = document.querySelector(".mobile-search input");
    const mobileSearchBtn = document.querySelector(".mobile-search button");

    if (mobileSearchInput && mobileSearchBtn) {
      mobileSearchBtn.addEventListener("click", function (e) {
        e.preventDefault();
        const query = mobileSearchInput.value.trim();
        if (query) {
          window.location.href = `/search?q=${encodeURIComponent(query)}`;
        }
      });

      mobileSearchInput.addEventListener("keypress", function (e) {
        if (e.key === "Enter") {
          e.preventDefault();
          const query = this.value.trim();
          if (query) {
            window.location.href = `/search?q=${encodeURIComponent(query)}`;
          }
        }
      });
    }
  } catch (error) {
    console.error("Error in client-header.js:", error);
  }
});

// Close mobile menu when clicking outside
document.addEventListener("click", function (e) {
  const mobileMenu = document.getElementById("mobileMenu");
  const mobileMenuBtn = document.getElementById("mobileMenuBtn");

  if (mobileMenu && mobileMenuBtn) {
    if (!mobileMenu.contains(e.target) && !mobileMenuBtn.contains(e.target)) {
      mobileMenu.classList.remove("active");
      mobileMenuBtn.classList.remove("active");
    }
  }
});

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
  anchor.addEventListener("click", function (e) {
    e.preventDefault();
    const target = document.querySelector(this.getAttribute("href"));
    if (target) {
      target.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }
  });
});
