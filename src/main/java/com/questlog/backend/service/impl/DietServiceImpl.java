package com.questlog.backend.service.impl;

import com.questlog.backend.dto.DietLogRequest;
import com.questlog.backend.dto.DietLogResponse;
import com.questlog.backend.model.DietLog;
import com.questlog.backend.repository.DietLogRepository;
import com.questlog.backend.service.DietService;
import com.questlog.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietServiceImpl implements DietService {

    private final DietLogRepository dietLogRepository;
    private final UserService userService;
    private final com.questlog.backend.service.AchievementService achievementService;

    @Override
    @Transactional
    public DietLogResponse addDietLog(DietLogRequest request) {
        log.info("Menambahkan log diet baru untuk user ID: {}", request.userId());
        
        // Validasi keberadaan user
        userService.getUserById(request.userId());
        
        DietLog logObj = DietLog.builder()
                .userId(request.userId())
                .foodName(request.foodName())
                .protein(request.protein())
                .carbs(request.carbs())
                .fat(request.fat())
                .calories(request.calories())
                .logDate(LocalDate.now())
                .build();

        DietLog savedLog = dietLogRepository.save(logObj);

        int xpReward = 10;
        if (logObj.getProtein() >= 30.0) {
            log.info("Bonus protein terdeteksi (>= 30g). Memberikan tambahan +15 VITALITY XP.");
            xpReward += 15;
        }
        
        userService.addXp(logObj.getUserId(), xpReward, "VITALITY");
        
        // Periksa kelayakan pencapaian
        achievementService.checkAndUnlockAchievements(logObj.getUserId(), "DIET");
        
        return DietLogResponse.fromEntity(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DietLogResponse> getDailyDiet(Long userId, LocalDate date) {
        log.info("Mengambil riwayat diet harian untuk user ID: {} pada tanggal: {}", userId, date);
        
        // Validasi keberadaan user
        userService.getUserById(userId);
        
        return dietLogRepository.findByUserIdAndLogDate(userId, date).stream()
                .map(DietLogResponse::fromEntity)
                .toList();
    }
}
