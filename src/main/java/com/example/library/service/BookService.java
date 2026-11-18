package com.example.library.service;

import com.example.library.dto.BookForm;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowRepository borrowRepository;

    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository,
                       BorrowRepository borrowRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.borrowRepository = borrowRepository;
    }

    public Page<Book> search(BookSearchCriteria criteria) {
        return bookRepository.search(criteria.keyword(), criteria.categoryId(), criteria.toPageable());
    }

    public Book findById(Long id) {
        return bookRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sách", id));
    }

    public List<Book> findAvailable() {
        return bookRepository.findByQuantityGreaterThanOrderByTitleAsc(0);
    }

    @Transactional
    public Book create(BookForm form) {
        String code = normalizeCode(form.getCode());
        if (bookRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("code", "Mã sách '" + code + "' đã tồn tại");
        }
        Category category = loadCategory(form.getCategoryId());
        Book book = new Book(code, form.getTitle(), form.getAuthor(),
                form.getPublishYear(), form.getQuantity(), category);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, BookForm form) {
        Book book = findById(id);
        String code = normalizeCode(form.getCode());
        if (bookRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("code", "Mã sách '" + code + "' đã tồn tại");
        }
        Category category = loadCategory(form.getCategoryId());
        book.updateDetails(code, form.getTitle(), form.getAuthor(),
                form.getPublishYear(), form.getQuantity(), category);
        return book;
    }

    @Transactional
    public void delete(Long id) {
        Book book = findById(id);
        if (borrowRepository.existsByBookId(id)) {
            throw new BusinessException("Không thể xoá sách '" + book.getTitle()
                    + "' vì đã có lịch sử mượn");
        }
        bookRepository.delete(book);
    }

    private Category loadCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("categoryId", "Thể loại không tồn tại"));
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
