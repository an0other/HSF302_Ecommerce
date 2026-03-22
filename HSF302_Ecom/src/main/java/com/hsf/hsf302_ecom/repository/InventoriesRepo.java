package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.Inventories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoriesRepo extends JpaRepository<Inventories, Long> {

    Optional<Inventories> findByProductVariantId(Long variantId);

    /**
     * Returns available = stock − reserved as a scalar Long.
     *
     * WHY SCALAR INSTEAD OF findByProductVariantId():
     *   Derived finders like findByProductVariantId() load a full entity and
     *   store it in Hibernate's first-level (session) cache. A subsequent call
     *   in the same transaction returns the cached object even if a @Modifying
     *   UPDATE already changed the DB row. A scalar @Query projection skips the
     *   entity cache entirely and always issues a fresh SELECT to the DB.
     *
     * Returns 0 if no inventory row exists for this variant.
     */
    @Query("""
        SELECT CASE WHEN (i.stock - i.reserved) < 0 THEN 0
                    ELSE (i.stock - i.reserved) END
          FROM Inventories i
         WHERE i.productVariant.id = :variantId
        """)
    Long findAvailableStock(@Param("variantId") Long variantId);

    /**
     * Atomically increase reserved by delta.
     * Guard: (reserved + delta) must not exceed stock — prevents over-reservation.
     * Returns 1 if updated, 0 if the guard blocked it (race condition / out of stock).
     *
     * clearAutomatically = true  → evicts Inventories from the session cache after
     *                              the UPDATE, so any subsequent entity read in this
     *                              transaction fetches fresh data from the DB.
     * flushAutomatically = true  → flushes pending dirty writes BEFORE the UPDATE
     *                              so the WHERE clause evaluates against the latest state.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventories i
           SET i.reserved  = i.reserved + :delta,
               i.updatedAt = CURRENT_TIMESTAMP
         WHERE i.productVariant.id = :variantId
           AND (i.reserved + :delta) <= i.stock
        """)
    int incrementReserved(@Param("variantId") Long variantId,
                          @Param("delta")     Long delta);

    /**
     * Atomically decrease reserved by delta, floors at 0.
     * Called on remove-item, decrease-qty, clear-cart.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventories i
           SET i.reserved  = CASE WHEN (i.reserved - :delta) < 0 THEN 0
                                  ELSE (i.reserved - :delta) END,
               i.updatedAt = CURRENT_TIMESTAMP
         WHERE i.productVariant.id = :variantId
        """)
    void decrementReserved(@Param("variantId") Long variantId,
                           @Param("delta")     Long delta);

    /**
     * Called when an order is PLACED.
     * Deducts qty from stock (physical reduction) and releases qty from
     * reserved (soft-hold consumed). Both columns shrink equally.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventories i
           SET i.stock     = CASE WHEN (i.stock    - :qty) < 0 THEN 0
                                  ELSE (i.stock    - :qty) END,
               i.reserved  = CASE WHEN (i.reserved - :qty) < 0 THEN 0
                                  ELSE (i.reserved - :qty) END,
               i.updatedAt = CURRENT_TIMESTAMP
         WHERE i.productVariant.id = :variantId
        """)
    void deductStockOnOrder(@Param("variantId") Long variantId,
                            @Param("qty")       Long qty);

    /**
     * Called when an order is CANCELLED or REFUNDED.
     * Returns qty to stock only — reserved is not touched.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventories i
           SET i.stock     = i.stock + :qty,
               i.updatedAt = CURRENT_TIMESTAMP
         WHERE i.productVariant.id = :variantId
        """)
    void restockOnCancel(@Param("variantId") Long variantId,
                         @Param("qty")       Long qty);

    @Query(value = """
        SELECT i FROM Inventories i
        JOIN i.productVariant pv
        JOIN pv.product p
        JOIN p.brand b
        JOIN p.category c
        WHERE (:keyword IS NULL OR
               LOWER(p.name)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(pv.color) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(pv.spec)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(b.name)  LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:brandId    IS NULL OR b.id = :brandId)
        ORDER BY
            CASE WHEN (i.stock - i.reserved) <= 0 THEN 0
                 WHEN (i.stock - i.reserved) <= 5 THEN 1
                 ELSE 2 END,
            p.name ASC
        """,
            countQuery = """
        SELECT COUNT(i) FROM Inventories i
        JOIN i.productVariant pv
        JOIN pv.product p
        JOIN p.brand b
        JOIN p.category c
        WHERE (:keyword IS NULL OR
               LOWER(p.name)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(pv.color) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(pv.spec)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(b.name)  LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR c.id = :categoryId)
          AND (:brandId    IS NULL OR b.id = :brandId)
        """)
    Page<Inventories> findByFilters(
            @Param("keyword")    String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brandId")    Long brandId,
            Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inventories i WHERE (i.stock - i.reserved) <= 0")
    long countOutOfStock();

    @Query("SELECT COUNT(i) FROM Inventories i WHERE (i.stock - i.reserved) > 0 AND (i.stock - i.reserved) <= 5")
    long countLowStock();
}