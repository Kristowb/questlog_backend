package com.questlog.backend.service;

import com.questlog.backend.dto.CheckoutSessionResponse;
import com.questlog.backend.dto.PaymentIntentResponse;
import com.questlog.backend.dto.UserResponse;

public interface PremiumService {
    CheckoutSessionResponse createCheckoutSession(Long userId);
    UserResponse handleWebhookPaymentSuccess(Long userId);
    String getMockCheckoutPage(Long userId);
    String getMockSuccessPage(Long userId);
    String getSuccessPage(String sessionId, Long userId);
    String getCancelPage(Long userId);
    PaymentIntentResponse createPaymentIntent(Long userId);
    UserResponse confirmPayment(Long userId, String paymentIntentId);
}
