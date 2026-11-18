// Xác nhận trước khi submit các form có thuộc tính data-confirm (xoá, trả sách)
document.addEventListener('submit', function (event) {
    const form = event.target;
    const message = form.getAttribute('data-confirm');
    if (message && !window.confirm(message)) {
        event.preventDefault();
    }
});

// Tự đóng thông báo thành công sau 5 giây
document.querySelectorAll('.alert-success').forEach(function (el) {
    setTimeout(function () {
        if (window.bootstrap) {
            bootstrap.Alert.getOrCreateInstance(el).close();
        }
    }, 5000);
});
