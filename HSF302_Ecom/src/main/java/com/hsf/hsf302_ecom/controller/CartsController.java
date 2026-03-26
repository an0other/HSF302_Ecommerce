package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.CartItemDTO;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserRole;
import com.hsf.hsf302_ecom.service.CartsService;
import com.hsf.hsf302_ecom.service.HomeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartsController {

    private static final String SK_USER = "loggedInUser";

    private final CartsService cartsService;
    private final HomeService  homeService;

    private Users currentUser(HttpSession session) {
        return (Users) session.getAttribute(SK_USER);
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        Users user = currentUser(session);

        if (user != null && user.getRole() == UserRole.ADMIN) return "redirect:/admin";

        if (user == null) {
            model.addAttribute("categories", homeService.getActiveCategories());
            model.addAttribute("cartItems",  Collections.emptyList());
            model.addAttribute("cartTotal",  BigDecimal.ZERO);
            model.addAttribute("itemCount",  0L);
            model.addAttribute("isGuest",    true);
            return "cart";
        }

        List<CartItemDTO> items = cartsService.getCartItems(user.getId());
        BigDecimal        total = cartsService.getCartTotal(user.getId());
        long              count = items.stream().mapToLong(CartItemDTO::getQuantity).sum();

        model.addAttribute("categories", homeService.getActiveCategories());
        model.addAttribute("cartItems",  items);
        model.addAttribute("cartTotal",  total);
        model.addAttribute("itemCount",  count);
        model.addAttribute("isGuest",    false);
        return "cart";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long variantId,
            @RequestParam(defaultValue = "1") Long qty,
            HttpSession session) {

        Users user = currentUser(session);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Please sign in to add items to your cart."));
        }
        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Admins cannot place orders."));
        }

        try {
            cartsService.addToCart(user.getId(), variantId, qty);
            long cartCount = cartsService.getCartItemCount(user.getId());
            return ResponseEntity.ok(Map.of(
                    "success",   true,
                    "message",   "Added to cart!",
                    "cartCount", cartCount
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Something went wrong. Please try again."));
        }
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateItem(
            @RequestParam Long  cartItemId,
            @RequestParam Long  qty,
            HttpSession session) {

        Users user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("success", false));
        if (user.getRole() == UserRole.ADMIN) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admins cannot place orders."));

        try {
            cartsService.updateQuantity(user.getId(), cartItemId, qty);
            List<CartItemDTO> items = cartsService.getCartItems(user.getId());
            BigDecimal total = cartsService.getCartTotal(user.getId());
            long count = items.stream().mapToLong(CartItemDTO::getQuantity).sum();

            CartItemDTO updatedItem = items.stream()
                    .filter(i -> i.getCartItemId().equals(cartItemId))
                    .findFirst()
                    .orElse(null);

            BigDecimal itemSub     = updatedItem != null ? updatedItem.getSubTotal()       : BigDecimal.ZERO;
            Long       avail       = updatedItem != null ? updatedItem.getAvailableStock()  : 0L;

            return ResponseEntity.ok(Map.of(
                    "success",          true,
                    "cartCount",        count,
                    "cartTotal",        String.format("%,.0f ₫", total),
                    "itemSubTotal",     String.format("%,.0f ₫", itemSub),
                    "availableStock",   avail
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeItem(
            @RequestParam Long cartItemId,
            HttpSession session) {

        Users user = currentUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("success", false));
        if (user.getRole() == UserRole.ADMIN) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admins cannot place orders."));

        try {
            cartsService.removeItem(user.getId(), cartItemId);
            List<CartItemDTO> items = cartsService.getCartItems(user.getId());
            BigDecimal total = cartsService.getCartTotal(user.getId());
            long count = items.stream().mapToLong(CartItemDTO::getQuantity).sum();

            return ResponseEntity.ok(Map.of(
                    "success",   true,
                    "cartCount", count,
                    "cartTotal", String.format("%,.0f ₫", total),
                    "isEmpty",   items.isEmpty()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes ra) {
        Users user = currentUser(session);
        if (user != null && user.getRole() == UserRole.ADMIN) return "redirect:/admin";
        if (user != null) {
            cartsService.clearCart(user.getId());
            ra.addFlashAttribute("toast", "Your cart has been cleared.");
            ra.addFlashAttribute("toastType", "info");
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cartCount(HttpSession session) {
        Users user = currentUser(session);
        long count = (user != null) ? cartsService.getCartItemCount(user.getId()) : 0L;
        return ResponseEntity.ok(Map.of("count", count));
    }
}