package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.entity.Orders;

import java.util.List;

public interface OrdersService {

    List<Orders> getOrdersByUser(Long userId);

    Orders getOrderDetail(Long orderId, Long userId);

}
