package com.hsf.hsf302_ecom.repository;

import com.hsf.hsf302_ecom.entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemsRepo extends JpaRepository<CartItems, Long> {

    @Query("SELECT ci FROM CartItems ci WHERE ci.cart.id = :cartId AND ci.productVariant.id = :variantId")
    Optional<CartItems> findByCartIdAndVariantId(@Param("cartId") Long cartId,
                                                 @Param("variantId") Long variantId);

    //Thay bằng entitygraph sẽ tiện hơn để lam sau
    @Query("""
        SELECT ci FROM CartItems ci
        JOIN FETCH ci.productVariant pv
        JOIN FETCH pv.product p
        JOIN FETCH p.brand
        LEFT JOIN FETCH p.productImages
        WHERE ci.cart.id = :cartId
        """)
    List<CartItems> findByCartIdWithDetails(@Param("cartId") Long cartId);
}