
window.addEventListener("load", function () {
    const overlay = document.getElementById("loadingOverlay");
    overlay.style.opacity = 0;
    overlay.style.transition = "opacity 3s ease";
    setTimeout(() => {
        overlay.style.display = "none";
    }, 500);
});




function showLocalLoading() {
    document.getElementById("localLoading").style.display = "flex";
}

function hideLocalLoading() {
    document.getElementById("localLoading").style.display = "none";
}

function showPage(page) {
    showLocalLoading(); // Bắt đầu loading

    setTimeout(() => {
        // Ẩn toàn bộ
        items.forEach((item, index) => {
            item.style.display = "none";
        });

        // Hiện trang hiện tại
        const start = (page - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        items.slice(start, end).forEach(item => {
            item.style.display = "block";
        });

        updatePagination(page);
        hideLocalLoading(); // Kết thúc loading
    }, 400); // 400ms là hiệu ứng mượt
}
