package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.entity.Orders;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping
    public String myOrders(HttpSession session, Model model) {

        Users user = (Users) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        List<Orders> orders = ordersService.getOrdersByUser(user.getId());

        model.addAttribute("orders", orders);

        return "orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id,
                              HttpSession session,
                              Model model) {

        Users user = (Users) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Orders order = ordersService.getOrderDetail(id, user.getId());

        model.addAttribute("order", order);

        return "order-detail";
    }
}
