package com.example.library.dto;

import com.example.library.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dữ liệu form thêm/sửa thể loại. */
public class CategoryForm {

    private Long id;

    @NotBlank(message = "Tên thể loại không được để trống")
    @Size(max = 100, message = "Tên thể loại tối đa 100 ký tự")
    private String name;

    public static CategoryForm from(Category category) {
        CategoryForm form = new CategoryForm();
        form.setId(category.getId());
        form.setName(category.getName());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
