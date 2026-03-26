package com.hsf.hsf302_ecom.controller;

import com.hsf.hsf302_ecom.dto.admin.InventoryFormDTO;
import com.hsf.hsf302_ecom.dto.admin.ProductFormDTO;
import com.hsf.hsf302_ecom.dto.admin.VariantFormDTO;
import com.hsf.hsf302_ecom.entity.ProductImages;
import com.hsf.hsf302_ecom.entity.Products;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserRole;
import com.hsf.hsf302_ecom.service.AdminProductService;
import com.hsf.hsf302_ecom.service.AdminService;
import com.hsf.hsf302_ecom.service.ProductImagesService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private static final String SK_USER = "loggedInUser";

    private final AdminProductService productService;
    private final AdminService        adminService;
    private final ProductImagesService imageService;

    private Users requireAdmin(HttpSession session) {
        Users user = (Users) session.getAttribute(SK_USER);
        if (user == null || user.getRole() != UserRole.ADMIN) return null;
        return user;
    }

    @GetMapping
    public String productList(HttpSession session, Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String keyword,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Long brandId) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("productPage",  productService.getProducts(keyword, categoryId, brandId, PageRequest.of(page, 12)));
        model.addAttribute("categories",   adminService.getAllCategories());
        model.addAttribute("brands",       adminService.getAllBrands());
        model.addAttribute("keyword",      keyword);
        model.addAttribute("categoryId",   categoryId);
        model.addAttribute("brandId",      brandId);
        model.addAttribute("currentPage",  page);
        model.addAttribute("section",      "products");
        model.addAttribute("stage",        "products");
        return "admin/products";
    }

    @GetMapping("/new")
    public String productNewForm(HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        model.addAttribute("productForm", new ProductFormDTO());
        model.addAttribute("categories",  adminService.getAllCategories());
        model.addAttribute("brands",      adminService.getAllBrands());
        model.addAttribute("isEdit",  false);
        model.addAttribute("section", "products");
        model.addAttribute("stage",   "products");
        return "admin/product-form";
    }

    @PostMapping("/new")
    public String productCreate(@Valid @ModelAttribute("productForm") ProductFormDTO form,
                                BindingResult br, HttpSession session,
                                Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        if (br.hasErrors()) {
            model.addAttribute("categories", adminService.getAllCategories());
            model.addAttribute("brands",     adminService.getAllBrands());
            model.addAttribute("isEdit",  false);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "products");
            return "admin/product-form";
        }
        String err = productService.createProduct(form);
        if (err != null) {
            model.addAttribute("formError", err);
            model.addAttribute("categories", adminService.getAllCategories());
            model.addAttribute("brands",     adminService.getAllBrands());
            model.addAttribute("isEdit",  false);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "products");
            return "admin/product-form";
        }
        ra.addFlashAttribute("toast",     "Product \"" + form.getName() + "\" created. Add variants and stock to activate it.");
        ra.addFlashAttribute("toastType", "info");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String productEditForm(@PathVariable Long id, HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("productForm",     productService.getProductForm(id));
        model.addAttribute("productId",       id);
        model.addAttribute("productName",     p.getName());
        model.addAttribute("hasActiveStock",  productService.productHasActiveStock(id));
        model.addAttribute("categories",      adminService.getAllCategories());
        model.addAttribute("brands",          adminService.getAllBrands());
        model.addAttribute("images",          imageService.getImages(id));
        model.addAttribute("imageCount",      imageService.getImages(id).size());
        model.addAttribute("maxImages",       5);
        model.addAttribute("isEdit",  true);
        model.addAttribute("section", "products");
        model.addAttribute("stage",   "products");
        return "admin/product-form";
    }

    @PostMapping("/{id}/edit")
    public String productUpdate(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductFormDTO form,
                                BindingResult br, HttpSession session,
                                Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (br.hasErrors()) {
            model.addAttribute("productId",      id);
            model.addAttribute("productName",    p.getName());
            model.addAttribute("hasActiveStock", productService.productHasActiveStock(id));
            model.addAttribute("categories",     adminService.getAllCategories());
            model.addAttribute("brands",         adminService.getAllBrands());
            model.addAttribute("images",         imageService.getImages(id));
            model.addAttribute("imageCount",     imageService.getImages(id).size());
            model.addAttribute("maxImages",      5);
            model.addAttribute("isEdit",  true);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "products");
            return "admin/product-form";
        }
        String err = productService.updateProduct(id, form);
        if (err != null) {
            model.addAttribute("formError",      err);
            model.addAttribute("productId",      id);
            model.addAttribute("productName",    p.getName());
            model.addAttribute("hasActiveStock", productService.productHasActiveStock(id));
            model.addAttribute("categories",     adminService.getAllCategories());
            model.addAttribute("brands",         adminService.getAllBrands());
            model.addAttribute("images",         imageService.getImages(id));
            model.addAttribute("imageCount",     imageService.getImages(id).size());
            model.addAttribute("maxImages",      5);
            model.addAttribute("isEdit",  true);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "products");
            return "admin/product-form";
        }
        ra.addFlashAttribute("toast",     "Product updated successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String productDelete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        String err = productService.softDeleteProduct(id);
        if (err != null) {
            ra.addFlashAttribute("toast",     err);
            ra.addFlashAttribute("toastType", "error");
        } else {
            ra.addFlashAttribute("toast",     "Product and all its variants deactivated.");
            ra.addFlashAttribute("toastType", "success");
        }
        return "redirect:/admin/products";
    }

    // ── Image CRUD (AJAX JSON) ────────────────────────────────────────────────

    /**
     * POST /admin/products/{id}/images
     * Upload one or more images (multipart). Returns JSON list of saved images.
     */
    @PostMapping("/{id}/images")
    @ResponseBody
    public ResponseEntity<?> uploadImages(@PathVariable Long id,
                                          @RequestParam("files") List<MultipartFile> files,
                                          HttpSession session) {
        if (requireAdmin(session) == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            List<ProductImages> saved = imageService.uploadImages(id, files);
            List<Map<String, Object>> result = saved.stream()
                    .map(img -> Map.<String, Object>of(
                            "id",        img.getId(),
                            "imageUrl",  img.getImageUrl(),
                            "isPrimary", Boolean.TRUE.equals(img.getIsPrimary())))
                    .toList();
            return ResponseEntity.ok(Map.of("images", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /admin/products/{id}/images/{imageId}
     * Remove one image. Returns updated image list.
     */
    @DeleteMapping("/{id}/images/{imageId}")
    @ResponseBody
    public ResponseEntity<?> deleteImage(@PathVariable Long id,
                                         @PathVariable Long imageId,
                                         HttpSession session) {
        if (requireAdmin(session) == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            imageService.deleteImage(imageId, id);
            List<Map<String, Object>> remaining = imageService.getImages(id).stream()
                    .map(img -> Map.<String, Object>of(
                            "id",        img.getId(),
                            "imageUrl",  img.getImageUrl(),
                            "isPrimary", Boolean.TRUE.equals(img.getIsPrimary())))
                    .toList();
            return ResponseEntity.ok(Map.of("images", remaining));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /admin/products/{id}/images/list
     * Return current image list as JSON (used by the frontend after mutations).
     */
    @GetMapping("/{id}/images/list")
    @ResponseBody
    public ResponseEntity<?> listImages(@PathVariable Long id, HttpSession session) {
        if (requireAdmin(session) == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        List<Map<String, Object>> result = imageService.getImages(id).stream()
                .map(img -> Map.<String, Object>of(
                        "id",        img.getId(),
                        "imageUrl",  img.getImageUrl(),
                        "isPrimary", Boolean.TRUE.equals(img.getIsPrimary())))
                .toList();
        return ResponseEntity.ok(Map.of("images", result));
    }

    /**
     * PATCH /admin/products/{id}/images/{imageId}/primary
     * Set an image as the primary image for this product.
     */
    @PatchMapping("/{id}/images/{imageId}/primary")
    @ResponseBody
    public ResponseEntity<?> setPrimary(@PathVariable Long id,
                                        @PathVariable Long imageId,
                                        HttpSession session) {
        if (requireAdmin(session) == null)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            imageService.setPrimary(imageId, id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{productId}/variants")
    public String variantList(@PathVariable Long productId,
                              HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product",  p);
        model.addAttribute("variants", productService.getVariantsForProduct(productId));
        model.addAttribute("section",  "products");
        model.addAttribute("stage",    "variants");
        return "admin/variants";
    }

    @GetMapping("/{productId}/variants/new")
    public String variantNewForm(@PathVariable Long productId,
                                 HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        VariantFormDTO form = new VariantFormDTO();
        form.setProductName(p.getName());
        model.addAttribute("variantForm", form);
        model.addAttribute("product",     p);
        model.addAttribute("isEdit",  false);
        model.addAttribute("section", "products");
        model.addAttribute("stage",   "variants");
        return "admin/variant-form";
    }

    @PostMapping("/{productId}/variants/new")
    public String variantCreate(@PathVariable Long productId,
                                @Valid @ModelAttribute("variantForm") VariantFormDTO form,
                                BindingResult br, HttpSession session,
                                Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        form.setProductName(p.getName());
        if (br.hasErrors()) {
            model.addAttribute("product", p);
            model.addAttribute("isEdit",  false);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "variants");
            return "admin/variant-form";
        }
        String err = productService.createVariant(productId, form);
        if (err != null) {
            model.addAttribute("formError", err);
            model.addAttribute("product",   p);
            model.addAttribute("isEdit",    false);
            model.addAttribute("section",   "products");
            model.addAttribute("stage",     "variants");
            return "admin/variant-form";
        }
        ra.addFlashAttribute("toast",     "Variant created. Go to Inventory to add stock and activate it.");
        ra.addFlashAttribute("toastType", "info");
        return "redirect:/admin/products/" + productId + "/variants";
    }

    @GetMapping("/{productId}/variants/{variantId}/edit")
    public String variantEditForm(@PathVariable Long productId,
                                  @PathVariable Long variantId,
                                  HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        long avail = productService.getAvailableStockForVariant(variantId);
        boolean productActive = Boolean.TRUE.equals(p.getStatus());

        String activateBlockReason = null;
        if (avail <= 0)
            activateBlockReason = "Cannot activate — no available stock (stock − reserved must be > 0). Add inventory first.";
        else if (!productActive)
            activateBlockReason = "Cannot activate — the parent product is inactive. Add stock via Inventory (auto-activates both), or activate the product first.";

        model.addAttribute("variantForm",          productService.getVariantForm(variantId));
        model.addAttribute("variantId",            variantId);
        model.addAttribute("product",              p);
        model.addAttribute("hasActiveStock",       productService.variantHasActiveStock(variantId));
        model.addAttribute("activateBlockReason",  activateBlockReason);
        model.addAttribute("isEdit",  true);
        model.addAttribute("section", "products");
        model.addAttribute("stage",   "variants");
        return "admin/variant-form";
    }

    @PostMapping("/{productId}/variants/{variantId}/edit")
    public String variantUpdate(@PathVariable Long productId,
                                @PathVariable Long variantId,
                                @Valid @ModelAttribute("variantForm") VariantFormDTO form,
                                BindingResult br, HttpSession session,
                                Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        form.setProductName(p.getName());
        if (br.hasErrors()) {
            long avail = productService.getAvailableStockForVariant(variantId);
            boolean productActive = Boolean.TRUE.equals(p.getStatus());
            String activateBlockReason = null;
            if (avail <= 0)
                activateBlockReason = "Cannot activate — no available stock (stock − reserved must be > 0). Add inventory first.";
            else if (!productActive)
                activateBlockReason = "Cannot activate — the parent product is inactive.";
            model.addAttribute("variantId",           variantId);
            model.addAttribute("product",             p);
            model.addAttribute("hasActiveStock",      productService.variantHasActiveStock(variantId));
            model.addAttribute("activateBlockReason", activateBlockReason);
            model.addAttribute("isEdit",  true);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "variants");
            return "admin/variant-form";
        }
        String err = productService.updateVariant(variantId, form);
        if (err != null) {
            long avail = productService.getAvailableStockForVariant(variantId);
            boolean productActive = Boolean.TRUE.equals(p.getStatus());
            String activateBlockReason = null;
            if (avail <= 0)
                activateBlockReason = "Cannot activate — no available stock (stock − reserved must be > 0). Add inventory first.";
            else if (!productActive)
                activateBlockReason = "Cannot activate — the parent product is inactive.";
            model.addAttribute("formError",           err);
            model.addAttribute("variantId",           variantId);
            model.addAttribute("product",             p);
            model.addAttribute("hasActiveStock",      productService.variantHasActiveStock(variantId));
            model.addAttribute("activateBlockReason", activateBlockReason);
            model.addAttribute("isEdit",  true);
            model.addAttribute("section", "products");
            model.addAttribute("stage",   "variants");
            return "admin/variant-form";
        }
        ra.addFlashAttribute("toast",     "Variant updated successfully.");
        ra.addFlashAttribute("toastType", "success");
        return "redirect:/admin/products/" + productId + "/variants";
    }

    @PostMapping("/{productId}/variants/{variantId}/delete")
    public String variantDelete(@PathVariable Long productId,
                                @PathVariable Long variantId,
                                HttpSession session, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        String err = productService.softDeleteVariant(variantId);
        if (err != null) {
            ra.addFlashAttribute("toast",     err);
            ra.addFlashAttribute("toastType", "error");
        } else {
            ra.addFlashAttribute("toast",     "Variant deactivated.");
            ra.addFlashAttribute("toastType", "success");
        }
        return "redirect:/admin/products/" + productId + "/variants";
    }

    @GetMapping("/{productId}/variants/{variantId}/inventory")
    public String inventoryForm(@PathVariable Long productId,
                                @PathVariable Long variantId,
                                @RequestParam(defaultValue = "") String returnUrl,
                                HttpSession session, Model model) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        boolean variantActive = Boolean.TRUE.equals(
                productService.getVariantForm(variantId).getStatus());
        boolean productActive = Boolean.TRUE.equals(p.getStatus());

        String statusNote = null;
        if (!variantActive && !productActive)
            statusNote = "Both the variant and the product are inactive. Adding stock will automatically activate them.";
        else if (!variantActive)
            statusNote = "This variant is inactive. Adding stock will automatically activate it.";
        else if (!productActive)
            statusNote = "The parent product is inactive. Adding stock will automatically activate the product too.";

        String resolvedReturn = returnUrl.isBlank()
                ? "/admin/products/" + productId + "/variants"
                : returnUrl;

        model.addAttribute("statusNote",    statusNote);
        model.addAttribute("inventoryForm", productService.getInventoryFormForVariant(variantId));
        model.addAttribute("variantId",     variantId);
        model.addAttribute("product",       p);
        model.addAttribute("returnUrl",     resolvedReturn);
        model.addAttribute("section",       "products");
        model.addAttribute("stage",         "inventory");
        return "admin/product-inventory-form";
    }

    @PostMapping("/{productId}/variants/{variantId}/inventory")
    public String inventorySave(@PathVariable Long productId,
                                @PathVariable Long variantId,
                                @RequestParam(defaultValue = "") String returnUrl,
                                @Valid @ModelAttribute("inventoryForm") InventoryFormDTO form,
                                BindingResult br, HttpSession session,
                                Model model, RedirectAttributes ra) {
        if (requireAdmin(session) == null) return "redirect:/login";
        Products p = productService.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String resolvedReturn = returnUrl.isBlank()
                ? "/admin/products/" + productId + "/variants"
                : returnUrl;

        if (br.hasErrors()) {
            model.addAttribute("variantId", variantId);
            model.addAttribute("product",   p);
            model.addAttribute("returnUrl", resolvedReturn);
            model.addAttribute("section",   "products");
            model.addAttribute("stage",     "inventory");
            return "admin/product-inventory-form";
        }
        try {
            productService.createOrUpdateInventory(variantId, form);
            ra.addFlashAttribute("toast",     "Inventory updated successfully.");
            ra.addFlashAttribute("toastType", "success");
        } catch (Exception e) {
            model.addAttribute("formError", e.getMessage());
            model.addAttribute("variantId", variantId);
            model.addAttribute("product",   p);
            model.addAttribute("returnUrl", resolvedReturn);
            model.addAttribute("section",   "products");
            model.addAttribute("stage",     "inventory");
            return "admin/product-inventory-form";
        }
        return "redirect:" + resolvedReturn;
    }
}