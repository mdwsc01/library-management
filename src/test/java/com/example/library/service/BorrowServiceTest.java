package com.example.library.service;

import com.example.library.dto.BorrowForm;
import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

    @Mock
    private BorrowRepository borrowRepository;
    @Mock
    private BookRepository bookRepository;

    private BorrowService borrowService;
    private Book book;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        borrowService = new BorrowService(borrowRepository, bookRepository, fixedClock);

        book = new Book("IT001", "Clean Code", "Robert C. Martin", 2008, 2, new Category("CNTT"));
        ReflectionTestUtils.setField(book, "id", 1L);
    }

    private BorrowForm form(String email, LocalDate dueDate) {
        BorrowForm form = new BorrowForm();
        form.setBookId(1L);
        form.setBorrowerName("Nguyễn Văn An");
        form.setEmail(email);
        form.setDueDate(dueDate);
        return form;
    }

    @Test
    @DisplayName("Mượn thành công: trừ kho, ngày mượn = hôm nay, email chuẩn hoá chữ thường")
    void borrow_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndEmailAndReturnDateIsNull(1L, "an@example.com")).thenReturn(false);
        when(bookRepository.decrementQuantity(1L)).thenReturn(1);
        when(borrowRepository.saveAndFlush(any(Borrow.class))).thenAnswer(inv -> inv.getArgument(0));

        Borrow result = borrowService.borrow(form("  An@Example.com ", TODAY.plusDays(7)));

        assertThat(result.getEmail()).isEqualTo("an@example.com");
        assertThat(result.getBorrowDate()).isEqualTo(TODAY);
        assertThat(result.getDueDate()).isEqualTo(TODAY.plusDays(7));
        assertThat(result.isReturned()).isFalse();
        verify(bookRepository).decrementQuantity(1L);
    }

    @Test
    @DisplayName("Hết sách thì không cho mượn")
    void borrow_outOfStock_throws() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndEmailAndReturnDateIsNull(1L, "an@example.com")).thenReturn(false);
        when(bookRepository.decrementQuantity(1L)).thenReturn(0); // không còn cuốn nào

        assertThatThrownBy(() -> borrowService.borrow(form("an@example.com", TODAY.plusDays(7))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã hết")
                .extracting("field").isEqualTo("bookId");
        verify(borrowRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Một email không được mượn trùng cùng một cuốn khi chưa trả")
    void borrow_duplicateActiveBorrow_throws() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndEmailAndReturnDateIsNull(1L, "an@example.com")).thenReturn(true);

        assertThatThrownBy(() -> borrowService.borrow(form("AN@example.com", TODAY.plusDays(7))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chưa trả")
                .extracting("field").isEqualTo("email");
        verify(bookRepository, never()).decrementQuantity(anyLong());
    }

    @Test
    @DisplayName("Race condition: unique key ở DB chặn mượn trùng -> chuyển thành BusinessException")
    void borrow_concurrentDuplicate_isTranslated() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndEmailAndReturnDateIsNull(1L, "an@example.com")).thenReturn(false);
        when(bookRepository.decrementQuantity(1L)).thenReturn(1);
        when(borrowRepository.saveAndFlush(any(Borrow.class)))
                .thenThrow(new DataIntegrityViolationException("uk_borrow_active"));

        assertThatThrownBy(() -> borrowService.borrow(form("an@example.com", TODAY.plusDays(7))))
                .isInstanceOf(BusinessException.class)
                .extracting("field").isEqualTo("email");
    }

    @Test
    @DisplayName("Hạn trả trước hôm nay bị từ chối")
    void borrow_dueDateInPast_throws() {
        assertThatThrownBy(() -> borrowService.borrow(form("an@example.com", TODAY.minusDays(1))))
                .isInstanceOf(BusinessException.class)
                .extracting("field").isEqualTo("dueDate");
        verify(bookRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Mượn sách không tồn tại")
    void borrow_bookNotFound_throws() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrow(form("an@example.com", TODAY.plusDays(3))))
                .isInstanceOf(BusinessException.class)
                .extracting("field").isEqualTo("bookId");
    }

    @Test
    @DisplayName("Trả sách: ghi ngày trả = hôm nay và cộng lại kho")
    void returnBook_success() {
        Borrow borrow = new Borrow(book, "An", "an@example.com", TODAY.minusDays(5), TODAY.plusDays(2));
        when(borrowRepository.findWithBookById(10L)).thenReturn(Optional.of(borrow));

        Borrow result = borrowService.returnBook(10L);

        assertThat(result.getReturnDate()).isEqualTo(TODAY);
        assertThat(result.isReturned()).isTrue();
        verify(bookRepository).incrementQuantity(1L);
    }

    @Test
    @DisplayName("Không thể trả một lượt mượn đã trả")
    void returnBook_alreadyReturned_throws() {
        Borrow borrow = new Borrow(book, "An", "an@example.com", TODAY.minusDays(5), TODAY.plusDays(2));
        borrow.markReturned(TODAY.minusDays(1));
        when(borrowRepository.findWithBookById(10L)).thenReturn(Optional.of(borrow));

        assertThatThrownBy(() -> borrowService.returnBook(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã được trả");
        verify(bookRepository, never()).incrementQuantity(anyLong());
    }

    @Test
    @DisplayName("Trả lượt mượn không tồn tại -> 404")
    void returnBook_notFound_throws() {
        when(borrowRepository.findWithBookById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.returnBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Đánh dấu quá hạn: chưa trả và đã qua hạn trả")
    void overdue_detection() {
        Borrow overdue = new Borrow(book, "An", "an@example.com", TODAY.minusDays(20), TODAY.minusDays(3));
        Borrow dueToday = new Borrow(book, "Bình", "binh@example.com", TODAY.minusDays(7), TODAY);
        Borrow returnedLate = new Borrow(book, "Cường", "cuong@example.com", TODAY.minusDays(20), TODAY.minusDays(3));
        returnedLate.markReturned(TODAY.minusDays(1));

        assertThat(overdue.isOverdue(TODAY)).isTrue();
        assertThat(overdue.overdueDays(TODAY)).isEqualTo(3);
        assertThat(dueToday.isOverdue(TODAY)).isFalse();
        assertThat(returnedLate.isOverdue(TODAY)).isFalse();
    }
}
