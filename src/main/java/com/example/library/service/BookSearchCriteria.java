package com.example.library.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Tham số tìm kiếm sách từ query string. Tự chuẩn hoá giá trị không hợp lệ
 * (trang âm, kích thước quá lớn, cột sắp xếp lạ) về mặc định an toàn.
 */
public record BookSearchCriteria(String keyword, Long categoryId, int page, int size,
                                 String sort, String direction) {

    public static final Set<String> SORTABLE_FIELDS = Set.of("code", "title", "author", "publishYear", "quantity");
    public static final int DEFAULT_SIZE = 5;
    public static final int MAX_SIZE = 50;

    public BookSearchCriteria {
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        page = Math.max(page, 0);
        size = (size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        sort = (sort != null && SORTABLE_FIELDS.contains(sort)) ? sort : "title";
        direction = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
    }

    public Pageable toPageable() {
        Sort.Direction dir = "desc".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Thêm id để thứ tự ổn định khi các giá trị sắp xếp trùng nhau
        return PageRequest.of(page, size, Sort.by(dir, sort).and(Sort.by("id")));
    }

    /** Hướng ngược lại, dùng cho link bấm vào tiêu đề cột. */
    public String reverseDirection() {
        return "asc".equals(direction) ? "desc" : "asc";
    }
}
