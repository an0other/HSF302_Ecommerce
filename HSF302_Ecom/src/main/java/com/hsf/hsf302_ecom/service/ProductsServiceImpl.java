package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.dto.ProductDetailDTO;
import com.hsf.hsf302_ecom.entity.*;
import com.hsf.hsf302_ecom.repository.BrandsRepo;
import com.hsf.hsf302_ecom.repository.CategoriesRepo;
import com.hsf.hsf302_ecom.repository.InventoriesRepo;
import com.hsf.hsf302_ecom.repository.ProductsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductsServiceImpl implements ProductsService {

    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final CategoriesRepo categoriesRepo;
    private final ProductsRepo   productsRepo;
    private final BrandsRepo     brandsRepo;
    private final InventoriesRepo inventoriesRepo;

    @Override
    public List<Categories> getActiveCategories() {
        return categoriesRepo.findByStatusTrueOrderByNameAsc();
    }

    @Override
    public Page<HomeProductDTO> getProducts(Long categoryId, Long brandId, String keyword,
                                            String sort, BigDecimal minPrice, BigDecimal maxPrice,
                                            int page) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return productsRepo.findByFilter(categoryId, brandId, kw, sort, minPrice, maxPrice,
                PageRequest.of(page, PAGE_SIZE));
    }

    @Override
    public Categories getCategoryById(Long id) {
        if (id == null) return null;
        return categoriesRepo.findById(id).orElse(null);
    }

    @Override
    public List<Brands> getBrandsByCategory(Long categoryId) {
        if (categoryId == null) return Collections.emptyList();
        return brandsRepo.findDistinctByProductsStatusTrueAndProductsCategoryIdOrderByNameAsc(categoryId);
    }

    @Override
    public Optional<ProductDetailDTO> getProductDetail(Long productId) {
        return productsRepo.findById(productId)
                .filter(Products::getStatus)
                .map(this::toDTO);
    }

    private ProductDetailDTO toDTO(Products p) {

        List<ProductDetailDTO.ImageDTO> images = p.getProductImages().stream()
                .map(img -> new ProductDetailDTO.ImageDTO(
                        img.getId(), img.getImageUrl(), Boolean.TRUE.equals(img.getIsPrimary())))
                .sorted(Comparator.comparing(i -> i.isPrimary() ? 0 : 1))
                .collect(Collectors.toList());

        List<ProductDetailDTO.VariantDTO> variants = p.getProductVariants().stream()
                .sorted(Comparator.comparing(ProductVariants::getPrice))
                .map(v -> {
                    long avail = inventoriesRepo
                            .findByProductVariantId(v.getId())
                            .map(inv -> Math.max(0L, inv.getStock() - inv.getReserved()))
                            .orElse(0L);
                    return new ProductDetailDTO.VariantDTO(
                            v.getId(), v.getColor(), v.getSpec(), v.getPrice(),
                            Boolean.TRUE.equals(v.getStatus()), avail);
                })
                .collect(Collectors.toList());

        List<ProductDetailDTO.ReviewDTO> reviews = p.getReviews().stream()
                .map(r -> new ProductDetailDTO.ReviewDTO(
                        r.getId(),
                        r.getUser() != null ? r.getUser().getUsername() : "Anonymous",
                        r.getRating(),
                        r.getComment(),
                        r.getCreatedAt() != null ? r.getCreatedAt().format(FMT) : ""))
                .sorted(Comparator.comparing(ProductDetailDTO.ReviewDTO::getId).reversed())
                .collect(Collectors.toList());

        double avgRating = p.getReviews().stream()
                .mapToInt(Reviews::getRating)
                .average().orElse(0.0);
        long reviewCount = p.getReviews().size();

        return new ProductDetailDTO(
                p.getId(), p.getName(), p.getDescription(),
                p.getBrand()    != null ? p.getBrand().getName()    : "",
                p.getCategory() != null ? p.getCategory().getName() : "",
                p.getCategory() != null ? p.getCategory().getId()   : null,
                avgRating, reviewCount, 0L,
                images, variants, reviews);
    }
}