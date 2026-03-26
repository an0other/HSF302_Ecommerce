package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImagesRepo extends JpaRepository<ProductImages, Long> {

    List<ProductImages> findByProductIdOrderByIsPrimaryDescIdAsc(Long productId);

    long countByProductId(Long productId);

    Optional<ProductImages> findFirstByProductIdAndIsPrimaryTrue(Long productId);

    @Modifying
    @Query("UPDATE ProductImages pi SET pi.isPrimary = false WHERE pi.product.id = :productId")
    void clearPrimaryForProduct(@Param("productId") Long productId);
}