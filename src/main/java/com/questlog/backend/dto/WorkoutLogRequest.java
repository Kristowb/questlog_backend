package com.questlog.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record WorkoutLogRequest(
    @NotNull(message = "User ID wajib diisi")
    Long userId,
    @NotBlank(message = "Nama exercise tidak boleh kosong")
    String exerciseName,
    @Positive(message = "Sets harus lebih dari 0")
    int sets,
    @Positive(message = "Reps harus lebih dari 0")
    int reps,
    @PositiveOrZero(message = "Weight tidak boleh negatif")
    double weight
) {
}
