package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private static final String SK_USER = "loggedInUser";

    private final OrdersService ordersService;

    @GetMapping()
    public String showMyOrders(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute(SK_USER);

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("orders", ordersService.getMyOrders(loggedInUser.getId()));
        return "order/orders";
    }

    @GetMapping("/{orderId}")
    public String showOrderDetail(@PathVariable Long orderId,
                                  HttpSession session,
                                  Model model) {
        Users loggedInUser = (Users) session.getAttribute(SK_USER);

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("order", ordersService.getMyOrderDetail(loggedInUser.getId(), orderId));
        return "order/order-detail";
    }
}
