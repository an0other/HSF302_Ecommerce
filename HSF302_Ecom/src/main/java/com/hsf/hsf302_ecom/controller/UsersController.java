package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.ChangePassRequest;
import com.hsf.hsf302_ecom.dto.UserProfileDTO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UsersController {

    private static final String SK_USER = "loggedInUser";

    private final UsersService usersService;

    @GetMapping
    public String showProfile(HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute(SK_USER);

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        UserProfileDTO profile = usersService.getProfileByUserId(loggedInUser.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("changePassRequest", new ChangePassRequest());
        model.addAttribute("showPasswordForm", false);

        return "user-profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ("changePassRequest") ChangePassRequest changePassRequest,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes ra) {

        Users loggedInUser = (Users) session.getAttribute(SK_USER);

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        UserProfileDTO profile = usersService.getProfileByUserId(loggedInUser.getId());
        model.addAttribute("profile", profile);

        if (bindingResult.hasErrors()) {
            model.addAttribute("changePassRequest", changePassRequest);
            model.addAttribute("showPasswordForm", true);
            return "user-profile";
        }

        try {
            usersService.changePassword(loggedInUser.getId(), changePassRequest);
            ra.addFlashAttribute("toast", "Changed password successfully.");
            ra.addFlashAttribute("toastType", "success");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            model.addAttribute("changePassRequest", changePassRequest);
            model.addAttribute("showPasswordForm", true);
            model.addAttribute("toast", e.getMessage());
            model.addAttribute("toastType", "error");
            return "user-profile";
        }
    }
}
