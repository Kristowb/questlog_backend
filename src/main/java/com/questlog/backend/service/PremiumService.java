package com.questlog.backend.service;

import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.dto.UserResponse;

public interface PremiumService {
    CheckoutSessionResponse createCheckoutSession(Long userId);
    UserResponse handleWebhookPaymentSuccess(Long userId);
    String getMockCheckoutPage(Long userId);
    String getMockSuccessPage(Long userId);
}
