package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepo extends JpaRepository<Products, Long> {

    /* ── HOME: newest per category (carousel) ─────────────────────── */
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
        ORDER BY p.createdDate DESC
        """)
    List<HomeProductDTO> findNewestByCategory(@Param("categoryId") Long categoryId);

    /* ─────────────────────────────────────────────────────────────────
       PRODUCTS PAGE — ORDER BY newest (default)
    ───────────────────────────────────────────────────────────────── */
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
          AND (:minRating  IS NULL OR
               (SELECT COALESCE(AVG(CAST(r2.rating AS double)), 0) FROM Reviews r2 WHERE r2.product = p)
               >= :minRating)
        ORDER BY p.createdDate DESC
        """)
    Page<HomeProductDTO> findByFilterNewest(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minRating")  Integer minRating,
            Pageable pageable);

    /* ── ORDER BY price ASC ──────────────────────────────────────── */
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
          AND (:minRating  IS NULL OR
               (SELECT COALESCE(AVG(CAST(r2.rating AS double)), 0) FROM Reviews r2 WHERE r2.product = p)
               >= :minRating)
        ORDER BY (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true) ASC
        """)
    Page<HomeProductDTO> findByFilterPriceAsc(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minRating")  Integer minRating,
            Pageable pageable);

    /* ── ORDER BY price DESC ─────────────────────────────────────── */
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
          AND (:minRating  IS NULL OR
               (SELECT COALESCE(AVG(CAST(r2.rating AS double)), 0) FROM Reviews r2 WHERE r2.product = p)
               >= :minRating)
        ORDER BY (SELECT MIN(pv.price) FROM ProductVariants pv WHERE pv.product = p AND pv.status = true) DESC
        """)
    Page<HomeProductDTO> findByFilterPriceDesc(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minRating")  Integer minRating,
            Pageable pageable);

    /* ── ORDER BY rating DESC ────────────────────────────────────── */
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
          AND (:minRating  IS NULL OR
               (SELECT COALESCE(AVG(CAST(r2.rating AS double)), 0) FROM Reviews r2 WHERE r2.product = p)
               >= :minRating)
        ORDER BY (SELECT COALESCE(AVG(CAST(r3.rating AS double)), 0) FROM Reviews r3 WHERE r3.product = p) DESC
        """)
    Page<HomeProductDTO> findByFilterRating(
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            @Param("keyword")    String keyword,
            @Param("minRating")  Integer minRating,
            Pageable pageable);

    /* ── Dispatcher used by ProductServiceImpl ───────────────────── */
    default Page<HomeProductDTO> findByFilter(
            Long categoryId, Long brandId, String keyword,
            String sort, Integer minRating, Pageable pageable) {
        if ("price_asc".equals(sort))
            return findByFilterPriceAsc(categoryId, brandId, keyword, minRating, pageable);
        if ("price_desc".equals(sort))
            return findByFilterPriceDesc(categoryId, brandId, keyword, minRating, pageable);
        if ("rating".equals(sort))
            return findByFilterRating(categoryId, brandId, keyword, minRating, pageable);
        return findByFilterNewest(categoryId, brandId, keyword, minRating, pageable);
    }
}