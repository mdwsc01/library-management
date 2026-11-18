package com.example.library.service;

import com.example.library.dto.BookForm;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private BookService bookService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Công nghệ thông tin");
        ReflectionTestUtils.setField(category, "id", 1L);
    }

    private BookForm validForm() {
        BookForm form = new BookForm();
        form.setCode(" it010 ");
        form.setTitle("  Head First Java ");
        form.setAuthor("Kathy Sierra");
        form.setPublishYear(2022);
        form.setQuantity(3);
        form.setCategoryId(1L);
        return form;
    }

    @Test
    @DisplayName("Thêm sách thành công: mã sách được viết hoa, chuỗi được trim")
    void create_success() {
        when(bookRepository.existsByCodeIgnoreCase("IT010")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book book = bookService.create(validForm());

        assertThat(book.getCode()).isEqualTo("IT010");
        assertThat(book.getTitle()).isEqualTo("Head First Java");
        assertThat(book.getCategory()).isSameAs(category);
    }

    @Test
    @DisplayName("Thêm sách trùng mã -> lỗi gắn vào trường code")
    void create_duplicateCode_throws() {
        when(bookRepository.existsByCodeIgnoreCase("IT010")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(validForm()))
                .isInstanceOf(BusinessException.class)
                .extracting("field").isEqualTo("code");
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sửa sách không tồn tại -> ResourceNotFoundException (trang 404)")
    void update_notFound_throws() {
        when(bookRepository.findWithCategoryById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(42L, validForm()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Không xoá được sách đã có lịch sử mượn")
    void delete_withBorrowHistory_throws() {
        Book book = new Book("IT001", "Clean Code", "Robert C. Martin", 2008, 1, category);
        when(bookRepository.findWithCategoryById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookId(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("lịch sử mượn");
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Tham số tìm kiếm lạ được chuẩn hoá về giá trị an toàn")
    void searchCriteria_sanitizesInput() {
        BookSearchCriteria criteria = new BookSearchCriteria("   ", null, -3, 1000, "password; drop", "xyz");

        assertThat(criteria.keyword()).isNull();
        assertThat(criteria.page()).isZero();
        assertThat(criteria.size()).isEqualTo(BookSearchCriteria.MAX_SIZE);
        assertThat(criteria.sort()).isEqualTo("title");
        assertThat(criteria.direction()).isEqualTo("asc");

        Pageable pageable = criteria.toPageable();
        assertThat(pageable.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
