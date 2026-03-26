package com.hsf.hsf302_ecom.enums;

import java.util.List;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED;

    public List<OrderStatus> nextStatuses() {
        return switch (this) {
            case PENDING -> List.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> List.of(PROCESSING, CANCELLED);
            case PROCESSING -> List.of(SHIPPED);
            case SHIPPED -> List.of(DELIVERED, REFUNDED);
            case DELIVERED -> List.of(REFUNDED);
            case CANCELLED, REFUNDED -> List.of();
        };
    }

    public boolean canTransitionTo(OrderStatus next) {
        return nextStatuses().contains(next);
    }
}
