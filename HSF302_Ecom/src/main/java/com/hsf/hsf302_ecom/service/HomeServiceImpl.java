package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Categories;
import com.hsf.hsf302_ecom.repository.CategoriesRepo;
import com.hsf.hsf302_ecom.repository.ProductsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private final CategoriesRepo categoriesRepo;
    private final ProductsRepo   productsRepo;

    @Override
    public List<Categories> getActiveCategories() {
        return categoriesRepo.findByStatusTrueOrderByNameAsc();
    }

    @Override
    public Map<Categories, List<HomeProductDTO>> getCarouselsByCategory() {
        List<Categories> cats = categoriesRepo.findDistinctByStatusTrueAndProductsStatusTrueOrderByNameAsc();
        Map<Categories, List<HomeProductDTO>> result = new LinkedHashMap<>();
        for (Categories cat : cats) {
            List<HomeProductDTO> products = productsRepo.findNewestByCategory(cat.getId());
            if (!products.isEmpty()) {
                result.put(cat, products);
            }
        }
        return result;
    }
}