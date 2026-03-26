package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.RegisterDTO;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserRole;
import com.hsf.hsf302_ecom.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class  AuthController {

    private static final String SK_USER          = "loggedInUser";
    private static final String SK_PENDING_ID    = "_pendingUserId";
    private static final String SK_PENDING_EMAIL = "_pendingEmail";
    private static final String SK_REG_CODE      = "_regCode";
    private static final String SK_RESET_CODE    = "_resetCode";
    private static final String SK_RESET_UID     = "_resetUserId";
    private static final String SK_RESET_OK      = "_resetVerified";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at < 2) return email;
        return email.substring(0, 2) + "****" + email.substring(at);
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute(SK_USER) != null) return "redirect:/";
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String usernameOrEmail,
                              @RequestParam String password,
                              HttpSession session,
                              Model model,
                              RedirectAttributes ra) {
        Optional<Users> result = authService.login(usernameOrEmail, password);
        if (result.isEmpty()) {
            model.addAttribute("loginError", "Invalid credentials or account not activated.");
            return "auth/login";
        }

        Users user = result.get();
        session.setAttribute(SK_USER, user);
        session.setMaxInactiveInterval(3600);
        ra.addFlashAttribute("toast", "Welcome back, " + user.getUsername() + "! \uD83D\uDC4B");
        ra.addFlashAttribute("toastType", "success");

        if (user.getRole() == UserRole.ADMIN) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("toast", "You've been signed out. See you soon!");
        ra.addFlashAttribute("toastType", "info");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (session.getAttribute(SK_USER) != null) return "redirect:/";
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                                 BindingResult br,
                                 HttpSession session,
                                 Model model) {
        if (br.hasErrors()) return "auth/register";

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
            return "auth/register";
        }
        if (authService.existsByUsername(dto.getUsername())) {
            br.rejectValue("username", "taken", "Username is already taken.");
            return "auth/register";
        }
        if (authService.existsByEmail(dto.getEmail())) {
            br.rejectValue("email", "taken", "Email is already registered.");
            return "auth/register";
        }

        AuthService.RegisterResult result = authService.registerPending(dto);
        session.setAttribute(SK_PENDING_ID,    result.user().getId());
        session.setAttribute(SK_PENDING_EMAIL, result.user().getEmail());
        session.setAttribute(SK_REG_CODE,      result.code());

        return "redirect:/register/verify";
    }

    @GetMapping("/register/verify")
    public String verifyPage(HttpSession session, Model model) {
        if (session.getAttribute(SK_PENDING_ID) == null) return "redirect:/register";
        String email = (String) session.getAttribute(SK_PENDING_EMAIL);
        model.addAttribute("email", email != null ? maskEmail(email) : "");
        return "auth/verify-email";
    }

    @PostMapping("/register/verify")
    public String verifySubmit(@RequestParam String code,
                               HttpSession session,
                               Model model,
                               RedirectAttributes ra) {
        Long   userId = (Long)   session.getAttribute(SK_PENDING_ID);
        String stored = (String) session.getAttribute(SK_REG_CODE);
        if (userId == null) return "redirect:/register";

        boolean ok = authService.activateAccount(userId, code, stored);
        if (!ok) {
            model.addAttribute("verifyError", "Incorrect code. Please try again.");
            String email = (String) session.getAttribute(SK_PENDING_EMAIL);
            model.addAttribute("email", email != null ? maskEmail(email) : "");
            return "auth/verify-email";
        }

        session.removeAttribute(SK_PENDING_ID);
        session.removeAttribute(SK_PENDING_EMAIL);
        session.removeAttribute(SK_REG_CODE);
        ra.addFlashAttribute("successMessage", "Account activated! Welcome to Voltex \uD83C\uDF89");
        return "redirect:/login";
    }

    @PostMapping("/register/resend")
    public String resendActivation(HttpSession session, Model model) {
        String email = (String) session.getAttribute(SK_PENDING_EMAIL);
        if (email == null) return "redirect:/register";

        String newCode = authService.resendActivationCode(email);
        session.setAttribute(SK_REG_CODE, newCode);
        model.addAttribute("infoMessage", "A new code has been sent to " + maskEmail(email) + ".");
        model.addAttribute("email", maskEmail(email));
        return "auth/verify-email";
    }

    @GetMapping("/forgot-password")
    public String forgotPage(HttpSession session) {
        if (session.getAttribute(SK_USER) != null) return "redirect:/";
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@RequestParam String identifier,
                               HttpSession session,
                               Model model) {
        Optional<AuthService.PasswordResetResult> result = authService.initiatePasswordReset(identifier);
        if (result.isEmpty()) {
            model.addAttribute("forgotError", "No active account found with that email or username.");
            return "auth/forgot-password";
        }

        session.setAttribute(SK_RESET_UID,  result.get().user().getId());
        session.setAttribute(SK_RESET_CODE, result.get().code());

        return "redirect:/forgot-password/verify";
    }

    @GetMapping("/forgot-password/verify")
    public String resetVerifyPage(HttpSession session) {
        if (session.getAttribute(SK_RESET_UID) == null) return "redirect:/forgot-password";
        return "auth/reset-verify";
    }

    @PostMapping("/forgot-password/verify")
    public String resetVerifySubmit(@RequestParam String code,
                                    HttpSession session,
                                    Model model) {
        String stored = (String) session.getAttribute(SK_RESET_CODE);
        Long   userId = (Long)   session.getAttribute(SK_RESET_UID);
        if (userId == null) return "redirect:/forgot-password";

        if (stored == null || !stored.equals(code)) {
            model.addAttribute("resetVerifyError", "Incorrect code. Please try again.");
            return "auth/reset-verify";
        }

        session.setAttribute(SK_RESET_OK, true);
        session.removeAttribute(SK_RESET_CODE);
        return "redirect:/forgot-password/new-password";
    }

    @GetMapping("/forgot-password/new-password")
    public String newPasswordPage(HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute(SK_RESET_OK);
        if (verified == null || !verified) return "redirect:/forgot-password";
        return "auth/new-password";
    }

    @PostMapping("/forgot-password/new-password")
    public String newPasswordSubmit(@RequestParam String newPassword,
                                    @RequestParam String confirmNewPassword,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes ra) {
        Boolean verified = (Boolean) session.getAttribute(SK_RESET_OK);
        Long    userId   = (Long)    session.getAttribute(SK_RESET_UID);
        if (verified == null || !verified || userId == null) return "redirect:/forgot-password";

        if (newPassword.length() < 6) {
            model.addAttribute("newPassError", "Password must be at least 6 characters.");
            return "auth/new-password";
        }
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("newPassError", "Passwords do not match.");
            return "auth/new-password";
        }

        authService.resetPassword(userId, newPassword);
        session.removeAttribute(SK_RESET_UID);
        session.removeAttribute(SK_RESET_OK);
        ra.addFlashAttribute("successMessage", "Password reset successful! Sign in with your new password.");
        return "redirect:/login";
    }
}