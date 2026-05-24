package com.questlog.backend.dto;

import com.questlog.backend.model.DietLog;
import java.time.LocalDate;

public record DietLogResponse(
    Long id,
    Long userId,
    String foodName,
    double protein,
    double carbs,
    double fat,
    double calories,
    LocalDate logDate
) {
    public static DietLogResponse fromEntity(DietLog log) {
        if (log == null) return null;
        return new DietLogResponse(
            log.getId(),
            log.getUserId(),
            log.getFoodName(),
            log.getProtein(),
            log.getCarbs(),
            log.getFat(),
            log.getCalories(),
            log.getLogDate()
        );
    }
}
