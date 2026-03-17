package com.hsf.hsf302_ecom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequestDTO {

    @NotBlank(message = "Shipping address is required")
    @Size(min = 5, max = 255, message = "Address must be 5–255 characters")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be 2–100 characters")
    private String shippingCity;

    @NotBlank(message = "District is required")
    @Size(min = 2, max = 100, message = "District must be 2–100 characters")
    private String shippingDistrict;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String notes;
}