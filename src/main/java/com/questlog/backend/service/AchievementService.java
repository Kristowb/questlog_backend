package com.questlog.backend.service;

import com.questlog.backend.dto.AchievementResponse;
import java.util.List;

public interface AchievementService {
    List<AchievementResponse> getAchievementsForUser(Long userId);
    void checkAndUnlockAchievements(Long userId, String type);
}
