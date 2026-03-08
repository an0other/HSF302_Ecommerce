package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Brands;
import com.hsf.hsf302_ecom.entity.Categories;
import com.hsf.hsf302_ecom.repository.BrandsRepo;
import com.hsf.hsf302_ecom.repository.CategoriesRepo;
import com.hsf.hsf302_ecom.repository.ProductsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductsServiceImpl implements ProductsService {

    private static final int PAGE_SIZE = 8;

    private final CategoriesRepo categoriesRepo;
    private final ProductsRepo   productsRepo;
    private final BrandsRepo     brandsRepo;

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
}