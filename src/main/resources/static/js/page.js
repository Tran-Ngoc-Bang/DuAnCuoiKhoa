
document.addEventListener("DOMContentLoaded", function () {
    const itemsPerPage = 4;
    const container = document.getElementById("docContainer");
    const items = Array.from(container.getElementsByClassName("doc-card-item"));
    const pagination = document.getElementById("pagination");

    let currentPage = 1;
    const totalPages = Math.ceil(items.length / itemsPerPage);

    function showPage(page) {
        // Ẩn tất cả
        items.forEach((item, index) => {
            item.style.display = "none";
        });

        // Hiện trang hiện tại
        const start = (page - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        items.slice(start, end).forEach(item => {
            item.style.display = ""; // hoặc item.style.removeProperty("display");
        });

        updatePagination(page);
    }

    function updatePagination(activePage) {
        pagination.innerHTML = "";

        for (let i = 1; i <= totalPages; i++) {
            const btn = document.createElement("button");
            btn.textContent = i;
            btn.className = "pagination-btn";
            if (i === activePage) btn.classList.add("active");

            btn.addEventListener("click", () => {
                currentPage = i;
                showPage(currentPage);
                setTimeout(() => {
                    // Gợi ý reflow lại bố cục nếu dùng layout động
                    container.offsetHeight; // Trigger reflow
                }, 0);
            });

            pagination.appendChild(btn);
        }
    }

    showPage(currentPage);
});




