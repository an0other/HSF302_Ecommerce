package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.repository.OrdersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepo ordersRepo;

    @Override
    public List<Orders> getOrdersByUser(Long userId) {
        return ordersRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
    @Transactional
    @Override
    public Orders getOrderDetail(Long orderId, Long userId) {
        return ordersRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }
}
