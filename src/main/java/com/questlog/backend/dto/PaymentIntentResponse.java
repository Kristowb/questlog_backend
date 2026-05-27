package com.questlog.backend.dto;

public record PaymentIntentResponse(
    String paymentIntentClientSecret,
    String publishableKey,
    String status
) {
}
