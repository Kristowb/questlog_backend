package com.questlog.backend.service.impl;

import com.questlog.backend.dto.WorkoutLogRequest;
import com.questlog.backend.dto.WorkoutLogResponse;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.WorkoutLog;
import com.questlog.backend.repository.WorkoutLogRepository;
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
class WorkoutServiceImplTest {

    @Mock
    private WorkoutLogRepository workoutLogRepository;

    @Mock
    private UserService userService;

    @Mock
    private com.questlog.backend.service.AchievementService achievementService;

    @InjectMocks
    private WorkoutServiceImpl workoutService;

    private WorkoutLogRequest testRequest;
    private WorkoutLog testLog;

    @BeforeEach
    void setUp() {
        testRequest = new WorkoutLogRequest(1L, "Bench Press", 4, 10, 60.0);
        testLog = WorkoutLog.builder()
                .id(200L)
                .userId(1L)
                .exerciseName("Bench Press")
                .sets(4)
                .reps(10)
                .weight(60.0)
                .logDate(LocalDate.now())
                .build();
    }

    @Test
    void addWorkoutLog_Success() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenReturn(testLog);

        WorkoutLogResponse response = workoutService.addWorkoutLog(testRequest);

        assertThat(response).isNotNull();
        assertThat(response.exerciseName()).isEqualTo("Bench Press");
        verify(userService, times(1)).getUserById(1L);
        verify(userService, times(1)).addXp(1L, 10, "STRENGTH");
        verify(workoutLogRepository, times(1)).save(any(WorkoutLog.class));
    }

    @Test
    void addWorkoutLog_UserNotFound_ThrowsException() {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User tidak ditemukan"));
        WorkoutLogRequest invalidRequest = new WorkoutLogRequest(99L, "Bench Press", 4, 10, 60.0);

        assertThatThrownBy(() -> workoutService.addWorkoutLog(invalidRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(workoutLogRepository, never()).save(any(WorkoutLog.class));
    }

    @Test
    void getDailyWorkouts_Success() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(workoutLogRepository.findByUserIdAndLogDate(eq(1L), any(LocalDate.class))).thenReturn(List.of(testLog));

        List<WorkoutLogResponse> result = workoutService.getDailyWorkouts(1L, LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).exerciseName()).isEqualTo("Bench Press");
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getWorkoutStats_Success() {
        Long userId = 1L;
        int days = 3;
        LocalDate today = LocalDate.now();
        
        WorkoutLog log1 = WorkoutLog.builder()
                .id(1L)
                .userId(userId)
                .exerciseName("Bench Press")
                .sets(3)
                .reps(10)
                .weight(50.0) // volume = 3 * 10 * 50 = 1500
                .logDate(today)
                .build();
                
        WorkoutLog log2 = WorkoutLog.builder()
                .id(2L)
                .userId(userId)
                .exerciseName("Squat")
                .sets(4)
                .reps(8)
                .weight(80.0) // volume = 4 * 8 * 80 = 2560
                .logDate(today.minusDays(1))
                .build();

        when(userService.getUserById(userId)).thenReturn(null);
        when(workoutLogRepository.findByUserIdAndLogDateGreaterThanEqualOrderByLogDateAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(log2, log1));

        List<com.questlog.backend.dto.WorkoutStatsResponse> stats = workoutService.getWorkoutStats(userId, days);

        assertThat(stats).hasSize(days);
        
        // 2 hari yang lalu (index 0) - tidak ada workout
        assertThat(stats.get(0).date()).isEqualTo(today.minusDays(2));
        assertThat(stats.get(0).totalWorkouts()).isZero();
        assertThat(stats.get(0).totalSets()).isZero();
        assertThat(stats.get(0).totalWeightVolume()).isZero();

        // kemarin (index 1) - log2
        assertThat(stats.get(1).date()).isEqualTo(today.minusDays(1));
        assertThat(stats.get(1).totalWorkouts()).isEqualTo(1);
        assertThat(stats.get(1).totalSets()).isEqualTo(4);
        assertThat(stats.get(1).totalWeightVolume()).isEqualTo(2560.0);

        // hari ini (index 2) - log1
        assertThat(stats.get(2).date()).isEqualTo(today);
        assertThat(stats.get(2).totalWorkouts()).isEqualTo(1);
        assertThat(stats.get(2).totalSets()).isEqualTo(3);
        assertThat(stats.get(2).totalWeightVolume()).isEqualTo(1500.0);

        verify(userService, times(1)).getUserById(userId);
        verify(workoutLogRepository, times(1)).findByUserIdAndLogDateGreaterThanEqualOrderByLogDateAsc(eq(userId), any(LocalDate.class));
    }
}
