package com.hsf.hsf302_ecom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Serves uploaded product images from the local filesystem at /uploads/products/**
 *
 * Place this in your config package. With this in place, images saved to
 * the directory configured via app.upload.dir (default: uploads/products
 * relative to the working directory) become accessible at
 *   http://localhost:8080/uploads/products/<filename>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Absolute path so it works regardless of the working directory
        String absolutePath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        // Ensure it ends with /
        if (!absolutePath.endsWith("/")) absolutePath += "/";

        registry
                .addResourceHandler("/uploads/products/**")
                .addResourceLocations(absolutePath);
    }
}