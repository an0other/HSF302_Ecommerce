package com.hsf.hsf302_ecom.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private final Long       cartItemId;
    private final Long       productId;
    private final Long       variantId;
    private final String     productName;
    private final String     brandName;
    private final String     color;
    private final String     spec;
    private final BigDecimal unitPrice;
    private final Long       quantity;
    private final BigDecimal subTotal;
    private final String     imageUrl;
    private final Long       availableStock;

    public CartItemDTO(Long cartItemId, Long productId, Long variantId,
                       String productName, String brandName,
                       String color, String spec,
                       BigDecimal unitPrice, Long quantity,
                       BigDecimal subTotal, String imageUrl,
                       Long availableStock) {
        this.cartItemId     = cartItemId;
        this.productId      = productId;
        this.variantId      = variantId;
        this.productName    = productName;
        this.brandName      = brandName;
        this.color          = color;
        this.spec           = spec;
        this.unitPrice      = unitPrice;
        this.quantity       = quantity;
        this.subTotal       = subTotal;
        this.imageUrl       = imageUrl;
        this.availableStock = availableStock;
    }

    public String getPriceFormatted() {
        return unitPrice == null ? "—" : String.format("%,.0f ₫", unitPrice);
    }

    public String getSubTotalFormatted() {
        return subTotal == null ? "—" : String.format("%,.0f ₫", subTotal);
    }

    public Long       getCartItemId()     { return cartItemId; }
    public Long       getProductId()      { return productId; }
    public Long       getVariantId()      { return variantId; }
    public String     getProductName()    { return productName; }
    public String     getBrandName()      { return brandName; }
    public String     getColor()          { return color; }
    public String     getSpec()           { return spec; }
    public BigDecimal getUnitPrice()      { return unitPrice; }
    public Long       getQuantity()       { return quantity; }
    public BigDecimal getSubTotal()       { return subTotal; }
    public String     getImageUrl()       { return imageUrl; }
    public Long       getAvailableStock() { return availableStock; }
}