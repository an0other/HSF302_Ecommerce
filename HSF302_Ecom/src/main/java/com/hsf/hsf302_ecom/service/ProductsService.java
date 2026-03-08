package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Brands;
import com.hsf.hsf302_ecom.entity.Categories;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductsService {

    List<Categories> getActiveCategories();

    Page<HomeProductDTO> getProducts(Long categoryId, Long brandId, String keyword,
                                     String sort, Integer minRating, int page);

    Categories getCategoryById(Long id);

    List<Brands> getBrandsByCategory(Long categoryId);
}