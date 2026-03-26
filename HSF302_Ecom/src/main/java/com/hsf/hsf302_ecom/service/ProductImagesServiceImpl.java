package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.entity.ProductImages;
import com.hsf.hsf302_ecom.entity.Products;
import com.hsf.hsf302_ecom.repository.ProductImagesRepo;
import com.hsf.hsf302_ecom.repository.ProductsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImagesServiceImpl implements ProductImagesService {

    private static final int    MAX_IMAGES      = 5;
    private static final long   MAX_FILE_BYTES  = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final ProductImagesRepo imagesRepo;
    private final ProductsRepo      productsRepo;

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    // ── helpers ────────────────────────────────────────────────────────────────

    /** Absolute path resolved relative to the running JVM's working directory. */
    private Path uploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) return ".jpg";
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot).toLowerCase() : ".jpg";
    }

    private void ensureDir() throws IOException {
        Files.createDirectories(uploadRoot());
    }

    // ── public API ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<ProductImages> uploadImages(Long productId, List<MultipartFile> files) {
        if (files == null || files.isEmpty())
            throw new IllegalArgumentException("No files provided.");

        Products product = productsRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        long existing = imagesRepo.countByProductId(productId);
        long slots    = MAX_IMAGES - existing;

        if (slots <= 0)
            throw new IllegalStateException(
                    "Maximum " + MAX_IMAGES + " images allowed per product. "
                            + "Delete some images first.");

        // validate all files before touching the filesystem
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            if (f.getSize() > MAX_FILE_BYTES)
                throw new IllegalArgumentException(
                        "File \"" + f.getOriginalFilename() + "\" exceeds the 5 MB limit.");
            if (!ALLOWED_TYPES.contains(f.getContentType()))
                throw new IllegalArgumentException(
                        "File \"" + f.getOriginalFilename()
                                + "\" has an unsupported type. Allowed: JPEG, PNG, WEBP, GIF.");
        }

        boolean needsPrimary =
                imagesRepo.findFirstByProductIdAndIsPrimaryTrue(productId).isEmpty();

        List<ProductImages> saved = new ArrayList<>();
        long written = 0;

        try {
            ensureDir();
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory.", e);
        }

        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            if (written >= slots) break; // cap at available slots

            String filename = UUID.randomUUID() + extension(f.getOriginalFilename());
            Path   target   = uploadRoot().resolve(filename);

            try {
                Files.copy(f.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Failed to save upload {}", filename, e);
                throw new IllegalStateException("File upload failed: " + f.getOriginalFilename());
            }

            boolean isPrimary = needsPrimary && written == 0;
            String  webPath   = "/uploads/products/" + filename;

            ProductImages img = ProductImages.builder()
                    .product(product)
                    .imageUrl(webPath)
                    .isPrimary(isPrimary)
                    .build();
            saved.add(imagesRepo.save(img));
            written++;
        }

        return saved;
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId, Long productId) {
        ProductImages img = imagesRepo.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));

        if (!img.getProduct().getId().equals(productId))
            throw new IllegalArgumentException("Image does not belong to this product.");

        boolean wasPrimary = Boolean.TRUE.equals(img.getIsPrimary());

        // delete physical file (best-effort — don't fail the whole request)
        try {
            String url  = img.getImageUrl();                     // e.g. /uploads/products/abc.jpg
            String file = url.replaceFirst("^/uploads/products/", "");
            Path   path = uploadRoot().resolve(file).normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete physical file for image {}: {}", imageId, e.getMessage());
        }

        imagesRepo.delete(img);

        // if the deleted image was primary, promote the next available image
        if (wasPrimary) {
            List<ProductImages> remaining =
                    imagesRepo.findByProductIdOrderByIsPrimaryDescIdAsc(productId);
            if (!remaining.isEmpty()) {
                ProductImages next = remaining.get(0);
                next.setIsPrimary(true);
                imagesRepo.save(next);
            }
        }
    }

    @Override
    @Transactional
    public void setPrimary(Long imageId, Long productId) {
        ProductImages img = imagesRepo.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));

        if (!img.getProduct().getId().equals(productId))
            throw new IllegalArgumentException("Image does not belong to this product.");

        imagesRepo.clearPrimaryForProduct(productId);
        img.setIsPrimary(true);
        imagesRepo.save(img);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImages> getImages(Long productId) {
        return imagesRepo.findByProductIdOrderByIsPrimaryDescIdAsc(productId);
    }
}