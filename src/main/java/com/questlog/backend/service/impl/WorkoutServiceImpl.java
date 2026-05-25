package com.questlog.backend.service.impl;

import com.questlog.backend.dto.WorkoutLogRequest;
import com.questlog.backend.dto.WorkoutLogResponse;
import com.questlog.backend.model.WorkoutLog;
import com.questlog.backend.repository.WorkoutLogRepository;
import com.questlog.backend.service.UserService;
import com.questlog.backend.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutLogRepository workoutLogRepository;
    private final UserService userService;
    private final com.questlog.backend.service.AchievementService achievementService;

    @Override
    @Transactional
    public WorkoutLogResponse addWorkoutLog(WorkoutLogRequest request) {
        log.info("Menambahkan log latihan fisik baru untuk user ID: {}", request.userId());
        
        // Validasi keberadaan user
        userService.getUserById(request.userId());

        WorkoutLog logObj = WorkoutLog.builder()
                .userId(request.userId())
                .exerciseName(request.exerciseName())
                .sets(request.sets())
                .reps(request.reps())
                .weight(request.weight())
                .logDate(LocalDate.now())
                .build();
        
        WorkoutLog savedLog = workoutLogRepository.save(logObj);
        log.info("Memberikan hadiah +10 STRENGTH XP ke user ID: {}", logObj.getUserId());
        userService.addXp(logObj.getUserId(), 10, "STRENGTH");
        
        // Periksa kelayakan pencapaian
        achievementService.checkAndUnlockAchievements(logObj.getUserId(), "WORKOUT");
        
        return WorkoutLogResponse.fromEntity(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutLogResponse> getDailyWorkouts(Long userId, LocalDate date) {
        log.info("Mengambil riwayat log latihan fisik harian untuk user ID: {} pada tanggal: {}", userId, date);
        
        // Validasi keberadaan user
        userService.getUserById(userId);
        
        return workoutLogRepository.findByUserIdAndLogDate(userId, date).stream()
                .map(WorkoutLogResponse::fromEntity)
                .toList();
    }
}
