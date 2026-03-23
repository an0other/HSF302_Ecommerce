package com.hsf.hsf302_ecom.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantFormDTO {

    @NotBlank(message = "Color is required")
    @Size(min = 2, max = 50, message = "Color must be 2–50 characters")
    private String color;

    @NotBlank(message = "Spec is required")
    @Size(min = 2, max = 50, message = "Spec must be 2–50 characters")
    private String spec;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private Boolean status = true;

    // Display-only
    private String productName;
}