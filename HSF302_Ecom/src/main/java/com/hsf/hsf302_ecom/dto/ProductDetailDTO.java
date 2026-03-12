package com.hsf.hsf302_ecom.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductDetailDTO {

    private final Long   productId;
    private final String productName;
    private final String description;
    private final String brandName;
    private final String categoryName;
    private final Long   categoryId;
    private final Double avgRating;
    private final Long   reviewCount;
    private final Long   soldCount;

    private final List<ImageDTO>   images;
    private final List<VariantDTO> variants;
    private final List<ReviewDTO>  reviews;

    public ProductDetailDTO(Long productId, String productName, String description,
                            String brandName, String categoryName, Long categoryId,
                            Double avgRating, Long reviewCount, Long soldCount,
                            List<ImageDTO> images, List<VariantDTO> variants, List<ReviewDTO> reviews) {
        this.productId    = productId;
        this.productName  = productName;
        this.description  = description;
        this.brandName    = brandName;
        this.categoryName = categoryName;
        this.categoryId   = categoryId;
        this.avgRating    = avgRating != null ? avgRating : 0.0;
        this.reviewCount  = reviewCount != null ? reviewCount : 0L;
        this.soldCount    = soldCount  != null ? soldCount  : 0L;
        this.images       = images;
        this.variants     = variants;
        this.reviews      = reviews;
    }

    public int getStarFillPct() { return (int) Math.round((avgRating / 5.0) * 100); }
    public String getAvgRatingFormatted() { return String.format("%.1f", avgRating); }

    public Long   getProductId()    { return productId; }
    public String getProductName()  { return productName; }
    public String getDescription()  { return description; }
    public String getBrandName()    { return brandName; }
    public String getCategoryName() { return categoryName; }
    public Long   getCategoryId()   { return categoryId; }
    public Double getAvgRating()    { return avgRating; }
    public Long   getReviewCount()  { return reviewCount; }
    public Long   getSoldCount()    { return soldCount; }
    public List<ImageDTO>   getImages()   { return images; }
    public List<VariantDTO> getVariants() { return variants; }
    public List<ReviewDTO>  getReviews()  { return reviews; }

    public static class ImageDTO {
        private final Long    id;
        private final String  imageUrl;
        private final boolean isPrimary;

        public ImageDTO(Long id, String imageUrl, boolean isPrimary) {
            this.id = id; this.imageUrl = imageUrl; this.isPrimary = isPrimary;
        }
        public Long    getId()       { return id; }
        public String  getImageUrl() { return imageUrl; }
        public boolean isPrimary()   { return isPrimary; }
    }

    public static class VariantDTO {
        private final Long       id;
        private final String     color;
        private final String     spec;
        private final BigDecimal price;
        private final boolean    active;
        private final long       availableStock;  // stock - reserved

        public VariantDTO(Long id, String color, String spec, BigDecimal price,
                          boolean active, long availableStock) {
            this.id             = id;
            this.color          = color;
            this.spec           = spec;
            this.price          = price;
            this.active         = active;
            this.availableStock = availableStock;
        }

        public boolean isAvailable() { return active && availableStock > 0; }

        public Long       getId()             { return id; }
        public String     getColor()          { return color; }
        public String     getSpec()           { return spec; }
        public BigDecimal getPrice()          { return price; }
        public boolean    isActive()          { return active; }
        public long       getAvailableStock() { return availableStock; }

        public String getPriceFormatted() {
            if (price == null) return "Contact for price";
            return String.format("%,.0f ₫", price);
        }
    }

    public static class ReviewDTO {
        private final Long   id;
        private final String username;
        private final byte   rating;
        private final String comment;
        private final String createdAt;

        public ReviewDTO(Long id, String username, byte rating, String comment, String createdAt) {
            this.id = id; this.username = username; this.rating = rating;
            this.comment = comment; this.createdAt = createdAt;
        }
        public int getStarFillPct() { return (int) Math.round((rating / 5.0) * 100); }

        public Long   getId()        { return id; }
        public String getUsername()  { return username; }
        public byte   getRating()    { return rating; }
        public String getComment()   { return comment; }
        public String getCreatedAt() { return createdAt; }
    }
}