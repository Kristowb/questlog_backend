package com.questlog.backend.dto;

import java.time.LocalDate;

public record WorkoutStatsResponse(
    LocalDate date,
    int totalWorkouts,
    int totalSets,
    double totalWeightVolume
) {}
