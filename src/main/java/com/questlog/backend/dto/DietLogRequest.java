package com.questlog.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DietLogRequest(
    @NotNull(message = "User ID wajib diisi")
    Long userId,
    @NotBlank(message = "Nama makanan tidak boleh kosong")
    String foodName,
    @PositiveOrZero(message = "Protein tidak boleh negatif")
    double protein,
    @PositiveOrZero(message = "Karbohidrat tidak boleh negatif")
    double carbs,
    @PositiveOrZero(message = "Lemak tidak boleh negatif")
    double fat,
    @PositiveOrZero(message = "Kalori tidak boleh negatif")
    double calories
) {
}
