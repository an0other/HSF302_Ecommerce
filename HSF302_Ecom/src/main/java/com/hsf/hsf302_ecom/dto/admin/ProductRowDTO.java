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
public class ProductRowDTO {
    private Long   id;
    private String name;
    private String categoryName;
    private String brandName;
    private int    variantCount;
    private long   totalStock;
    private boolean status;

    private BigDecimal lowestPrice;

    public String getPriceFormatted() {
        return lowestPrice == null ? "—" : String.format("%,.0f ₫", lowestPrice);
    }
}