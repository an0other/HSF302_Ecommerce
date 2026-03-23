package com.hsf.hsf302_ecom.dto.admin;

import com.hsf.hsf302_ecom.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}
