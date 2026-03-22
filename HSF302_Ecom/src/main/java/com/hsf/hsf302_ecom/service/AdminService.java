package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.admin.*;
import com.hsf.hsf302_ecom.entity.Brands;
import com.hsf.hsf302_ecom.entity.Categories;
import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    DashboardStatsDTO getDashboardStats();

    Page<Brands> getBrands(String keyword, Pageable pageable);
    BrandFormDTO getBrandForm(Long id);
    String createBrand(BrandFormDTO form);
    String updateBrand(Long id, BrandFormDTO form);

    boolean brandHasInventory(Long id);

    List<Brands> getAllBrands();

    Page<Categories> getCategories(String keyword, Pageable pageable);
    CategoryFormDTO getCategoryForm(Long id);
    String createCategory(CategoryFormDTO form);
    String updateCategory(Long id, CategoryFormDTO form);


    boolean categoryHasInventory(Long id);

    List<Categories> getAllCategories();

    Page<InventoryRowDTO> getInventories(String keyword, Long categoryId, Long brandId, Pageable pageable);
    InventoryFormDTO getInventoryForm(Long variantId);
    void updateInventory(Long variantId, InventoryFormDTO form);
    void restockInventory(Long variantId, Long amount);

    List<Orders> getAllOrders();
    List<Orders> getOrdersByStatus(OrderStatus status);
    Orders getOrderById(Long id);
    void updateOrderStatus(Long orderId, OrderStatus status);
}