package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.ProductVariants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantsRepo extends JpaRepository<ProductVariants, Long> {

    boolean existsByProductIdAndColorIgnoreCaseAndSpecIgnoreCase(
            Long productId, String color, String spec);

    boolean existsByProductIdAndColorIgnoreCaseAndSpecIgnoreCaseAndIdNot(
            Long productId, String color, String spec, Long excludeId);

    @Query("""
        SELECT COUNT(v) > 0
          FROM ProductVariants v
         WHERE v.product.id = :productId
           AND v.status = true
           AND v.id <> :excludeVariantId
        """)
    boolean existsOtherActiveVariant(
            @Param("productId")         Long productId,
            @Param("excludeVariantId")  Long excludeVariantId);
}