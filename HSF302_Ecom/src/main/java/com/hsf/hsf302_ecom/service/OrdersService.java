package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.OrderDTO;
import com.hsf.hsf302_ecom.dto.OrderDetailDTO;
import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.enums.OrderStatus;

import java.util.List;

public interface OrdersService {

    List<OrderDTO> getMyOrders(Long userId);
    OrderDetailDTO getMyOrderDetail(Long userId, Long orderId);

}
