package com.hsf.hsf302_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutSummaryDTO {

    private List<CheckoutItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private boolean    cartEmpty;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckoutItemDTO {
        private Long       productId;
        private Long       variantId;
        private String     productName;
        private String     brandName;
        private String     color;
        private String     spec;
        private BigDecimal unitPrice;
        private Long       quantity;
        private BigDecimal subTotal;
        private String     imageUrl;

        public String getPriceFormatted() {
            return unitPrice == null ? "—" : String.format("%,.0f ₫", unitPrice);
        }

        public String getSubTotalFormatted() {
            return subTotal == null ? "—" : String.format("%,.0f ₫", subTotal);
        }
    }

    public String getSubtotalFormatted() {
        return subtotal == null ? "0 ₫" : String.format("%,.0f ₫", subtotal);
    }

    public String getShippingFeeFormatted() {
        return shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) == 0 ? "Free" : String.format("%,.0f ₫", shippingFee);
    }

    public String getTotalFormatted() {
        return total == null ? "0 ₫" : String.format("%,.0f ₫", total);
    }
}