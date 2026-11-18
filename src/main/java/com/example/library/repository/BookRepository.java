package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Tìm theo tên hoặc tác giả (không phân biệt hoa thường), lọc theo thể loại.
     * Tham số null nghĩa là bỏ qua điều kiện đó. EntityGraph nạp luôn category để tránh N+1.
     */
    @EntityGraph(attributePaths = "category")
    @Query(value = """
            SELECT b FROM Book b
            WHERE (:keyword IS NULL
                   OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
            """,
            countQuery = """
            SELECT COUNT(b) FROM Book b
            WHERE (:keyword IS NULL
                   OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
            """)
    Page<Book> search(@Param("keyword") String keyword,
                      @Param("categoryId") Long categoryId,
                      Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Book> findWithCategoryById(Long id);

    /** Sách còn trong kho, dùng cho dropdown ở form mượn. */
    List<Book> findByQuantityGreaterThanOrderByTitleAsc(int quantity);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    long countByCategoryId(Long categoryId);

    /**
     * Trừ kho nguyên tử: chỉ trừ khi còn sách. Trả về số dòng bị ảnh hưởng (0 = hết sách).
     * Tránh trường hợp hai người mượn cùng lúc cuốn cuối cùng.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Book b SET b.quantity = b.quantity - 1 WHERE b.id = :id AND b.quantity > 0")
    int decrementQuantity(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Book b SET b.quantity = b.quantity + 1 WHERE b.id = :id")
    int incrementQuantity(@Param("id") Long id);
}
