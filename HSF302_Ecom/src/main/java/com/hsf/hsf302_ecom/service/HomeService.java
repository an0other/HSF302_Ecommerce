package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Categories;

import java.util.List;
import java.util.Map;

public interface HomeService {

    /** All active categories (for nav strip + search dropdown) */
    List<Categories> getActiveCategories();

    /**
     * For each category that has active products, return an ordered list
     * of its newest products. Key = category entity, Value = product list.
     */
    Map<Categories, List<HomeProductDTO>> getCarouselsByCategory();
}