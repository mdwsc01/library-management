package com.example.library.service;

import com.example.library.dto.BorrowForm;
import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class BorrowService {

    private static final int PAGE_SIZE = 10;

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final Clock clock;

    public BorrowService(BorrowRepository borrowRepository, BookRepository bookRepository, Clock clock) {
        this.borrowRepository = borrowRepository;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public Page<Borrow> findByStatus(BorrowStatus status, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        return switch (status) {
            case ACTIVE -> borrowRepository.findActive(pageable);
            case OVERDUE -> borrowRepository.findOverdue(today(), pageable);
            case RETURNED -> borrowRepository.findReturned(pageable);
        };
    }

    public long countActive() {
        return borrowRepository.countByReturnDateIsNull();
    }

    public long countOverdue() {
        return borrowRepository.countByReturnDateIsNullAndDueDateBefore(today());
    }

    /**
     * Mượn sách:
     * <ul>
     *   <li>hạn trả không được trước hôm nay</li>
     *   <li>một email không được mượn trùng một cuốn khi chưa trả</li>
     *   <li>hết sách (quantity = 0) thì không cho mượn — trừ kho bằng UPDATE có điều kiện</li>
     * </ul>
     */
    @Transactional
    public Borrow borrow(BorrowForm form) {
        LocalDate today = today();
        if (form.getDueDate().isBefore(today)) {
            throw new BusinessException("dueDate", "Hạn trả không được trước hôm nay");
        }

        Book book = bookRepository.findById(form.getBookId())
                .orElseThrow(() -> new BusinessException("bookId", "Sách không tồn tại"));
        String email = Borrow.normalizeEmail(form.getEmail());

        if (borrowRepository.existsByBookIdAndEmailAndReturnDateIsNull(book.getId(), email)) {
            throw new BusinessException("email",
                    "Email " + email + " đang mượn cuốn '" + book.getTitle() + "' và chưa trả");
        }
        if (bookRepository.decrementQuantity(book.getId()) == 0) {
            throw new BusinessException("bookId", "Sách '" + book.getTitle() + "' đã hết, không thể mượn");
        }

        try {
            Borrow borrow = new Borrow(book, form.getBorrowerName(), email, today, form.getDueDate());
            return borrowRepository.saveAndFlush(borrow);
        } catch (DataIntegrityViolationException ex) {
            // Hai request gửi cùng lúc lọt qua bước kiểm tra -> unique key uk_borrow_active chặn lại
            throw new BusinessException("email",
                    "Email " + email + " đang mượn cuốn '" + book.getTitle() + "' và chưa trả");
        }
    }

    @Transactional
    public Borrow returnBook(Long borrowId) {
        Borrow borrow = borrowRepository.findWithBookById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("lượt mượn", borrowId));
        if (borrow.isReturned()) {
            throw new BusinessException("Lượt mượn #" + borrowId + " đã được trả trước đó");
        }
        borrow.markReturned(today());
        bookRepository.incrementQuantity(borrow.getBook().getId());
        return borrow;
    }
}
