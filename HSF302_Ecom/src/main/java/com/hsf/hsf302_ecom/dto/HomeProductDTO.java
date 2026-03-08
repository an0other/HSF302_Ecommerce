package com.hsf.hsf302_ecom.dto;

import java.math.BigDecimal;

public class HomeProductDTO {

    private final Long   productId;
    private final String productName;
    private final String brandName;
    private final String categoryName;
    private final Long   categoryId;
    private final String imageUrl;
    private final BigDecimal lowestPrice;
    private final Double avgRating;
    private final Long   soldCount;

    public HomeProductDTO(
            Long productId,
            String productName,
            String brandName,
            String categoryName,
            Long categoryId,
            String imageUrl,
            BigDecimal lowestPrice,
            Double avgRating,
            Long soldCount
    ) {
        this.productId    = productId;
        this.productName  = productName;
        this.brandName    = brandName;
        this.categoryName = categoryName;
        this.categoryId   = categoryId;
        this.imageUrl     = imageUrl;
        this.lowestPrice  = lowestPrice;
        this.avgRating    = avgRating != null ? avgRating : 0.0;
        this.soldCount    = soldCount  != null ? soldCount  : 0L;
    }

    public int getStarFillPct() {
        return (int) Math.round((avgRating / 5.0) * 100);
    }

    public String getAvgRatingFormatted() {
        return String.format("%.1f", avgRating);
    }

    /* ── Getters ─────────────────────────────────────────────────── */
    public Long       getProductId()    { return productId; }
    public String     getProductName()  { return productName; }
    public String     getBrandName()    { return brandName; }
    public String     getCategoryName() { return categoryName; }
    public Long       getCategoryId()   { return categoryId; }
    public String     getImageUrl()     { return imageUrl; }
    public BigDecimal getLowestPrice()  { return lowestPrice; }
    public Double     getAvgRating()    { return avgRating; }
    public Long       getSoldCount()    { return soldCount; }
}