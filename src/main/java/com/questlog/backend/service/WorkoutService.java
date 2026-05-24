package com.questlog.backend.service;

import com.questlog.backend.dto.WorkoutLogRequest;
import com.questlog.backend.dto.WorkoutLogResponse;
import java.time.LocalDate;
import java.util.List;

public interface WorkoutService {
    WorkoutLogResponse addWorkoutLog(WorkoutLogRequest request);
    List<WorkoutLogResponse> getDailyWorkouts(Long userId, LocalDate date);
}
