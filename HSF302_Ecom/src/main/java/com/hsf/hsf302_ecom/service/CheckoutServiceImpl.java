package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.CartItemDTO;
import com.hsf.hsf302_ecom.dto.CheckoutRequestDTO;
import com.hsf.hsf302_ecom.dto.CheckoutSummaryDTO;
import com.hsf.hsf302_ecom.entity.*;
import com.hsf.hsf302_ecom.enums.CartStatus;
import com.hsf.hsf302_ecom.enums.OrderStatus;
import com.hsf.hsf302_ecom.enums.PaymentMethod;
import com.hsf.hsf302_ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartsService        cartsService;
    private final CartsRepo           cartsRepo;
    private final CartItemsRepo       cartItemsRepo;
    private final UsersRepo           usersRepo;
    private final OrdersRepo          ordersRepo;
    private final OrderItemsRepo      orderItemsRepo;
    private final PaymentsRepo        paymentsRepo;
    private final InventoriesRepo     inventoriesRepo;
    private final ProductVariantsRepo variantsRepo;
    private final ProductsRepo        productsRepo;

    private void autoDeactivateIfDepleted(Long variantId, Long productId) {
        Inventories inv = inventoriesRepo.findByProductVariantId(variantId).orElse(null);
        if (inv == null || inv.getStock() > 0) return;

        variantsRepo.findById(variantId).ifPresent(v -> {
            if (Boolean.TRUE.equals(v.getStatus())) {
                v.setStatus(false);
                variantsRepo.save(v);
            }
        });

        boolean anyOtherActive =
                variantsRepo.existsOtherActiveVariant(productId, variantId);

        if (!anyOtherActive) {
            productsRepo.findById(productId).ifPresent(p -> {
                if (Boolean.TRUE.equals(p.getStatus())) {
                    p.setStatus(false);
                    productsRepo.save(p);
                }
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutSummaryDTO getCheckoutSummary(Long userId) {
        List<CartItemDTO> cartItems = cartsService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            return CheckoutSummaryDTO.builder()
                    .items(List.of())
                    .subtotal(BigDecimal.ZERO)
                    .shippingFee(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .cartEmpty(true)
                    .build();
        }

        List<CheckoutSummaryDTO.CheckoutItemDTO> items = cartItems.stream()
                .map(ci -> CheckoutSummaryDTO.CheckoutItemDTO.builder()
                        .productId(ci.getProductId())
                        .variantId(ci.getVariantId())
                        .productName(ci.getProductName())
                        .brandName(ci.getBrandName())
                        .color(ci.getColor())
                        .spec(ci.getSpec())
                        .unitPrice(ci.getUnitPrice())
                        .quantity(ci.getQuantity())
                        .subTotal(ci.getSubTotal())
                        .imageUrl(ci.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CheckoutSummaryDTO.CheckoutItemDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CheckoutSummaryDTO.builder()
                .items(items)
                .subtotal(subtotal)
                .shippingFee(BigDecimal.ZERO)
                .total(subtotal)
                .cartEmpty(false)
                .build();
    }

    @Override
    @Transactional
    public Long placeOrder(Long userId, CheckoutRequestDTO request) {

        Carts cart = cartsRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active cart found."));

        List<CartItems> cartItems = cartItemsRepo.findByCartIdWithDetails(cart.getId());
        if (cartItems.isEmpty())
            throw new IllegalStateException("Cannot place an order with an empty cart.");

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItems ci : cartItems) {
            subtotal = subtotal.add(
                    ci.getProductVariant().getPrice()
                            .multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // ── Resolve payment method ───────────────────────────────────────────
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid payment method: " + request.getPaymentMethod());
        }

        Users  user  = usersRepo.getReferenceById(userId);
        Orders order = Orders.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingDistrict(request.getShippingDistrict())
                .status(OrderStatus.PENDING)
                .totalPrice(subtotal)
                .build();
        order = ordersRepo.save(order);

        // ── Persist order items + deduct inventory ───────────────────────────
        List<OrderItems> orderItemsList = new ArrayList<>();
        for (CartItems ci : cartItems) {
            ProductVariants variant   = ci.getProductVariant();
            Long            variantId = variant.getId();
            // Read product id directly from the already-fetched variant proxy —
            // getProduct() here only loads the FK, not the variants collection.
            Long            productId = variant.getProduct().getId();
            Long            qty       = ci.getQuantity();
            BigDecimal      unitPrice = variant.getPrice();

            String productName = variant.getProduct().getName()
                    + " (" + variant.getColor() + " / " + variant.getSpec() + ")";

            orderItemsList.add(OrderItems.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(productName)
                    .price(unitPrice)
                    .quantity(qty)
                    .subTotal(unitPrice.multiply(BigDecimal.valueOf(qty)))
                    .build());

            inventoriesRepo.deductStockOnOrder(variantId, qty);

            autoDeactivateIfDepleted(variantId, productId);
        }
        orderItemsRepo.saveAll(orderItemsList);

        paymentsRepo.save(Payments.builder()
                .order(order)
                .method(paymentMethod)
                .amount(subtotal)
                .build());

        cart.setCartItems(cartItems);
        cart.setStatus(CartStatus.ORDERED);
        cartsRepo.save(cart);

        return order.getId();
    }
}