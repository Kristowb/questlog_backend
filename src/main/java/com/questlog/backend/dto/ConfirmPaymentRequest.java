package com.questlog.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
    @NotNull(message = "User ID wajib diisi")
    Long userId,
    
    @NotBlank(message = "Payment Intent ID wajib diisi")
    String paymentIntentId
) {
}
