package com.questlog.backend.service;

import com.questlog.backend.dto.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse getUserById(Long id);
    UserResponse chooseClass(Long id, String classType);
    UserResponse addXp(Long userId, int xpAmount, String xpType);
    List<UserResponse> getLeaderboard();
    UserResponse setPremium(Long userId, boolean isPremium);
    UserResponse addCoins(Long userId, int coinAmount);
}
