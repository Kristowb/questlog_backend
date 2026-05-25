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
}
