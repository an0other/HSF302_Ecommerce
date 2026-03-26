package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.admin.InventoryFormDTO;
import com.hsf.hsf302_ecom.dto.admin.ProductFormDTO;
import com.hsf.hsf302_ecom.dto.admin.ProductRowDTO;
import com.hsf.hsf302_ecom.dto.admin.VariantFormDTO;
import com.hsf.hsf302_ecom.dto.admin.VariantRowDTO;
import com.hsf.hsf302_ecom.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AdminProductService {

    Page<ProductRowDTO> getProducts(String keyword, Long categoryId, Long brandId, Pageable pageable);
    Optional<Products>  findProductById(Long id);
    ProductFormDTO      getProductForm(Long id);
    String              createProduct(ProductFormDTO form);
    String              updateProduct(Long id, ProductFormDTO form);
    String              softDeleteProduct(Long id);
    boolean             productHasActiveStock(Long id);

    List<VariantRowDTO> getVariantsForProduct(Long productId);
    VariantFormDTO      getVariantForm(Long variantId);
    String              createVariant(Long productId, VariantFormDTO form);
    String              updateVariant(Long variantId, VariantFormDTO form);
    String              softDeleteVariant(Long variantId);
    boolean             variantHasActiveStock(Long variantId);

    /** Returns stock − reserved for a variant (never negative). */
    long                getAvailableStockForVariant(Long variantId);

    InventoryFormDTO getInventoryFormForVariant(Long variantId);
    void             createOrUpdateInventory(Long variantId, InventoryFormDTO form);
}