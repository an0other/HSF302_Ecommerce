package com.hsf.hsf302_ecom.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryFormDTO {

    // Display-only fields (not bound from form, set by service)
    private String productName;
    private String variantLabel;
    private String color;
    private String spec;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Long stock;

    @NotNull(message = "Reserved is required")
    @Min(value = 0, message = "Reserved cannot be negative")
    private Long reserved;
}