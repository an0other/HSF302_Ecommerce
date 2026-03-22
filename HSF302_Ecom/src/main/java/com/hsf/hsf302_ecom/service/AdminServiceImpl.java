package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.admin.*;
import com.hsf.hsf302_ecom.entity.*;
import com.hsf.hsf302_ecom.enums.OrderStatus;
import com.hsf.hsf302_ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final BrandsRepo          brandsRepo;
    private final CategoriesRepo      categoriesRepo;
    private final ProductsRepo        productsRepo;
    private final ProductVariantsRepo variantsRepo;
    private final InventoriesRepo     inventoriesRepo;
    private final OrdersRepo          ordersRepo;
    private final UsersRepo           usersRepo;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        return DashboardStatsDTO.builder()
                .totalBrands(brandsRepo.count())
                .activeBrands(brandsRepo.countByStatusTrue())
                .totalCategories(categoriesRepo.count())
                .activeCategories(categoriesRepo.countByStatusTrue())
                .totalProducts(productsRepo.count())
                .totalVariants(variantsRepo.count())
                .lowStockVariants(inventoriesRepo.countLowStock())
                .outOfStockVariants(inventoriesRepo.countOutOfStock())
                .totalOrders(ordersRepo.count())
                .totalUsers(usersRepo.count())
                .build();
    }

    @Override
    public Page<Brands> getBrands(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank())
            return brandsRepo.findAllByOrderByNameAsc(pageable);
        return brandsRepo.findByNameContainingIgnoreCaseOrderByNameAsc(keyword, pageable);
    }

    @Override
    public BrandFormDTO getBrandForm(Long id) {
        Brands b = brandsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        return new BrandFormDTO(b.getName(), b.getStatus());
    }

    @Override
    @Transactional
    public String createBrand(BrandFormDTO form) {
        if (brandsRepo.existsByNameIgnoreCase(form.getName()))
            return "A brand with this name already exists.";
        brandsRepo.save(Brands.builder()
                .name(form.getName().trim())
                .status(Boolean.TRUE.equals(form.getStatus()))
                .build());
        return null;
    }

    @Override
    @Transactional
    public String updateBrand(Long id, BrandFormDTO form) {
        Brands brand = brandsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        if (brandsRepo.existsByNameIgnoreCaseAndIdNot(form.getName(), id))
            return "A brand with this name already exists.";
        if (!Boolean.TRUE.equals(form.getStatus()) && brandHasInventory(id))
            return "Cannot deactivate \"" + brand.getName() + "\" — it still has products with stock in inventory. Please clear all inventory first.";
        brand.setName(form.getName().trim());
        brand.setStatus(Boolean.TRUE.equals(form.getStatus()));
        brandsRepo.save(brand);
        return null;
    }

    @Override
    public boolean brandHasInventory(Long id) {
        Brands brand = brandsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        if (brand.getProducts() == null || brand.getProducts().isEmpty()) return false;
        for (Products product : brand.getProducts()) {
            if (product.getProductVariants() == null) continue;
            for (ProductVariants variant : product.getProductVariants()) {
                Inventories inv = inventoriesRepo.findByProductVariantId(variant.getId()).orElse(null);
                if (inv != null && inv.getStock() > 0) return true;
            }
        }
        return false;
    }

    @Override
    public List<Brands> getAllBrands() {
        return brandsRepo.findAll(Sort.by("name"));
    }

    @Override
    public Page<Categories> getCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank())
            return categoriesRepo.findAllByOrderByNameAsc(pageable);
        return categoriesRepo.findByNameContainingIgnoreCaseOrderByNameAsc(keyword, pageable);
    }

    @Override
    public CategoryFormDTO getCategoryForm(Long id) {
        Categories c = categoriesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return new CategoryFormDTO(c.getName(), c.getStatus());
    }

    @Override
    @Transactional
    public String createCategory(CategoryFormDTO form) {
        if (categoriesRepo.existsByNameIgnoreCase(form.getName()))
            return "A category with this name already exists.";
        categoriesRepo.save(Categories.builder()
                .name(form.getName().trim())
                .status(Boolean.TRUE.equals(form.getStatus()))
                .build());
        return null;
    }

    @Override
    @Transactional
    public String updateCategory(Long id, CategoryFormDTO form) {
        Categories cat = categoriesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (categoriesRepo.existsByNameIgnoreCaseAndIdNot(form.getName(), id))
            return "A category with this name already exists.";
        if (!Boolean.TRUE.equals(form.getStatus()) && categoryHasInventory(id))
            return "Cannot deactivate \"" + cat.getName() + "\" — it still has products with stock in inventory. Please clear all inventory first.";
        cat.setName(form.getName().trim());
        cat.setStatus(Boolean.TRUE.equals(form.getStatus()));
        categoriesRepo.save(cat);
        return null;
    }

    @Override
    public boolean categoryHasInventory(Long id) {
        Categories cat = categoriesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (cat.getProducts() == null || cat.getProducts().isEmpty()) return false;
        for (Products product : cat.getProducts()) {
            if (product.getProductVariants() == null) continue;
            for (ProductVariants variant : product.getProductVariants()) {
                Inventories inv = inventoriesRepo.findByProductVariantId(variant.getId()).orElse(null);
                if (inv != null && inv.getStock() > 0) return true;
            }
        }
        return false;
    }

    @Override
    public List<Categories> getAllCategories() {
        return categoriesRepo.findAll(Sort.by("name"));
    }

    @Override
    public Page<InventoryRowDTO> getInventories(String keyword, Long categoryId, Long brandId, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Inventories> page = inventoriesRepo.findByFilters(kw, categoryId, brandId, pageable);

        List<InventoryRowDTO> rows = page.getContent().stream()
                .map(inv -> {
                    ProductVariants v = inv.getProductVariant();
                    Products p = v.getProduct();
                    long avail = Math.max(0, inv.getStock() - inv.getReserved());
                    return InventoryRowDTO.builder()
                            .variantId(v.getId())
                            .productId(p.getId())
                            .productName(p.getName())
                            .brandName(p.getBrand()       != null ? p.getBrand().getName()    : "—")
                            .categoryName(p.getCategory() != null ? p.getCategory().getName() : "—")
                            .color(v.getColor())
                            .spec(v.getSpec())
                            .price(v.getPrice())
                            .stock(inv.getStock())
                            .reserved(inv.getReserved())
                            .available(avail)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(rows, pageable, page.getTotalElements());
    }

    private int stockOrder(String s) {
        return switch (s) { case "OUT" -> 0; case "LOW" -> 1; default -> 2; };
    }

    @Override
    public InventoryFormDTO getInventoryForm(Long variantId) {
        ProductVariants v = variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        Inventories inv = inventoriesRepo.findByProductVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for variant " + variantId));
        InventoryFormDTO form = new InventoryFormDTO();
        form.setProductName(v.getProduct() != null ? v.getProduct().getName() : "—");
        form.setVariantLabel(v.getColor() + " / " + v.getSpec());
        form.setColor(v.getColor());
        form.setSpec(v.getSpec());
        form.setStock(inv.getStock());
        form.setReserved(inv.getReserved());
        return form;
    }

    @Override
    @Transactional
    public void updateInventory(Long variantId, InventoryFormDTO form) {
        Inventories inv = inventoriesRepo.findByProductVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        if (form.getReserved() > form.getStock())
            throw new IllegalArgumentException("Reserved cannot exceed stock.");
        inv.setStock(form.getStock());
        inv.setReserved(form.getReserved());
        inventoriesRepo.save(inv);
    }

    @Override
    @Transactional
    public void restockInventory(Long variantId, Long amount) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive.");
        Inventories inv = inventoriesRepo.findByProductVariantId(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inv.setStock(inv.getStock() + amount);
        inventoriesRepo.save(inv);
    }

    @Override
    public List<Orders> getAllOrders() {
        return ordersRepo.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Orders> getOrdersByStatus(OrderStatus status) {
        return ordersRepo.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public Orders getOrderById(Long id) {
        return ordersRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }
    @Transactional
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setStatus(status);
        ordersRepo.save(order);
    }
}