package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", homeService.getActiveCategories());
        model.addAttribute("carousels",  homeService.getCarouselsByCategory());
        return "index";
    }
}