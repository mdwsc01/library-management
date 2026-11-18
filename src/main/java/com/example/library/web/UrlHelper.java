package com.example.library.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Dựng URL giữ nguyên các tham số đang có (từ khoá, bộ lọc...) và chỉ thay trang / sắp xếp.
 * Dùng trong Thymeleaf: {@code ${@urls.page(2)}}.
 */
@Component("urls")
public class UrlHelper {

    public String page(int page) {
        return toRelative(current().replaceQueryParam("page", page));
    }

    public String sort(String field, String direction) {
        return toRelative(current()
                .replaceQueryParam("sort", field)
                .replaceQueryParam("dir", direction)
                .replaceQueryParam("page", 0));
    }

    private UriComponentsBuilder current() {
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }

    private String toRelative(UriComponentsBuilder builder) {
        UriComponents uri = builder.build();
        String query = uri.getQuery();
        return uri.getPath() + (query == null || query.isEmpty() ? "" : "?" + query);
    }
}
