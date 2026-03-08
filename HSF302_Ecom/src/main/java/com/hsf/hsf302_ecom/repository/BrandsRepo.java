package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Brands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandsRepo extends JpaRepository<Brands, Long> {

    /** Brands that have active products in a given category.
     *  Navigates via Products (owner of the brand FK), not via Brands.products
     *  (which may not be mapped on the Brands entity). */
    @Query("""
        SELECT DISTINCT b FROM Brands b
        WHERE EXISTS (
            SELECT p FROM Products p
            WHERE p.brand = b
              AND p.status = true
              AND p.category.id = :categoryId
        )
        ORDER BY b.name ASC
        """)
    List<Brands> findByCategory(@Param("categoryId") Long categoryId);
}