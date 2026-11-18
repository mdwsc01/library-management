package com.example.library.repository;

import com.example.library.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    /** Email đã mượn cuốn này mà chưa trả? */
    boolean existsByBookIdAndEmailAndReturnDateIsNull(Long bookId, String email);

    boolean existsByBookId(Long bookId);

    long countByReturnDateIsNull();

    long countByReturnDateIsNullAndDueDateBefore(LocalDate date);

    @EntityGraph(attributePaths = "book")
    @Query("SELECT br FROM Borrow br WHERE br.returnDate IS NULL ORDER BY br.dueDate ASC, br.id ASC")
    Page<Borrow> findActive(Pageable pageable);

    @EntityGraph(attributePaths = "book")
    @Query("""
            SELECT br FROM Borrow br
            WHERE br.returnDate IS NULL AND br.dueDate < :today
            ORDER BY br.dueDate ASC, br.id ASC
            """)
    Page<Borrow> findOverdue(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "book")
    @Query("SELECT br FROM Borrow br WHERE br.returnDate IS NOT NULL ORDER BY br.returnDate DESC, br.id DESC")
    Page<Borrow> findReturned(Pageable pageable);
}
