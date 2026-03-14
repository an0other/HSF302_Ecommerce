package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.ChangePasswordRequest;
import com.hsf.hsf302_ecom.dto.RegisterRequest;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.service.UsersService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @PostMapping("/login")
    public String doLogin(Model model,
                          @RequestParam String email,
                          @RequestParam String password,
                          HttpSession session)
    {
        Users user = usersService.authenticate(email,password);
        if(user==null){
            model.addAttribute("error","Sai tên email hoặc password");
            return "login";
        }
        session.setAttribute("authenticated", true);
        session.setAttribute("userEmail", email);
        session.setAttribute("loggedInUser", user);
        return "redirect:/products";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model)
    {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            usersService.register(request);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @ModelAttribute ChangePasswordRequest request,
            HttpSession session,
            Model model) {

        Users user = (Users) session.getAttribute("loggedInUser");

        try {
            usersService.changePassword(user.getId(), request);
            model.addAttribute("success", "Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "change-password";
    }
}

