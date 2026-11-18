package com.example.library.dto;

/** Dòng hiển thị trên trang danh sách thể loại (tạo bằng constructor expression trong JPQL). */
public class CategoryView {

    private final Long id;
    private final String name;
    private final Long bookCount;

    public CategoryView(Long id, String name, Long bookCount) {
        this.id = id;
        this.name = name;
        this.bookCount = bookCount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getBookCount() {
        return bookCount;
    }

    public boolean isDeletable() {
        return bookCount == 0;
    }
}
