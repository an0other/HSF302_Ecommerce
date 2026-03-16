package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.CartItemDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CartsService {

    void addToCart(Long userId, Long variantId, Long qty);

    void updateQuantity(Long userId, Long cartItemId, Long newQty);

    void removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);

    void convertCartToOrder(Long userId);

    void releaseStockOnCancel(Long variantId, Long qty);

    List<CartItemDTO> getCartItems(Long userId);

    long getCartItemCount(Long userId);

    BigDecimal getCartTotal(Long userId);
}