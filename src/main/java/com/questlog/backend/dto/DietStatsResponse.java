package com.questlog.backend.dto;

import java.time.LocalDate;

public record DietStatsResponse(
    LocalDate date,
    double totalCalories,
    double totalProtein,
    double totalCarbs,
    double totalFat
) {}
