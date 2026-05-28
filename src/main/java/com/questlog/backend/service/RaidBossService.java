package com.questlog.backend.service;

import com.questlog.backend.dto.DailyBossResponse;
import com.questlog.backend.dto.ClaimRewardResponse;
import java.time.LocalDate;

public interface RaidBossService {
    DailyBossResponse getActiveBossForUser(Long userId, LocalDate date);
    DailyBossResponse registerDamage(Long userId, double damage);
    ClaimRewardResponse claimDefeatReward(Long userId);
}
