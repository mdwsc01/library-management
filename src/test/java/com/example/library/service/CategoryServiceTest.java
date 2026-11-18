package com.example.library.service;

import com.example.library.dto.CategoryForm;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryForm form(String name) {
        CategoryForm form = new CategoryForm();
        form.setName(name);
        return form;
    }

    @Test
    @DisplayName("Tên thể loại không được trùng (không phân biệt hoa thường)")
    void create_duplicateName_throws() {
        when(categoryRepository.existsByNameIgnoreCase("văn học")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(form("  văn học ")))
                .isInstanceOf(BusinessException.class)
                .extracting("field").isEqualTo("name");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sửa thể loại: đổi tên khi không trùng với thể loại khác")
    void update_success() {
        Category category = new Category("Van hoc");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Văn học", 2L)).thenReturn(false);

        Category result = categoryService.update(2L, form("Văn học"));

        assertThat(result.getName()).isEqualTo("Văn học");
    }

    @Test
    @DisplayName("Không xoá được thể loại còn sách")
    void delete_categoryWithBooks_throws() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category("CNTT")));
        when(bookRepository.countByCategoryId(1L)).thenReturn(4L);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("4 sách");
        verify(categoryRepository, never()).delete(any());
    }
}
