package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.CartItemDTO;
import com.hsf.hsf302_ecom.entity.*;
import com.hsf.hsf302_ecom.enums.CartStatus;
import com.hsf.hsf302_ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartsServiceImpl implements CartsService {

    private final CartsRepo           cartsRepo;
    private final CartItemsRepo       cartItemsRepo;
    private final UsersRepo           usersRepo;
    private final ProductVariantsRepo variantsRepo;
    private final InventoriesRepo     inventoriesRepo;


    private Carts getOrCreateActiveCart(Long userId) {
        return cartsRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Users user = usersRepo.getReferenceById(userId);
                    Carts cart = Carts.builder()
                            .user(user)
                            .status(CartStatus.ACTIVE)
                            .build();
                    return cartsRepo.save(cart);
                });
    }

    /**
     * Reads available = stock − reserved directly from the DB via a JPQL
     * projection query — bypasses the first-level cache entirely.
     *
     * WHY A SEPARATE QUERY INSTEAD OF findByProductVariantId():
     *   findByProductVariantId() goes through the entity cache. Any @Modifying
     *   query (incrementReserved, decrementReserved, etc.) that ran earlier in
     *   the same transaction would have cleared the cache (clearAutomatically=true),
     *   but a second call within the same transaction session could still return
     *   a re-cached stale object if Hibernate reloads it before our UPDATE commits.
     *   Using a scalar projection (@Query returning Long) skips the entit y cache
     *   completely and always hits the DB.
     */
    private Long availableStock(Long variantId) {
        return inventoriesRepo.findAvailableStock(variantId);
    }

    private String primaryImageUrl(Products product) {
        return product.getProductImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImages::getImageUrl)
                .findFirst()
                .orElseGet(() -> product.getProductImages().stream()
                        .map(ProductImages::getImageUrl)
                        .findFirst()
                        .orElse("/img/image1.webp"));
    }

    /* ══════════════════════════════════════════════════════════
       ADD TO CART
       ─────────────────────────────────────────────────────────
       Cache story:
         - availableStock() uses scalar projection → no cache hit
         - incrementReserved() has clearAutomatically=true → evicts cache
         - no further reads of Inventories in this method → safe
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void addToCart(Long userId, Long variantId, Long qty) {
        if (qty == null || qty <= 0) return;

        variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));

        Long available = availableStock(variantId);
        if (available <= 0) throw new IllegalStateException("This item is out of stock.");

        Carts cart = getOrCreateActiveCart(userId);

        Optional<CartItems> existing =
                cartItemsRepo.findByCartIdAndVariantId(cart.getId(), variantId);

        if (existing.isPresent()) {
            CartItems item        = existing.get();
            Long      oldQty      = item.getQuantity();
            Long      newQty      = Math.min(oldQty + qty, oldQty + available);
            Long      netIncrease = newQty - oldQty;

            if (netIncrease <= 0) throw new IllegalStateException("No additional stock available.");

            item.setQuantity(newQty);
            cartItemsRepo.save(item);

            int updated = inventoriesRepo.incrementReserved(variantId, netIncrease);
            if (updated == 0)
                throw new IllegalStateException("Stock reservation failed — insufficient stock.");

        } else {
            Long addQty = Math.min(qty, available);
            ProductVariants variant = variantsRepo.getReferenceById(variantId);
            CartItems item = CartItems.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(addQty)
                    .build();
            cartItemsRepo.save(item);

            int updated = inventoriesRepo.incrementReserved(variantId, addQty);
            if (updated == 0)
                throw new IllegalStateException("Stock reservation failed — insufficient stock.");
        }
    }

    /* ══════════════════════════════════════════════════════════
       UPDATE QUANTITY
       ─────────────────────────────────────────────────────────
       Cache story:
         delta > 0:
           - availableStock() called BEFORE incrementReserved() → fresh scalar read
           - incrementReserved() clears cache after → safe
         delta < 0:
           - no read of Inventories needed → safe
         newQty <= 0 (treat as remove):
           - no read of Inventories needed → safe
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, Long newQty) {
        CartItems item = cartItemsRepo.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(userId))
            throw new SecurityException("Access denied");

        Long variantId = item.getProductVariant().getId();
        Long oldQty    = item.getQuantity();

        if (newQty == null || newQty <= 0) {
            cartItemsRepo.delete(item);
            inventoriesRepo.decrementReserved(variantId, oldQty);
            return;
        }

        long delta = newQty - oldQty;

        if (delta > 0) {
            Long available = availableStock(variantId);  // scalar projection — always fresh
            if (available < delta)
                throw new IllegalStateException(
                        "Only " + (oldQty + available) + " units available.");

            item.setQuantity(newQty);
            cartItemsRepo.save(item);
            inventoriesRepo.incrementReserved(variantId, delta);

        } else if (delta < 0) {
            item.setQuantity(newQty);
            cartItemsRepo.save(item);
            inventoriesRepo.decrementReserved(variantId, Math.abs(delta));
        }
    }

    /* ══════════════════════════════════════════════════════════
       REMOVE ITEM
       ─────────────────────────────────────────────────────────
       Cache story:
         - No read of Inventories — no cache risk.
         - decrementReserved() clears cache after anyway.
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItems item = cartItemsRepo.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(userId))
            throw new SecurityException("Access denied");

        Long variantId = item.getProductVariant().getId();
        Long qty       = item.getQuantity();

        cartItemsRepo.delete(item);
        inventoriesRepo.decrementReserved(variantId, qty);
    }

    /* ══════════════════════════════════════════════════════════
       CLEAR CART
       ─────────────────────────────────────────────────────────
       Cache story:
         - Iterates multiple variants, calling decrementReserved() per item.
         - Each decrementReserved() has clearAutomatically=true, so cache is
           evicted after each UPDATE. No stale reads between iterations.
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartsRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .ifPresent(cart -> {
                    List<CartItems> items = cartItemsRepo.findByCartIdWithDetails(cart.getId());

                    items.forEach(item ->
                            inventoriesRepo.decrementReserved(
                                    item.getProductVariant().getId(),
                                    item.getQuantity()));

                    cartItemsRepo.deleteAll(items);
                });
    }

    /* ══════════════════════════════════════════════════════════
       CONVERT CART → ORDER
       ─────────────────────────────────────────────────────────
       Cache story:
         - availableStock() is called BEFORE deductStockOnOrder() per item.
         - Because it uses a scalar projection it always reads fresh DB values.
         - After deductStockOnOrder() the cache is cleared (clearAutomatically).
         - The NEXT loop iteration's availableStock() therefore also reads fresh
           values — correct even when two items share the same variant (rare but
           possible if someone had duplicates before dedup logic existed).
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void convertCartToOrder(Long userId) {
        Carts cart = cartsRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                        "No active cart found for user " + userId));

        List<CartItems> items = cartItemsRepo.findByCartIdWithDetails(cart.getId());
        if (items.isEmpty())
            throw new IllegalStateException("Cannot place order with an empty cart.");

        for (CartItems item : items) {
            Long variantId = item.getProductVariant().getId();
            Long qty       = item.getQuantity();

            Long available = availableStock(variantId);  // scalar projection — always fresh
            if (available < 0) {
                throw new IllegalStateException(
                        "Insufficient stock for: "
                                + item.getProductVariant().getProduct().getName());
            }

            inventoriesRepo.deductStockOnOrder(variantId, qty);
            // cache cleared by clearAutomatically=true → next iteration reads fresh
        }

        cart.setStatus(CartStatus.ORDERED);
        cartsRepo.save(cart);
    }

    @Override
    @Transactional
    public void releaseStockOnCancel(Long variantId, Long qty) {
        inventoriesRepo.restockOnCancel(variantId, qty);
    }

    /* ══════════════════════════════════════════════════════════
       READ OPERATIONS
       ─────────────────────────────────────────────────────────
       Cache story:
         - readOnly=true transactions never run @Modifying queries, so the cache
           is never dirtied here. findByProductVariantId() inside toDTO() is safe.
         - But we still use availableStock() (scalar projection) in toDTO() to be
           consistent and future-proof — if this method is ever called in a
           read-write transaction the scalar read will still be correct.
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(Long userId) {
        return cartsRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .map(cart -> cartItemsRepo
                        .findByCartIdWithDetails(cart.getId())
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getCartItemCount(Long userId) {
        return getCartItems(userId).stream()
                .mapToLong(CartItemDTO::getQuantity)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(Long userId) {
        return getCartItems(userId).stream()
                .map(CartItemDTO::getSubTotal)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItemDTO toDTO(CartItems item) {
        ProductVariants v       = item.getProductVariant();
        Products        product = v.getProduct();
        Long            stock   = availableStock(v.getId());
        BigDecimal      sub     = v.getPrice() != null
                ? v.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                : BigDecimal.ZERO;

        return new CartItemDTO(
                item.getId(),
                product.getId(),
                v.getId(),
                product.getName(),
                product.getBrand() != null ? product.getBrand().getName() : "",
                v.getColor(),
                v.getSpec(),
                v.getPrice(),
                item.getQuantity(),
                sub,
                primaryImageUrl(product),
                stock
        );
    }
}