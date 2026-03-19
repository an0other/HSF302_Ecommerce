package com.hsf.hsf302_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDTO {
    private Long productVariantId;
    private Long productId;
    private String productName;
    private String variantLabel;
    private BigDecimal price;
    private Long quantity;
    private BigDecimal subTotal;
}