package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Một lượt mượn sách. {@code returnDate == null} nghĩa là chưa trả. */
@Entity
@Table(name = "borrow")
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrower_name", nullable = false, length = 100)
    private String borrowerName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    protected Borrow() {
        // cho JPA
    }

    public Borrow(Book book, String borrowerName, String email, LocalDate borrowDate, LocalDate dueDate) {
        if (dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Hạn trả phải sau hoặc bằng ngày mượn");
        }
        this.book = book;
        this.borrowerName = borrowerName.trim();
        this.email = normalizeEmail(email);
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    /** Quá hạn: chưa trả và hôm nay đã qua hạn trả. */
    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(dueDate);
    }

    public long overdueDays(LocalDate today) {
        return isOverdue(today) ? ChronoUnit.DAYS.between(dueDate, today) : 0;
    }

    public void markReturned(LocalDate date) {
        if (isReturned()) {
            throw new IllegalStateException("Lượt mượn này đã được trả");
        }
        this.returnDate = date;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }
}
