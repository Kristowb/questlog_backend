package com.questlog.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
    @NotBlank(message = "ID Token tidak boleh kosong")
    String idToken
) {
}
