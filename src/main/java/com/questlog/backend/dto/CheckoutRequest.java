package com.questlog.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
    @NotNull(message = "User ID wajib diisi")
    Long userId
) {
}
