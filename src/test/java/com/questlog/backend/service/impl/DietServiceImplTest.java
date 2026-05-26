package com.questlog.backend.service.impl;

import com.questlog.backend.dto.DietLogRequest;
import com.questlog.backend.dto.DietLogResponse;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.DietLog;
import com.questlog.backend.repository.DietLogRepository;
import com.questlog.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietServiceImplTest {

    @Mock
    private DietLogRepository dietLogRepository;

    @Mock
    private UserService userService;

    @Mock
    private com.questlog.backend.service.AchievementService achievementService;

    @InjectMocks
    private DietServiceImpl dietService;

    private DietLogRequest normalRequest;
    private DietLogRequest highProteinRequest;
    private DietLog testLog;

    @BeforeEach
    void setUp() {
        normalRequest = new DietLogRequest(1L, "Apple", 0.3, 25.0, 0.0, 95.0);
        highProteinRequest = new DietLogRequest(1L, "Chicken Breast", 31.0, 0.0, 3.6, 165.0);
        testLog = DietLog.builder()
                .id(100L)
                .userId(1L)
                .foodName("Apple")
                .protein(0.3)
                .carbs(25.0)
                .fat(0.0)
                .calories(95.0)
                .logDate(LocalDate.now())
                .build();
    }

    @Test
    void addDietLog_Success_Normal() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(dietLogRepository.save(any(DietLog.class))).thenReturn(testLog);

        DietLogResponse response = dietService.addDietLog(normalRequest);

        assertThat(response).isNotNull();
        assertThat(response.foodName()).isEqualTo("Apple");
        verify(userService, times(1)).getUserById(1L);
        verify(userService, times(1)).addXp(1L, 10, "VITALITY");
        verify(dietLogRepository, times(1)).save(any(DietLog.class));
    }

    @Test
    void addDietLog_Success_HighProteinBonus() {
        DietLog highProteinLog = DietLog.builder()
                .id(101L)
                .userId(1L)
                .foodName("Chicken Breast")
                .protein(31.0)
                .logDate(LocalDate.now())
                .build();

        when(userService.getUserById(1L)).thenReturn(null);
        when(dietLogRepository.save(any(DietLog.class))).thenReturn(highProteinLog);

        DietLogResponse response = dietService.addDietLog(highProteinRequest);

        assertThat(response).isNotNull();
        assertThat(response.protein()).isEqualTo(31.0);
        verify(userService, times(1)).getUserById(1L);
        verify(userService, times(1)).addXp(1L, 25, "VITALITY");
        verify(dietLogRepository, times(1)).save(any(DietLog.class));
    }

    @Test
    void addDietLog_UserNotFound_ThrowsException() {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User tidak ditemukan"));
        DietLogRequest invalidRequest = new DietLogRequest(99L, "Apple", 0.3, 25.0, 0.0, 95.0);

        assertThatThrownBy(() -> dietService.addDietLog(invalidRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(dietLogRepository, never()).save(any(DietLog.class));
    }

    @Test
    void getDailyDiet_Success() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(dietLogRepository.findByUserIdAndLogDate(eq(1L), any(LocalDate.class))).thenReturn(List.of(testLog));

        List<DietLogResponse> result = dietService.getDailyDiet(1L, LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).foodName()).isEqualTo("Apple");
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getDietStats_Success() {
        Long userId = 1L;
        int days = 3;
        LocalDate today = LocalDate.now();
        
        DietLog log1 = DietLog.builder()
                .id(1L)
                .userId(userId)
                .foodName("Apple")
                .calories(95.0)
                .protein(0.3)
                .carbs(25.0)
                .fat(0.0)
                .logDate(today)
                .build();
                
        DietLog log2 = DietLog.builder()
                .id(2L)
                .userId(userId)
                .foodName("Chicken Breast")
                .calories(165.0)
                .protein(31.0)
                .carbs(0.0)
                .fat(3.6)
                .logDate(today.minusDays(1))
                .build();

        when(userService.getUserById(userId)).thenReturn(null);
        when(dietLogRepository.findByUserIdAndLogDateGreaterThanEqualOrderByLogDateAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(log2, log1));

        List<com.questlog.backend.dto.DietStatsResponse> stats = dietService.getDietStats(userId, days);

        assertThat(stats).hasSize(days);
        
        // 2 hari yang lalu (index 0) - tidak ada diet log
        assertThat(stats.get(0).date()).isEqualTo(today.minusDays(2));
        assertThat(stats.get(0).totalCalories()).isZero();
        assertThat(stats.get(0).totalProtein()).isZero();
        assertThat(stats.get(0).totalCarbs()).isZero();
        assertThat(stats.get(0).totalFat()).isZero();

        // kemarin (index 1) - log2
        assertThat(stats.get(1).date()).isEqualTo(today.minusDays(1));
        assertThat(stats.get(1).totalCalories()).isEqualTo(165.0);
        assertThat(stats.get(1).totalProtein()).isEqualTo(31.0);
        assertThat(stats.get(1).totalCarbs()).isEqualTo(0.0);
        assertThat(stats.get(1).totalFat()).isEqualTo(3.6);

        // hari ini (index 2) - log1
        assertThat(stats.get(2).date()).isEqualTo(today);
        assertThat(stats.get(2).totalCalories()).isEqualTo(95.0);
        assertThat(stats.get(2).totalProtein()).isEqualTo(0.3);
        assertThat(stats.get(2).totalCarbs()).isEqualTo(25.0);
        assertThat(stats.get(2).totalFat()).isEqualTo(0.0);

        verify(userService, times(1)).getUserById(userId);
        verify(dietLogRepository, times(1)).findByUserIdAndLogDateGreaterThanEqualOrderByLogDateAsc(eq(userId), any(LocalDate.class));
    }
}
