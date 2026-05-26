package com.questlog.backend.service;

import com.questlog.backend.dto.WorkoutLogRequest;
import com.questlog.backend.dto.WorkoutLogResponse;
import com.questlog.backend.dto.WorkoutStatsResponse;
import java.time.LocalDate;
import java.util.List;

public interface WorkoutService {
    WorkoutLogResponse addWorkoutLog(WorkoutLogRequest request);
    List<WorkoutLogResponse> getDailyWorkouts(Long userId, LocalDate date);
    List<WorkoutStatsResponse> getWorkoutStats(Long userId, int days);
}
