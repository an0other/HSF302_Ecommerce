package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.admin.BrandFormDTO;
import com.hsf.hsf302_ecom.dto.admin.CategoryFormDTO;
import com.hsf.hsf302_ecom.dto.admin.InventoryFormDTO;
import com.hsf.hsf302_ecom.entity.Brands;
import com.hsf.hsf302_ecom.entity.Categories;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserRole;
import com.hsf.hsf302_ecom.service.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String SK_USER = "loggedInUser";

    private final AdminService adminService;

    private Users requireAdmin(HttpSession session) {
        Users user = (Users) session.getAttribute(SK_USER);
        if (user == null || user.getRole() != UserRole.ADMIN) return null;
        return user;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("stats",   adminService.getDashboardStats());
        model.addAttribute("section", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/brands")
    public String brandList(HttpSession session, Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "") String keyword) {
        if (requireAdmin(session) == null) return "redirect:/login";

        Page<Brands> brandPage = adminService.getBrands(keyword, PageRequest.of(page, 10));

        model.addAttribute("brandPage", brandPage);
        model.addAttribute("keyword",   keyword);
        model.addAttribute("currentPage",    page);
        model.addAttribute("section",        "brands");
        return "admin/brands";
    }

    @GetMapping("/brands/new")
    public String brandNewForm(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("brandForm", new BrandFormDTO());
        model.addAttribute("isEdit",    false);
        model.addAttribute("section",   "brands");
        return "admin/brand-form";
    }

    @PostMapping("/brands/new")
    public String brandCreate(@Valid @ModelAttribute("brandForm") BrandFormDTO form,
                              BindingResult br,
                              HttpSession session, Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        if (br.hasErrors()) {
            model.addAttribute("isEdit",  false);
            model.addAttribute("section", "brands");
            return "admin/brand-form";
        }
        String err = adminService.createBrand(form);
        if (err != null) {
            model.addAttribute("formError", err);
            model.addAttribute("isEdit",    false);
            model.addAttribute("section",   "brands");
            return "admin/brand-form";
        }
        ra.addFlashAttribute("toast",     "Brand \"" + form.getName() + "\" created successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/admin/brands";
    }

    @GetMapping("/brands/{id}/edit")
    public String brandEditForm(@PathVariable Long id, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("brandForm",    adminService.getBrandForm(id));
        model.addAttribute("brandId",      id);
        model.addAttribute("isEdit",       true);
        model.addAttribute("hasInventory", adminService.brandHasInventory(id));
        model.addAttribute("section",      "brands");
        return "admin/brand-form";
    }

    @PostMapping("/brands/{id}/edit")
    public String brandUpdate(@PathVariable Long id,
                              @Valid @ModelAttribute("brandForm") BrandFormDTO form,
                              BindingResult br,
                              HttpSession session, Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        if (br.hasErrors()) {
            model.addAttribute("brandId",      id);
            model.addAttribute("isEdit",       true);
            model.addAttribute("hasInventory", adminService.brandHasInventory(id));
            model.addAttribute("section",      "brands");
            return "admin/brand-form";
        }
        String err = adminService.updateBrand(id, form);
        if (err != null) {
            model.addAttribute("formError",    err);
            model.addAttribute("brandId",      id);
            model.addAttribute("isEdit",       true);
            model.addAttribute("hasInventory", adminService.brandHasInventory(id));
            model.addAttribute("section",      "brands");
            return "admin/brand-form";
        }
        ra.addFlashAttribute("toast",     "Brand updated successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/admin/brands";
    }

    @GetMapping("/categories")
    public String categoryList(HttpSession session, Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "") String keyword) {
        if (requireAdmin(session) == null) return "redirect:/login";

        Page<Categories> categoryPage = adminService.getCategories(keyword, PageRequest.of(page, 10));

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("currentPage",    page);
        model.addAttribute("section",        "categories");
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String categoryNewForm(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("categoryForm", new CategoryFormDTO());
        model.addAttribute("isEdit",       false);
        model.addAttribute("section",      "categories");
        return "admin/category-form";
    }

    @PostMapping("/categories/new")
    public String categoryCreate(@Valid @ModelAttribute("categoryForm") CategoryFormDTO form,
                                 BindingResult br,
                                 HttpSession session, Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        if (br.hasErrors()) {
            model.addAttribute("isEdit",  false);
            model.addAttribute("section", "categories");
            return "admin/category-form";
        }
        String err = adminService.createCategory(form);
        if (err != null) {
            model.addAttribute("formError", err);
            model.addAttribute("isEdit",    false);
            model.addAttribute("section",   "categories");
            return "admin/category-form";
        }
        ra.addFlashAttribute("toast",     "Category \"" + form.getName() + "\" created successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String categoryEditForm(@PathVariable Long id, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("categoryForm", adminService.getCategoryForm(id));
        model.addAttribute("categoryId",   id);
        model.addAttribute("isEdit",       true);
        model.addAttribute("hasInventory", adminService.categoryHasInventory(id));
        model.addAttribute("section",      "categories");
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}/edit")
    public String categoryUpdate(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryFormDTO form,
                                 BindingResult br,
                                 HttpSession session, Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        if (br.hasErrors()) {
            model.addAttribute("categoryId",   id);
            model.addAttribute("isEdit",       true);
            model.addAttribute("hasInventory", adminService.categoryHasInventory(id));
            model.addAttribute("section",      "categories");
            return "admin/category-form";
        }
        String err = adminService.updateCategory(id, form);
        if (err != null) {
            model.addAttribute("formError",    err);
            model.addAttribute("categoryId",   id);
            model.addAttribute("isEdit",       true);
            model.addAttribute("hasInventory", adminService.categoryHasInventory(id));
            model.addAttribute("section",      "categories");
            return "admin/category-form";
        }
        ra.addFlashAttribute("toast",     "Category updated successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/admin/categories";
    }

    @GetMapping("/inventory")
    public String inventoryList(HttpSession session, Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) Long brandId) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("inventoryPage", adminService.getInventories(keyword, categoryId, brandId, PageRequest.of(page, 15)));
        model.addAttribute("categories",    adminService.getAllCategories());
        model.addAttribute("brands",        adminService.getAllBrands());
        model.addAttribute("keyword",       keyword);
        model.addAttribute("categoryId",    categoryId);
        model.addAttribute("brandId",       brandId);
        model.addAttribute("currentPage",   page);
        model.addAttribute("section",       "inventory");
        return "admin/inventory";
    }

    @GetMapping("/inventory/{variantId}/edit")
    public String inventoryEditForm(@PathVariable Long variantId, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("inventoryForm", adminService.getInventoryForm(variantId));
        model.addAttribute("variantId",     variantId);
        model.addAttribute("section",       "inventory");
        return "admin/inventory-form";
    }

    @PostMapping("/inventory/{variantId}/edit")
    public String inventoryUpdate(@PathVariable Long variantId,
                                  @Valid @ModelAttribute("inventoryForm") InventoryFormDTO form,
                                  BindingResult br,
                                  HttpSession session, Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        if (br.hasErrors()) {
            model.addAttribute("variantId", variantId);
            model.addAttribute("section",   "inventory");
            return "admin/inventory-form";
        }
        try {
            adminService.updateInventory(variantId, form);
            ra.addFlashAttribute("toast",     "Inventory updated successfully.");
            ra.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            model.addAttribute("formError", e.getMessage());
            model.addAttribute("variantId", variantId);
            model.addAttribute("section",   "inventory");
            return "admin/inventory-form";
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/inventory/{variantId}/restock")
    public String inventoryRestock(@PathVariable Long variantId,
                                   @RequestParam Long amount,
                                   HttpSession session, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        try {
            adminService.restockInventory(variantId, amount);
            ra.addFlashAttribute("toast",     "Added " + amount + " units to stock.");
            ra.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("toast",     e.getMessage());
            ra.addFlashAttribute("toastType", "error");
        }
        return "redirect:/admin/inventory";
    }
}