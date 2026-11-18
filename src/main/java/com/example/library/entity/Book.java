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

/**
 * Sách. {@code quantity} là số cuốn còn trong kho có thể cho mượn:
 * giảm 1 khi mượn, tăng 1 khi trả.
 */
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(name = "publish_year", nullable = false)
    private Integer publishYear;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Book() {
        // cho JPA
    }

    public Book(String code, String title, String author, int publishYear, int quantity, Category category) {
        updateDetails(code, title, author, publishYear, quantity, category);
    }

    public void updateDetails(String code, String title, String author,
                              int publishYear, int quantity, Category category) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        this.code = code.trim().toUpperCase();
        this.title = title.trim();
        this.author = author.trim();
        this.publishYear = publishYear;
        this.quantity = quantity;
        this.category = category;
    }

    public boolean isAvailable() {
        return quantity != null && quantity > 0;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Category getCategory() {
        return category;
    }
}
