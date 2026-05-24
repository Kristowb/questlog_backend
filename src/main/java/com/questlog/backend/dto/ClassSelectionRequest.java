package com.questlog.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ClassSelectionRequest(
    @NotBlank(message = "Class type tidak boleh kosong")
    String classType
) {
}
