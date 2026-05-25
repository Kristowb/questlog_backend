package com.questlog.backend.service.impl;

import com.questlog.backend.exception.BadRequestException;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.Quest;
import com.questlog.backend.model.User;
import com.questlog.backend.repository.QuestRepository;
import com.questlog.backend.repository.UserRepository;
import com.questlog.backend.service.QuestService;
import com.questlog.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final com.questlog.backend.service.AchievementService achievementService;

    @Override
    @Transactional
    public List<com.questlog.backend.dto.QuestResponse> getDailyQuests(Long userId) {
        log.info("Mengambil quest harian untuk user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

        LocalDate today = LocalDate.now();
        List<Quest> quests = questRepository.findByUserIdAndQuestDate(userId, today);

        if (quests.isEmpty()) {
            log.info("Quest hari ini belum di-generate untuk user ID: {}. Membuat quest harian baru.", userId);
            quests = generateDailyQuests(user, today);
        }

        return quests.stream()
                .map(com.questlog.backend.dto.QuestResponse::fromEntity)
                .toList();
    }

    private List<Quest> generateDailyQuests(User user, LocalDate date) {
        List<Quest> quests = new ArrayList<>();
        String classType = user.getClassType() != null ? user.getClassType() : "WARRIOR";

        if ("WARRIOR".equalsIgnoreCase(classType)) {
            quests.add(Quest.builder()
                    .userId(user.getId())
                    .title("Heavy Lifting Day")
                    .description("Lakukan latihan Bench Press / Deadlift minimal 4 set dengan intensitas tinggi.")
                    .type("STRENGTH")
                    .xpReward(45)
                    .questDate(date)
                    .isCompleted(false)
                    .build());
            quests.add(Quest.builder()
                    .userId(user.getId())
                    .title("Iron Mastery")
                    .description("Selesaikan latihan beban total volume > 2000 kg.")
                    .type("STRENGTH")
                    .xpReward(40)
                    .questDate(date)
                    .isCompleted(false)
                    .build());
        } else { // ARCHER
            quests.add(Quest.builder()
                    .userId(user.getId())
                    .title("Wind Runner")
                    .description("Lari sejauh 5 KM atau sepeda statis selama 30 menit.")
                    .type("STRENGTH")
                    .xpReward(45)
                    .questDate(date)
                    .isCompleted(false)
                    .build());
            quests.add(Quest.builder()
                    .userId(user.getId())
                    .title("Cardio Endurance")
                    .description("Jaga detak jantung tinggi (HIIT) selama 15 menit.")
                    .type("STRENGTH")
                    .xpReward(35)
                    .questDate(date)
                    .isCompleted(false)
                    .build());
        }

        quests.add(Quest.builder()
                .userId(user.getId())
                .title("Carnivore Feast")
                .description("Konsumsi makanan padat protein (daging sapi/ayam/ikan) minimal 120g.")
                .type("VITALITY")
                .xpReward(40)
                .questDate(date)
                .isCompleted(false)
                .build());

        quests.add(Quest.builder()
                .userId(user.getId())
                .title("Pure Hydration")
                .description("Minum air putih minimal 3 Liter sepanjang hari.")
                .type("VITALITY")
                .xpReward(20)
                .questDate(date)
                .isCompleted(false)
                .build());

        log.info("Berhasil membuat 4 quest harian untuk pahlawan bertipe {}", classType);
        return questRepository.saveAll(quests);
    }

    @Override
    @Transactional
    public com.questlog.backend.dto.QuestResponse completeQuest(Long questId) {
        log.info("Menyelesaikan quest ID: {}", questId);
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new ResourceNotFoundException("Quest dengan ID " + questId + " tidak ditemukan"));

        if (quest.isCompleted()) {
            log.warn("Quest ID {} sudah pernah diselesaikan sebelumnya", questId);
            throw new BadRequestException("Quest ini sudah diselesaikan sebelumnya");
        }

        quest.setCompleted(true);
        questRepository.save(quest);

        log.info("Quest ID {} berhasil diselesaikan. Menghadiahi +{} {} XP.", questId, quest.getXpReward(), quest.getType());
        userService.addXp(quest.getUserId(), quest.getXpReward(), quest.getType());

        // Periksa kelayakan pencapaian
        achievementService.checkAndUnlockAchievements(quest.getUserId(), "QUEST");
        achievementService.checkAndUnlockAchievements(quest.getUserId(), "HYDRATION");

        return com.questlog.backend.dto.QuestResponse.fromEntity(quest);
    }
}
