package com.hsf.hsf302_ecom.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long totalBrands;
    private long activeBrands;
    private long totalCategories;
    private long activeCategories;
    private long totalProducts;
    private long totalVariants;
    private long lowStockVariants;
    private long outOfStockVariants;
    private long totalOrders;
    private long totalUsers;
}