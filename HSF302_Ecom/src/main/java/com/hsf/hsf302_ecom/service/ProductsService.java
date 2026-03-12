package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.dto.ProductDetailDTO;
import com.hsf.hsf302_ecom.entity.Brands;
import com.hsf.hsf302_ecom.entity.Categories;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductsService {

    List<Categories> getActiveCategories();

    Page<HomeProductDTO> getProducts(Long categoryId, Long brandId, String keyword,
                                     String sort, BigDecimal minPrice, BigDecimal maxPrice, int page);

    Categories getCategoryById(Long id);

    List<Brands> getBrandsByCategory(Long categoryId);

    Optional<ProductDetailDTO> getProductDetail(Long productId);

}