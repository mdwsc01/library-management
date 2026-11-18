# Ảnh chụp màn hình

Chạy app (`mvn spring-boot:run`), chụp các trang sau và lưu đúng tên file để README hiển thị:

| File | Cách tạo |
|------|----------|
| `01-books-list.png` | http://localhost:8080/books — gõ từ khoá, chọn thể loại, bấm tiêu đề cột để sắp xếp |
| `02-book-form-errors.png` | /books/new — để trống các ô, nhập mã `IT001` (đã tồn tại), năm `3000`, bấm Lưu |
| `03-delete-confirm.png` | Bấm nút thùng rác ở một dòng sách → chụp hộp thoại xác nhận |
| `04-categories.png` | http://localhost:8080/categories |
| `05-borrow-duplicate.png` | /borrows/new — chọn *Clean Code*, email `an.nguyen@example.com` (đang mượn trong dữ liệu mẫu) |
| `06-borrows-overdue.png` | http://localhost:8080/borrows — dòng của Trần Thị Bình được đánh dấu quá hạn |
| `07-flash-success.png` | Thêm một sách hợp lệ → chụp thông báo xanh |
| `08-not-found.png` | http://localhost:8080/books/9999/edit hoặc http://localhost:8080/khong-co |
