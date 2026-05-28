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

import com.questlog.backend.dto.WorkoutStatsResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutLogRepository workoutLogRepository;
    private final UserService userService;
    private final com.questlog.backend.service.AchievementService achievementService;
    private final com.questlog.backend.service.RaidBossService raidBossService;

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
        
        // Daftarkan damage serangan ke Raid Boss harian
        double damage = request.sets() * request.reps() * request.weight();
        try {
            raidBossService.registerDamage(request.userId(), damage);
        } catch (Exception e) {
            log.error("Gagal mendaftarkan damage latihan ke Raid Boss: {}", e.getMessage());
        }
        
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

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutStatsResponse> getWorkoutStats(Long userId, int days) {
        log.info("Mengambil statistik latihan fisik untuk user ID: {} selama {} hari terakhir", userId, days);
        
        // Validasi keberadaan user
        userService.getUserById(userId);

        LocalDate startDate = LocalDate.now().minusDays(days - 1L);
        List<WorkoutLog> logs = workoutLogRepository.findByUserIdAndLogDateGreaterThanEqualOrderByLogDateAsc(userId, startDate);

        Map<LocalDate, List<WorkoutLog>> logsByDate = logs.stream()
                .collect(Collectors.groupingBy(WorkoutLog::getLogDate));

        List<WorkoutStatsResponse> stats = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            List<WorkoutLog> dailyLogs = logsByDate.getOrDefault(date, Collections.emptyList());

            int totalWorkouts = dailyLogs.size();
            int totalSets = dailyLogs.stream().mapToInt(WorkoutLog::getSets).sum();
            double totalWeightVolume = dailyLogs.stream()
                    .mapToDouble(l -> l.getSets() * l.getReps() * l.getWeight())
                    .sum();

            stats.add(new WorkoutStatsResponse(date, totalWorkouts, totalSets, totalWeightVolume));
        }

        return stats;
    }
}
