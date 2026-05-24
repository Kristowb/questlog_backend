package com.questlog.backend.dto;

import com.questlog.backend.model.User;

public record UserResponse(
    Long id,
    String email,
    String name,
    String classType,
    int level,
    int strengthXp,
    int vitalityXp,
    int xpToNextLevel,
    int coins,
    boolean isPremium
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) return null;
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getClassType(),
            user.getLevel(),
            user.getStrengthXp(),
            user.getVitalityXp(),
            user.getXpToNextLevel(),
            user.getCoins(),
            user.isPremium()
        );
    }
}
