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
public class VariantRowDTO {
    private Long       id;
    private String     color;
    private String     spec;
    private BigDecimal price;
    private boolean    status;
    private Long       stock;
    private Long       reserved;
    private Long       available;

    public String getPriceFormatted() {
        return price == null ? "—" : String.format("%,.0f ₫", price);
    }

    public String getStockStatus() {
        if (available == null || available <= 0) return "OUT";
        if (available <= 5) return "LOW";
        return "OK";
    }
}