package com.example.library.dto;

import com.example.library.entity.Book;
import com.example.library.validation.NotFutureYear;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu form thêm/sửa sách. */
public class BookForm {

    private Long id;

    @NotBlank(message = "Mã sách không được để trống")
    @Size(max = 20, message = "Mã sách tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "Mã sách chỉ gồm chữ, số, '-' hoặc '_'")
    private String code;

    @NotBlank(message = "Tên sách không được để trống")
    @Size(max = 255, message = "Tên sách tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Size(max = 150, message = "Tác giả tối đa 150 ký tự")
    private String author;

    @NotNull(message = "Năm xuất bản không được để trống")
    @Min(value = 1000, message = "Năm xuất bản phải từ 1000 trở lên")
    @NotFutureYear
    private Integer publishYear;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    @Max(value = 10000, message = "Số lượng tối đa 10000")
    private Integer quantity;

    @NotNull(message = "Vui lòng chọn thể loại")
    private Long categoryId;

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setCode(book.getCode());
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setPublishYear(book.getPublishYear());
        form.setQuantity(book.getQuantity());
        form.setCategoryId(book.getCategory().getId());
        return form;
    }

    public boolean isNew() {
        return id == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
