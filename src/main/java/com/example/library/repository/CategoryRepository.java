package com.example.library.repository;

import com.example.library.dto.CategoryView;
import com.example.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByNameAsc();

    /** Danh sách thể loại kèm số sách — một câu truy vấn LEFT JOIN + GROUP BY, không N+1. */
    @Query("""
            SELECT new com.example.library.dto.CategoryView(c.id, c.name, COUNT(b.id))
            FROM Category c LEFT JOIN Book b ON b.category = c
            GROUP BY c.id, c.name
            ORDER BY c.name
            """)
    List<CategoryView> findAllWithBookCount();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
