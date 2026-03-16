package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.OrderDTO;
import com.hsf.hsf302_ecom.dto.OrderDetailDTO;
import com.hsf.hsf302_ecom.dto.OrderItemDTO;
import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.repository.OrderItemsRepo;
import com.hsf.hsf302_ecom.repository.OrdersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepo ordersRepo;
    private final OrderItemsRepo orderItemsRepo;

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

        List<OrderItemDTO> items = orderItemsRepo.findByOrder_Id(orderId)
                .stream()
                .map(item -> OrderItemDTO.builder()
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal())
                        .build())
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
                .build();
    }
}
