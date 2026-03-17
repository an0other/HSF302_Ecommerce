package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.CheckoutRequestDTO;
import com.hsf.hsf302_ecom.dto.CheckoutSummaryDTO;

public interface CheckoutService {

    CheckoutSummaryDTO getCheckoutSummary(Long userId);

    Long placeOrder(Long userId, CheckoutRequestDTO request);
}