package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductsRepo extends JpaRepository<Products, Long> {

    @Query("""
        SELECT new com.hsf.hsf302_ecom.dto.HomeProductDTO(
            p.id, p.name, b.name, c.name, c.id,
            (SELECT pi.imageUrl FROM ProductImages pi WHERE pi.product = p AND pi.isPrimary = true),
            (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true),
            (SELECT AVG(CAST(r.rating AS double)) FROM Reviews r WHERE r.product = p),
            (SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItems oi WHERE oi.productVariant.product = p)
        )
        FROM Products p JOIN p.brand b JOIN p.category c
        WHERE p.status = true AND c.id = :categoryId
          AND EXISTS (
              SELECT 1 FROM Inventories i
              WHERE i.productVariant.product = p
                AND i.productVariant.status = true
                AND (i.stock - i.reserved) > 0
          )
        ORDER BY p.createdDate DESC
        """)
    List<HomeProductDTO> findNewestByCategory(@Param("categoryId") Long categoryId);

    @Query("""
        SELECT new com.hsf.hsf302_ecom.dto.HomeProductDTO(
            p.id, p.name, b.name, c.name, c.id,
            (SELECT pi.imageUrl FROM ProductImages pi WHERE pi.product = p AND pi.isPrimary = true),
            (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true),
            (SELECT AVG(CAST(r.rating AS double)) FROM Reviews r WHERE r.product = p),
            (SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItems oi WHERE oi.productVariant.product = p)
        )
        FROM Products p JOIN p.brand b JOIN p.category c
        WHERE p.status = true
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:brandId    IS NULL OR b.id = :brandId)
          AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                   OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:minPrice   IS NULL OR
               (SELECT MIN(pv2.price) FROM ProductVariants pv2 WHERE pv2.product = p AND pv2.status = true)
               >= :minPrice)
          AND (:maxPrice   IS NULL OR
               (SELECT MIN(pv3.price) FROM ProductVariants pv3 WHERE pv3.product = p AND pv3.status = true)
               <= :maxPrice)
          AND EXISTS (
              SELECT 1 FROM Inventories i
              WHERE i.productVariant.product = p
                AND i.productVariant.status = true
                AND (i.stock - i.reserved) > 0
          )
        ORDER BY p.createdDate DESC
        """)
    Page<HomeProductDTO> findByFilterNewest(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        SELECT new com.hsf.hsf302_ecom.dto.HomeProductDTO(
            p.id, p.name, b.name, c.name, c.id,
            (SELECT pi.imageUrl FROM ProductImages pi WHERE pi.product = p AND pi.isPrimary = true),
            (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true),
            (SELECT AVG(CAST(r.rating AS double)) FROM Reviews r WHERE r.product = p),
            (SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItems oi WHERE oi.productVariant.product = p)
        )
        FROM Products p JOIN p.brand b JOIN p.category c
        WHERE p.status = true
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:brandId    IS NULL OR b.id = :brandId)
          AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                   OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:minPrice   IS NULL OR
               (SELECT MIN(pv2.price) FROM ProductVariants pv2 WHERE pv2.product = p AND pv2.status = true)
               >= :minPrice)
          AND (:maxPrice   IS NULL OR
               (SELECT MIN(pv3.price) FROM ProductVariants pv3 WHERE pv3.product = p AND pv3.status = true)
               <= :maxPrice)
          AND EXISTS (
              SELECT 1 FROM Inventories i
              WHERE i.productVariant.product = p
                AND i.productVariant.status = true
                AND (i.stock - i.reserved) > 0
          )
        ORDER BY (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true) ASC
        """)
    Page<HomeProductDTO> findByFilterPriceAsc(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        SELECT new com.hsf.hsf302_ecom.dto.HomeProductDTO(
            p.id, p.name, b.name, c.name, c.id,
            (SELECT pi.imageUrl FROM ProductImages pi WHERE pi.product = p AND pi.isPrimary = true),
            (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true),
            (SELECT AVG(CAST(r.rating AS double)) FROM Reviews r WHERE r.product = p),
            (SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItems oi WHERE oi.productVariant.product = p)
        )
        FROM Products p JOIN p.brand b JOIN p.category c
        WHERE p.status = true
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:brandId    IS NULL OR b.id = :brandId)
          AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                   OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:minPrice   IS NULL OR
               (SELECT MIN(pv2.price) FROM ProductVariants pv2 WHERE pv2.product = p AND pv2.status = true)
               >= :minPrice)
          AND (:maxPrice   IS NULL OR
               (SELECT MIN(pv3.price) FROM ProductVariants pv3 WHERE pv3.product = p AND pv3.status = true)
               <= :maxPrice)
          AND EXISTS (
              SELECT 1 FROM Inventories i
              WHERE i.productVariant.product = p
                AND i.productVariant.status = true
                AND (i.stock - i.reserved) > 0
          )
        ORDER BY (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true) DESC
        """)
    Page<HomeProductDTO> findByFilterPriceDesc(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        SELECT new com.hsf.hsf302_ecom.dto.HomeProductDTO(
            p.id, p.name, b.name, c.name, c.id,
            (SELECT pi.imageUrl FROM ProductImages pi WHERE pi.product = p AND pi.isPrimary = true),
            (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true),
            (SELECT AVG(CAST(r.rating AS double)) FROM Reviews r WHERE r.product = p),
            (SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItems oi WHERE oi.productVariant.product = p)
        )
        FROM Products p JOIN p.brand b JOIN p.category c
        WHERE p.status = true
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:brandId    IS NULL OR b.id = :brandId)
          AND (:keyword    IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                   OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:minPrice   IS NULL OR
               (SELECT MIN(pv2.price) FROM ProductVariants pv2 WHERE pv2.product = p AND pv2.status = true)
               >= :minPrice)
          AND (:maxPrice   IS NULL OR
               (SELECT MIN(pv3.price) FROM ProductVariants pv3 WHERE pv3.product = p AND pv3.status = true)
               <= :maxPrice)
          AND EXISTS (
              SELECT 1 FROM Inventories i
              WHERE i.productVariant.product = p
                AND i.productVariant.status = true
                AND (i.stock - i.reserved) > 0
          )
        ORDER BY (SELECT COALESCE(AVG(CAST(r3.rating AS double)), 0) FROM Reviews r3 WHERE r3.product = p) DESC
        """)
    Page<HomeProductDTO> findByFilterRating(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable);

    default Page<HomeProductDTO> findByFilter(
            Long categoryId, Long brandId, String keyword,
            String sort, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if ("price_asc".equals(sort))
            return findByFilterPriceAsc(categoryId, brandId, keyword, minPrice, maxPrice, pageable);
        if ("price_desc".equals(sort))
            return findByFilterPriceDesc(categoryId, brandId, keyword, minPrice, maxPrice, pageable);
        if ("rating".equals(sort))
            return findByFilterRating(categoryId, brandId, keyword, minPrice, maxPrice, pageable);
        return findByFilterNewest(categoryId, brandId, keyword, minPrice, maxPrice, pageable);
    }

    @Query(value = """
    SELECT p FROM Products p
    JOIN FETCH p.category
    JOIN FETCH p.brand
    LEFT JOIN FETCH p.productVariants
    WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%')))
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:brandId    IS NULL OR p.brand.id    = :brandId)
    ORDER BY p.createdDate DESC
    """,
            countQuery = """
    SELECT COUNT(p) FROM Products p
    WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%')))
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:brandId    IS NULL OR p.brand.id    = :brandId)
    """)
    Page<Products> findByFiltersAdmin(
            @Param("keyword")    String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            Pageable pageable);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCase(String name);
}