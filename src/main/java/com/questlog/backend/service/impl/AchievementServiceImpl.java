package com.questlog.backend.service.impl;

import com.questlog.backend.dto.AchievementResponse;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.Achievement;
import com.questlog.backend.model.User;
import com.questlog.backend.model.UserAchievement;
import com.questlog.backend.repository.*;
import com.questlog.backend.service.AchievementService;
import com.questlog.backend.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final DietLogRepository dietLogRepository;
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostConstruct
    @Transactional
    public void seedAchievements() {
        if (achievementRepository.count() == 0) {
            log.info("Menginisialisasi data pencapaian awal (seeding)...");
            achievementRepository.save(Achievement.builder()
                    .title("Iron Warrior")
                    .description("Lakukan latihan beban (workout) minimal 5 kali.")
                    .icon("🏆")
                    .type("WORKOUT")
                    .requirement(5)
                    .build());
            achievementRepository.save(Achievement.builder()
                    .title("Carnivore King")
                    .description("Mencatat asupan protein tinggi (>= 120g) minimal 3 kali.")
                    .icon("🥩")
                    .type("DIET")
                    .requirement(3)
                    .build());
            achievementRepository.save(Achievement.builder()
                    .title("Quest Champion")
                    .description("Selesaikan total minimal 10 quest harian.")
                    .icon("👑")
                    .type("QUEST")
                    .requirement(10)
                    .build());
            achievementRepository.save(Achievement.builder()
                    .title("Hydration Devotee")
                    .description("Selesaikan quest minum air 3L (Pure Hydration) minimal 5 kali.")
                    .icon("💧")
                    .type("HYDRATION")
                    .requirement(5)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievementsForUser(Long userId) {
        log.info("Mengambil status pencapaian untuk user ID: {}", userId);
        
        // Memastikan user ada
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

        List<Achievement> achievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        
        Map<Long, UserAchievement> unlockedMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        List<AchievementResponse> responses = new ArrayList<>();
        for (Achievement ach : achievements) {
            UserAchievement ua = unlockedMap.get(ach.getId());
            boolean isUnlocked = ua != null;
            LocalDateTime unlockedAt = isUnlocked ? ua.getUnlockedAt() : null;
            
            responses.add(new AchievementResponse(
                    ach.getId(),
                    ach.getTitle(),
                    ach.getDescription(),
                    ach.getIcon(),
                    ach.getType(),
                    ach.getRequirement(),
                    isUnlocked,
                    unlockedAt
            ));
        }
        return responses;
    }

    @Override
    @Transactional
    public void checkAndUnlockAchievements(Long userId, String type) {
        log.info("Memeriksa kelayakan pencapaian bertipe {} untuk user ID: {}", type, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

        List<Achievement> achievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        
        List<Long> unlockedIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .toList();

        for (Achievement ach : achievements) {
            // Lewati jika sudah pernah di-unlock
            if (unlockedIds.contains(ach.getId())) {
                continue;
            }

            boolean shouldUnlock = false;
            switch (ach.getType()) {
                case "WORKOUT" -> {
                    long count = workoutLogRepository.countByUserId(userId);
                    if (count >= ach.getRequirement()) {
                        shouldUnlock = true;
                    }
                }
                case "DIET" -> {
                    long count = dietLogRepository.countByUserIdAndProteinGreaterThanEqual(userId, 120.0);
                    if (count >= ach.getRequirement()) {
                        shouldUnlock = true;
                    }
                }
                case "QUEST" -> {
                    long count = questRepository.countByUserIdAndIsCompleted(userId, true);
                    if (count >= ach.getRequirement()) {
                        shouldUnlock = true;
                    }
                }
                case "HYDRATION" -> {
                    long count = questRepository.countByUserIdAndTitleAndIsCompleted(userId, "Pure Hydration", true);
                    if (count >= ach.getRequirement()) {
                        shouldUnlock = true;
                    }
                }
            }

            if (shouldUnlock) {
                log.info("Pencapaian '{}' BERHASIL dibuka oleh user ID: {}. Memberikan reward!", ach.getTitle(), userId);
                
                // Simpan unlock status
                UserAchievement ua = UserAchievement.builder()
                        .userId(userId)
                        .achievementId(ach.getId())
                        .unlockedAt(LocalDateTime.now())
                        .build();
                userAchievementRepository.save(ua);

                // Berikan hadiah 100 koin langsung
                user.setCoins(user.getCoins() + 100);
                userRepository.save(user);

                // Berikan hadiah 50 XP
                String xpType = "WARRIOR".equalsIgnoreCase(user.getClassType()) ? "STRENGTH" : "VITALITY";
                userService.addXp(userId, 50, xpType);
            }
        }
    }
}
