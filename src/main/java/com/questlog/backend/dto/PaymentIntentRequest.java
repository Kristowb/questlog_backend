package com.questlog.backend.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentIntentRequest(
    @NotNull(message = "User ID wajib diisi")
    Long userId
) {
}
