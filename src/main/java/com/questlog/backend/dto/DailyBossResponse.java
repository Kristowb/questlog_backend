package com.questlog.backend.dto;

public record DailyBossResponse(
    Long bossId,
    String name,
    double maxHp,
    double currentHp,
    double damageDealtToday,
    boolean isDefeated,
    boolean isRewardClaimed,
    String imageUrl
) {}
