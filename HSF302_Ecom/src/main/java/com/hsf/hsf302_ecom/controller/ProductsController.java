package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.HomeProductDTO;
import com.hsf.hsf302_ecom.entity.Brands;
import com.hsf.hsf302_ecom.entity.Categories;
import com.hsf.hsf302_ecom.service.ProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productService;

    @GetMapping
    public String products(
            @RequestParam(required = false)                Long       categoryId,
            @RequestParam(required = false)                Long       brandId,
            @RequestParam(required = false, defaultValue = "")        String     keyword,
            @RequestParam(required = false)                String     sort,
            @RequestParam(required = false)                BigDecimal minPrice,
            @RequestParam(required = false)                BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "0")       int        page,
            Model model
    ) {
        if (page < 0) page = 0;

        String kw = keyword.isBlank() ? null : keyword;

        Page<HomeProductDTO> productPage = productService.getProducts(
                categoryId, brandId, kw, sort, minPrice, maxPrice, page);

        if (productPage.getTotalPages() > 0 && page >= productPage.getTotalPages()) {
            page = productPage.getTotalPages() - 1;
            productPage = productService.getProducts(
                    categoryId, brandId, kw, sort, minPrice, maxPrice, page);
        }

        List<Brands> brands = productService.getBrandsByCategory(categoryId);

        model.addAttribute("categories",      productService.getActiveCategories());
        model.addAttribute("productPage",     productPage);
        model.addAttribute("currentCategory", productService.getCategoryById(categoryId));
        model.addAttribute("categoryId",      categoryId);
        model.addAttribute("brands",          brands);
        model.addAttribute("brandId",         brandId);
        model.addAttribute("keyword",         keyword);
        model.addAttribute("sort",            sort);
        model.addAttribute("minPrice",        minPrice);
        model.addAttribute("maxPrice",        maxPrice);
        model.addAttribute("currentPage",     page);
        model.addAttribute("totalPages",      productPage.getTotalPages());
        model.addAttribute("totalElements",   productPage.getTotalElements());

        return "products";
    }
}