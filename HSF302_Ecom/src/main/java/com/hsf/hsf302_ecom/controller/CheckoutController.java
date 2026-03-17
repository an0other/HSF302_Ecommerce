package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.CheckoutRequestDTO;
import com.hsf.hsf302_ecom.dto.CheckoutSummaryDTO;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.service.CheckoutService;
import com.hsf.hsf302_ecom.service.HomeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private static final String SK_USER = "loggedInUser";

    private final CheckoutService checkoutService;
    private final HomeService     homeService;

    private Users currentUser(HttpSession session) {
        return (Users) session.getAttribute(SK_USER);
    }

    @GetMapping
    public String checkoutPage(HttpSession session, Model model) {
        Users user = currentUser(session);
        if (user == null) return "redirect:/login";

        CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(user.getId());
        if (summary.isCartEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("summary",             summary);
        model.addAttribute("checkoutRequest",     new CheckoutRequestDTO());
        model.addAttribute("categories",          homeService.getActiveCategories());
        return "checkout";
    }

    @PostMapping
    public String placeOrder(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO request,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             RedirectAttributes ra) {

        Users user = currentUser(session);
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(user.getId());
            model.addAttribute("summary",     summary);
            model.addAttribute("categories",  homeService.getActiveCategories());
            return "checkout";
        }

        try {
            Long orderId = checkoutService.placeOrder(user.getId(), request);
            ra.addFlashAttribute("toast",     "Order #" + orderId + " placed successfully! 🎉");
            ra.addFlashAttribute("toastType", "success");
            return "redirect:/orders/" + orderId;

        } catch (IllegalStateException e) {
            CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(user.getId());
            model.addAttribute("summary",       summary);
            model.addAttribute("categories",    homeService.getActiveCategories());
            model.addAttribute("checkoutError", e.getMessage());
            return "checkout";
        }
    }
}