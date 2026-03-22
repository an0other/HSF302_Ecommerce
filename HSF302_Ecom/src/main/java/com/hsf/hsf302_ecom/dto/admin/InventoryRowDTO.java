package com.hsf.hsf302_ecom.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRowDTO {
    private Long variantId;
    private Long productId;
    private String productName;
    private String brandName;
    private String categoryName;
    private String color;
    private String spec;
    private BigDecimal price;
    private Long stock;
    private Long reserved;
    private Long available; // stock - reserved

    public String getStockStatus() {
        if (available <= 0) return "OUT";
        if (available <= 5) return "LOW";
        return "OK";
    }

    public String getPriceFormatted() {
        return price == null ? "—" : String.format("%,.0f ₫", price);
    }
}