package com.questlog.backend.dto;

import java.time.LocalDateTime;

public record AchievementResponse(
    Long id,
    String title,
    String description,
    String icon,
    String type,
    int requirement,
    boolean isUnlocked,
    LocalDateTime unlockedAt
) {
}
