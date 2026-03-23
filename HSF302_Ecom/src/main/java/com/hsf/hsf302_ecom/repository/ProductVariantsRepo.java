package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.ProductVariants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantsRepo extends JpaRepository<ProductVariants, Long> {
    // Check duplicate color+spec within same product (for create)
    boolean existsByProductIdAndColorIgnoreCaseAndSpecIgnoreCase(
            Long productId, String color, String spec);

    // Check duplicate color+spec excluding self (for edit)
    boolean existsByProductIdAndColorIgnoreCaseAndSpecIgnoreCaseAndIdNot(
            Long productId, String color, String spec, Long excludeId);
}
