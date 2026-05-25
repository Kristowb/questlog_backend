package com.questlog.backend.service.impl;

import com.questlog.backend.dto.AchievementResponse;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.Achievement;
import com.questlog.backend.model.User;
import com.questlog.backend.model.UserAchievement;
import com.questlog.backend.repository.*;
import com.questlog.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private WorkoutLogRepository workoutLogRepository;

    @Mock
    private DietLogRepository dietLogRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    private User testUser;
    private Achievement workoutAchievement;
    private UserAchievement userAchievement;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("hero@quest.com")
                .name("Arthur")
                .classType("WARRIOR")
                .level(1)
                .coins(50)
                .build();

        workoutAchievement = Achievement.builder()
                .id(100L)
                .title("Iron Warrior")
                .description("Lakukan latihan beban minimal 5 kali.")
                .icon("🏆")
                .type("WORKOUT")
                .requirement(5)
                .build();

        userAchievement = UserAchievement.builder()
                .id(200L)
                .userId(1L)
                .achievementId(100L)
                .unlockedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAchievementsForUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achievementService.getAchievementsForUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAchievementsForUser_Success_ReturnsWithUnlockStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(achievementRepository.findAll()).thenReturn(List.of(workoutAchievement));
        when(userAchievementRepository.findByUserId(1L)).thenReturn(List.of(userAchievement));

        List<AchievementResponse> results = achievementService.getAchievementsForUser(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("Iron Warrior");
        assertThat(results.get(0).isUnlocked()).isTrue();
    }

    @Test
    void checkAndUnlockAchievements_Workout_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(achievementRepository.findAll()).thenReturn(List.of(workoutAchievement));
        // User has not unlocked it yet
        when(userAchievementRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        // User has logged 5 workouts (matches requirement)
        when(workoutLogRepository.countByUserId(1L)).thenReturn(5L);

        achievementService.checkAndUnlockAchievements(1L, "WORKOUT");

        // Verify that achievement is unlocked and user gets rewards (+100 coins)
        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getCoins()).isEqualTo(150); // 50 initial + 100 reward
        verify(userService, times(1)).addXp(1L, 50, "STRENGTH");
    }

    @Test
    void checkAndUnlockAchievements_Workout_NotEnoughCount_DoesNotUnlock() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(achievementRepository.findAll()).thenReturn(List.of(workoutAchievement));
        when(userAchievementRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        // User has logged only 3 workouts (requirement is 5)
        when(workoutLogRepository.countByUserId(1L)).thenReturn(3L);

        achievementService.checkAndUnlockAchievements(1L, "WORKOUT");

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
        verify(userRepository, never()).save(any(User.class));
        verify(userService, never()).addXp(anyLong(), anyInt(), anyString());
    }

    @Test
    void checkAndUnlockAchievements_AlreadyUnlocked_DoesNotUnlockAgain() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(achievementRepository.findAll()).thenReturn(List.of(workoutAchievement));
        // Already unlocked
        when(userAchievementRepository.findByUserId(1L)).thenReturn(List.of(userAchievement));

        achievementService.checkAndUnlockAchievements(1L, "WORKOUT");

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
