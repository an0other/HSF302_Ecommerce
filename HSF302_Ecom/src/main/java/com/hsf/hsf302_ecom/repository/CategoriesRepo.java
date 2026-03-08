package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepo extends JpaRepository<Categories, Long> {

    /** All active categories ordered by name — used in nav strip & search dropdown */
    @Query("SELECT c FROM Categories c WHERE c.status = true ORDER BY c.name ASC")
    List<Categories> findAllActive();

    /** Categories that actually have at least one active product (for carousel sections) */
    @Query("""
        SELECT DISTINCT c FROM Categories c
        JOIN c.products p
        WHERE c.status = true AND p.status = true
        ORDER BY c.name ASC
        """)
    List<Categories> findAllWithActiveProducts();
}
