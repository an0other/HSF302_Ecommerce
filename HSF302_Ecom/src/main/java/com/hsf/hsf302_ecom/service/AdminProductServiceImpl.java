package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.admin.InventoryFormDTO;
import com.hsf.hsf302_ecom.dto.admin.ProductFormDTO;
import com.hsf.hsf302_ecom.dto.admin.ProductRowDTO;
import com.hsf.hsf302_ecom.dto.admin.VariantFormDTO;
import com.hsf.hsf302_ecom.dto.admin.VariantRowDTO;
import com.hsf.hsf302_ecom.entity.*;
import com.hsf.hsf302_ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductsRepo        productsRepo;
    private final ProductVariantsRepo variantsRepo;
    private final InventoriesRepo     inventoriesRepo;
    private final CategoriesRepo      categoriesRepo;
    private final BrandsRepo          brandsRepo;

    private long availableStock(Long variantId) {
        Long val = inventoriesRepo.findAvailableStock(variantId);
        return val != null ? val : 0L;
    }

    private void autoActivateIfRestocked(ProductVariants v, long newAvailable) {
        if (newAvailable > 0) {
            if (!Boolean.TRUE.equals(v.getStatus())) {
                v.setStatus(true);
                variantsRepo.save(v);
            }
            Products p = v.getProduct();
            if (p != null && !Boolean.TRUE.equals(p.getStatus())) {
                p.setStatus(true);
                productsRepo.save(p);
            }
        }
    }

    private void autoDeactivateIfDepleted(ProductVariants v, long newStock) {
        if (newStock <= 0) {
            if (Boolean.TRUE.equals(v.getStatus())) {
                v.setStatus(false);
                variantsRepo.save(v);
            }
            Products p = v.getProduct();
            if (p != null && Boolean.TRUE.equals(p.getStatus())) {
                boolean anyOtherActive =
                        variantsRepo.existsOtherActiveVariant(p.getId(), v.getId());
                if (!anyOtherActive) {
                    p.setStatus(false);
                    productsRepo.save(p);
                }
            }
        }
    }

    @Override
    public Page<ProductRowDTO> getProducts(String keyword, Long categoryId, Long brandId, Pageable pageable) {
        Page<Products> page = productsRepo.findByFiltersAdmin(
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                categoryId, brandId, pageable);

        List<ProductRowDTO> rows = page.getContent().stream()
                .map(p -> {
                    List<ProductVariants> variants =
                            p.getProductVariants() != null ? p.getProductVariants() : List.of();
                    long totalStock = variants.stream()
                            .filter(v -> Boolean.TRUE.equals(v.getStatus()))
                            .mapToLong(v -> availableStock(v.getId()))
                            .sum();
                    BigDecimal lowest = variants.stream()
                            .filter(v -> Boolean.TRUE.equals(v.getStatus()) && v.getPrice() != null)
                            .map(ProductVariants::getPrice)
                            .min(BigDecimal::compareTo)
                            .orElse(null);
                    return ProductRowDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .categoryName(p.getCategory() != null ? p.getCategory().getName() : "—")
                            .brandName(p.getBrand()     != null ? p.getBrand().getName()     : "—")
                            .variantCount((int) variants.stream()
                                    .filter(v -> Boolean.TRUE.equals(v.getStatus())).count())
                            .totalStock(totalStock)
                            .status(Boolean.TRUE.equals(p.getStatus()))
                            .lowestPrice(lowest)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(rows, pageable, page.getTotalElements());
    }

    @Override
    public Optional<Products> findProductById(Long id) {
        return productsRepo.findById(id);
    }

    @Override
    public ProductFormDTO getProductForm(Long id) {
        Products p = productsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        ProductFormDTO form = new ProductFormDTO();
        form.setName(p.getName());
        form.setDescription(p.getDescription());
        form.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
        form.setBrandId(p.getBrand()     != null ? p.getBrand().getId()     : null);
        form.setStatus(p.getStatus());
        return form;
    }

    @Override
    @Transactional
    public String createProduct(ProductFormDTO form) {
        if (productsRepo.existsByNameIgnoreCase(form.getName()))
            return "A product with this name already exists.";
        Categories cat   = categoriesRepo.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Brands     brand = brandsRepo.findById(form.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        productsRepo.save(Products.builder()
                .name(form.getName().trim())
                .description(form.getDescription().trim())
                .category(cat).brand(brand)
                .status(false)
                .build());
        return null;
    }

    @Override
    @Transactional
    public String updateProduct(Long id, ProductFormDTO form) {
        Products p = productsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (productsRepo.existsByNameIgnoreCaseAndIdNot(form.getName(), id))
            return "A product with this name already exists.";

        boolean wantsActive     = Boolean.TRUE.equals(form.getStatus());
        boolean currentlyActive = Boolean.TRUE.equals(p.getStatus());

        if (wantsActive && !currentlyActive) {
            boolean hasAvailableActiveVariant = p.getProductVariants() != null &&
                    p.getProductVariants().stream()
                            .anyMatch(v -> Boolean.TRUE.equals(v.getStatus())
                                    && availableStock(v.getId()) > 0);
            if (!hasAvailableActiveVariant)
                return "Cannot activate — the product has no active variant with available stock "
                        + "(stock − reserved > 0). Add stock to a variant first.";
        }

        Categories cat   = categoriesRepo.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Brands     brand = brandsRepo.findById(form.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));

        boolean deactivating = !wantsActive && currentlyActive;

        p.setName(form.getName().trim());
        p.setDescription(form.getDescription().trim());
        p.setCategory(cat);
        p.setBrand(brand);
        p.setStatus(wantsActive);
        productsRepo.save(p);

        if (deactivating && p.getProductVariants() != null) {
            for (ProductVariants v : p.getProductVariants()) {
                if (Boolean.TRUE.equals(v.getStatus())) {
                    v.setStatus(false);
                    variantsRepo.save(v);
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public String softDeleteProduct(Long id) {
        Products p = productsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        p.setStatus(false);
        productsRepo.save(p);
        if (p.getProductVariants() != null) {
            for (ProductVariants v : p.getProductVariants()) {
                if (Boolean.TRUE.equals(v.getStatus())) {
                    v.setStatus(false);
                    variantsRepo.save(v);
                }
            }
        }
        return null;
    }

    @Override
    public boolean productHasActiveStock(Long id) {
        Products p = productsRepo.findById(id).orElse(null);
        if (p == null || p.getProductVariants() == null) return false;
        return p.getProductVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getStatus()))
                .anyMatch(v -> {
                    Inventories inv = inventoriesRepo.findByProductVariantId(v.getId()).orElse(null);
                    return inv != null && inv.getStock() > 0;
                });
    }

    @Override
    public List<VariantRowDTO> getVariantsForProduct(Long productId) {
        Products p = productsRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (p.getProductVariants() == null) return List.of();
        return p.getProductVariants().stream()
                .map(v -> {
                    Inventories inv   = inventoriesRepo.findByProductVariantId(v.getId()).orElse(null);
                    long stock    = inv != null ? inv.getStock()    : 0L;
                    long reserved = inv != null ? inv.getReserved() : 0L;
                    long avail    = Math.max(0, stock - reserved);
                    return VariantRowDTO.builder()
                            .id(v.getId())
                            .color(v.getColor()).spec(v.getSpec()).price(v.getPrice())
                            .status(Boolean.TRUE.equals(v.getStatus()))
                            .stock(stock).reserved(reserved).available(avail)
                            .build();
                })
                .sorted(Comparator.comparing(VariantRowDTO::getColor))
                .collect(Collectors.toList());
    }

    @Override
    public VariantFormDTO getVariantForm(Long variantId) {
        ProductVariants v = variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        VariantFormDTO form = new VariantFormDTO();
        form.setColor(v.getColor());
        form.setSpec(v.getSpec());
        form.setPrice(v.getPrice());
        form.setStatus(v.getStatus());
        form.setProductName(v.getProduct() != null ? v.getProduct().getName() : "—");
        return form;
    }

    @Override
    @Transactional
    public String createVariant(Long productId, VariantFormDTO form) {
        Products p = productsRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (variantsRepo.existsByProductIdAndColorIgnoreCaseAndSpecIgnoreCase(
                productId, form.getColor(), form.getSpec()))
            return "A variant with this color & spec already exists for this product.";

        ProductVariants v = ProductVariants.builder()
                .product(p)
                .color(form.getColor().trim())
                .spec(form.getSpec().trim())
                .price(form.getPrice())
                .status(false)
                .build();
        variantsRepo.save(v);
        inventoriesRepo.save(Inventories.builder()
                .productVariant(v).stock(0L).reserved(0L).build());
        return null;
    }

    @Override
    @Transactional
    public String updateVariant(Long variantId, VariantFormDTO form) {
        ProductVariants v = variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        if (variantsRepo.existsByProductIdAndColorIgnoreCaseAndSpecIgnoreCaseAndIdNot(
                v.getProduct().getId(), form.getColor(), form.getSpec(), variantId))
            return "A variant with this color & spec already exists for this product.";

        boolean wantsActive     = Boolean.TRUE.equals(form.getStatus());
        boolean currentlyActive = Boolean.TRUE.equals(v.getStatus());

        if (wantsActive && !currentlyActive) {
            if (availableStock(variantId) <= 0)
                return "Cannot activate — this variant has no available stock "
                        + "(stock − reserved must be > 0). Add inventory first.";
            Products p = v.getProduct();
            if (p != null && !Boolean.TRUE.equals(p.getStatus()))
                return "Cannot activate — the parent product is inactive. "
                        + "Activate the product first, or add stock (which auto-activates both).";
        }

        if (!wantsActive && currentlyActive && variantHasActiveStock(variantId))
            return "Cannot deactivate — this variant still has stock in inventory.";

        v.setColor(form.getColor().trim());
        v.setSpec(form.getSpec().trim());
        v.setPrice(form.getPrice());
        v.setStatus(wantsActive);
        variantsRepo.save(v);
        return null;
    }

    @Override
    @Transactional
    public String softDeleteVariant(Long variantId) {
        if (variantHasActiveStock(variantId))
            return "Cannot deactivate — this variant still has stock in inventory.";
        ProductVariants v = variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        v.setStatus(false);
        variantsRepo.save(v);
        return null;
    }

    @Override
    public boolean variantHasActiveStock(Long variantId) {
        Inventories inv = inventoriesRepo.findByProductVariantId(variantId).orElse(null);
        return inv != null && inv.getStock() > 0;
    }

    @Override
    public long getAvailableStockForVariant(Long variantId) {
        return availableStock(variantId);
    }

    @Override
    public InventoryFormDTO getInventoryFormForVariant(Long variantId) {
        ProductVariants v   = variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        Inventories     inv = inventoriesRepo.findByProductVariantId(variantId).orElse(null);
        InventoryFormDTO form = new InventoryFormDTO();
        form.setProductName(v.getProduct() != null ? v.getProduct().getName() : "—");
        form.setVariantLabel(v.getColor() + " / " + v.getSpec());
        form.setColor(v.getColor());
        form.setSpec(v.getSpec());
        form.setStock(inv != null ? inv.getStock()    : 0L);
        form.setReserved(inv != null ? inv.getReserved() : 0L);
        return form;
    }

    @Override
    @Transactional
    public void createOrUpdateInventory(Long variantId, InventoryFormDTO form) {
        if (form.getReserved() > form.getStock())
            throw new IllegalArgumentException("Reserved cannot exceed stock.");

        ProductVariants v = variantsRepo.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        Inventories inv = inventoriesRepo.findByProductVariantId(variantId)
                .orElseGet(() -> Inventories.builder().productVariant(v).build());

        long newStock    = form.getStock();
        long newReserved = form.getReserved();
        long newAvailable = Math.max(0, newStock - newReserved);

        inv.setStock(newStock);
        inv.setReserved(newReserved);
        inventoriesRepo.save(inv);

        autoActivateIfRestocked(v, newAvailable);

        autoDeactivateIfDepleted(v, newStock);
    }
}