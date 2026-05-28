package com.questlog.backend.dto;

public record ClaimRewardResponse(
    boolean success,
    int coinsAwarded,
    int xpAwarded,
    int newUserLevel,
    int newUserCoins
) {}
