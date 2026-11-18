package com.example.library.service;

import com.example.library.dto.CategoryForm;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("thể loại", id));
    }

    public long countBooks(Long categoryId) {
        return bookRepository.countByCategoryId(categoryId);
    }

    @Transactional
    public Category create(CategoryForm form) {
        String name = form.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("name", "Tên thể loại '" + name + "' đã tồn tại");
        }
        return categoryRepository.save(new Category(name));
    }

    @Transactional
    public Category update(Long id, CategoryForm form) {
        Category category = findById(id);
        String name = form.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessException("name", "Tên thể loại '" + name + "' đã tồn tại");
        }
        category.rename(name);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        long bookCount = bookRepository.countByCategoryId(id);
        if (bookCount > 0) {
            throw new BusinessException("Không thể xoá thể loại '" + category.getName()
                    + "' vì còn " + bookCount + " sách thuộc thể loại này");
        }
        categoryRepository.delete(category);
    }
}
