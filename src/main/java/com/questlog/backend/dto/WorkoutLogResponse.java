package com.questlog.backend.dto;
import com.questlog.backend.model.WorkoutLog;
import java.time.LocalDate;

public record WorkoutLogResponse(
    Long id,
    Long userId,
    String exerciseName,
    int sets,
    int reps,
    double weight,
    LocalDate logDate
) {
    public static WorkoutLogResponse fromEntity(WorkoutLog log) {
        if (log == null) return null;
        return new WorkoutLogResponse(
            log.getId(),
            log.getUserId(),
            log.getExerciseName(),
            log.getSets(),
            log.getReps(),
            log.getWeight(),
            log.getLogDate()
        );
    }
}
