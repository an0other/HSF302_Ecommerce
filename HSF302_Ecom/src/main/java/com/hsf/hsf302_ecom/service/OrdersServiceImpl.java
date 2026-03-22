package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.OrderDTO;
import com.hsf.hsf302_ecom.dto.OrderDetailDTO;
import com.hsf.hsf302_ecom.dto.OrderItemDTO;
import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.entity.Payments;
import com.hsf.hsf302_ecom.enums.OrderStatus;
import com.hsf.hsf302_ecom.repository.OrderItemsRepo;
import com.hsf.hsf302_ecom.repository.OrdersRepo;
import com.hsf.hsf302_ecom.repository.PaymentsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepo     ordersRepo;
    private final OrderItemsRepo orderItemsRepo;
    private final PaymentsRepo   paymentsRepo;

    @Override
    public List<OrderDTO> getMyOrders(Long userId) {
        return ordersRepo.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(order -> OrderDTO.builder()
                        .id(order.getId())
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus().name())
                        .createdAt(order.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public OrderDetailDTO getMyOrderDetail(Long userId, Long orderId) {
        Orders order = ordersRepo.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

        List<Payments> payments = order.getPayments();
        String paymentMethod = "N/A";
        boolean paymentPaid  = false;
        if (payments != null && !payments.isEmpty()) {
            Payments p = payments.get(0);
            paymentMethod = p.getMethod() != null ? p.getMethod().name() : "N/A";
            paymentPaid   = p.getPaidAt() != null;
        }

        List<OrderItemDTO> items = orderItemsRepo.findByOrder_Id(orderId)
                .stream()
                .map(item -> {
                    Long productId = null;
                    String variantLabel = "";
                    if (item.getProductVariant() != null) {
                        if (item.getProductVariant().getProduct() != null) {
                            productId = item.getProductVariant().getProduct().getId();
                        }
                        variantLabel = item.getProductVariant().getColor()
                                + " / " + item.getProductVariant().getSpec();
                    }
                    return OrderItemDTO.builder()
                            .productVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                            .productId(productId)
                            .productName(item.getProductName())
                            .variantLabel(variantLabel)
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .subTotal(item.getSubTotal())
                            .build();
                })
                .toList();

        return OrderDetailDTO.builder()
                .id(order.getId())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingDistrict(order.getShippingDistrict())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(items)
                .paymentMethod(paymentMethod)
                .paymentPaid(paymentPaid)
                .build();
    }

}