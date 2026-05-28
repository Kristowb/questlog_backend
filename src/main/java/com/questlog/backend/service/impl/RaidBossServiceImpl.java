package com.questlog.backend.service.impl;

import com.questlog.backend.dto.DailyBossResponse;
import com.questlog.backend.dto.ClaimRewardResponse;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.exception.BadRequestException;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.DailyBoss;
import com.questlog.backend.model.User;
import com.questlog.backend.model.UserBossProgress;
import com.questlog.backend.repository.DailyBossRepository;
import com.questlog.backend.repository.UserBossProgressRepository;
import com.questlog.backend.repository.UserRepository;
import com.questlog.backend.service.RaidBossService;
import com.questlog.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RaidBossServiceImpl implements RaidBossService {

    private final DailyBossRepository dailyBossRepository;
    private final UserBossProgressRepository userBossProgressRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // Predefined templates for Daily Bosses (1 to 7)
    private static final String[] BOSS_NAMES = {
        "Iron Golem of the Depths",
        "Spectral Shadow Wyrm",
        "Undead Gladiator Champion",
        "Storm Phoenix Lord",
        "Corrupted Forest Ent",
        "Obsidian Colossus",
        "Lich King Acolyte"
    };

    private static final double[] BOSS_BASE_HP = {
        5000.0,
        6000.0,
        4500.0,
        5500.0,
        7000.0,
        8000.0,
        4000.0
    };

    private static final String[] BOSS_IMAGES = {
        "iron_golem.png",
        "spectral_wyrm.png",
        "undead_gladiator.png",
        "storm_phoenix.png",
        "corrupted_ent.png",
        "obsidian_colossus.png",
        "lich_king.png"
    };

    @Override
    @Transactional
    public DailyBossResponse getActiveBossForUser(Long userId, LocalDate date) {
        log.info("Mengambil status Raid Boss untuk user ID: {} pada tanggal: {}", userId, date);
        
        // 1. Dapatkan atau buat template DailyBoss untuk tanggal tersebut
        DailyBoss boss = dailyBossRepository.findByBossDate(date)
                .orElseGet(() -> {
                    int dayIndex = (date.getDayOfWeek().getValue() - 1) % BOSS_NAMES.length;
                    DailyBoss newBoss = DailyBoss.builder()
                            .name(BOSS_NAMES[dayIndex])
                            .baseMaxHp(BOSS_BASE_HP[dayIndex])
                            .imageUrl(BOSS_IMAGES[dayIndex])
                            .bossDate(date)
                            .build();
                    log.info("Menyemai Raid Boss baru '{}' untuk tanggal {}", newBoss.getName(), date);
                    return dailyBossRepository.save(newBoss);
                });

        // 2. Dapatkan User untuk melakukan scaling HP berdasarkan level
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

        // 3. Dapatkan atau buat progress pertarungan user hari ini
        UserBossProgress progress = userBossProgressRepository.findByUserIdAndBossId(userId, boss.getId())
                .orElseGet(() -> {
                    // HP berskala: baseHP * (1 + (level - 1) * 15%)
                    double maxHpScaled = boss.getBaseMaxHp() * (1.0 + (user.getLevel() - 1) * 0.15);
                    UserBossProgress newProgress = UserBossProgress.builder()
                            .userId(userId)
                            .bossId(boss.getId())
                            .maxHpScaled(maxHpScaled)
                            .currentHp(maxHpScaled)
                            .totalDamageDealt(0.0)
                            .isDefeated(false)
                            .isRewardClaimed(false)
                            .lastUpdated(LocalDateTime.now())
                            .build();
                    log.info("Membuat progress pertarungan baru untuk user ID {} melawan bos '{}' dengan HP berskala {}", 
                            userId, boss.getName(), maxHpScaled);
                    return userBossProgressRepository.save(newProgress);
                });

        return new DailyBossResponse(
                boss.getId(),
                boss.getName(),
                progress.getMaxHpScaled(),
                progress.getCurrentHp(),
                progress.getTotalDamageDealt(),
                progress.isDefeated(),
                progress.isRewardClaimed(),
                boss.getImageUrl()
        );
    }

    @Override
    @Transactional
    public DailyBossResponse registerDamage(Long userId, double damage) {
        log.info("Mendaftarkan damage sebesar {} untuk user ID: {}", damage, userId);
        LocalDate today = LocalDate.now();
        
        // Memastikan boss & progress hari ini sudah ada
        DailyBossResponse activeBoss = getActiveBossForUser(userId, today);
        
        UserBossProgress progress = userBossProgressRepository.findByUserIdAndBossId(userId, activeBoss.bossId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress bos tidak ditemukan untuk hari ini"));

        if (progress.isDefeated()) {
            log.info("Raid Boss sudah dikalahkan hari ini oleh user ID {}. Serangan diabaikan.", userId);
            return activeBoss;
        }

        // Terapkan damage
        double newDamageDealt = progress.getTotalDamageDealt() + damage;
        double newCurrentHp = Math.max(0.0, progress.getCurrentHp() - damage);
        boolean newlyDefeated = newCurrentHp <= 0.0;

        progress.setTotalDamageDealt(newDamageDealt);
        progress.setCurrentHp(newCurrentHp);
        progress.setLastUpdated(LocalDateTime.now());

        if (newlyDefeated) {
            log.info("Selamat! User ID {} telah mengalahkan Raid Boss '{}'!", userId, activeBoss.name());
            progress.setDefeated(true);
        }

        userBossProgressRepository.save(progress);

        return new DailyBossResponse(
                activeBoss.bossId(),
                activeBoss.name(),
                progress.getMaxHpScaled(),
                progress.getCurrentHp(),
                progress.getTotalDamageDealt(),
                progress.isDefeated(),
                progress.isRewardClaimed(),
                activeBoss.imageUrl()
        );
    }

    @Override
    @Transactional
    public ClaimRewardResponse claimDefeatReward(Long userId) {
        log.info("User ID {} mencoba mengklaim reward kekalahan Raid Boss", userId);
        LocalDate today = LocalDate.now();

        // Ambil boss aktif hari ini
        DailyBossResponse activeBoss = getActiveBossForUser(userId, today);
        
        UserBossProgress progress = userBossProgressRepository.findByUserIdAndBossId(userId, activeBoss.bossId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress bos tidak ditemukan untuk hari ini"));

        if (!progress.isDefeated()) {
            log.warn("User ID {} mencoba mengklaim reward tapi bos belum dikalahkan. HP tersisa: {}", userId, progress.getCurrentHp());
            throw new BadRequestException("Raid Boss belum dikalahkan!");
        }

        if (progress.isRewardClaimed()) {
            log.warn("User ID {} mencoba mengklaim reward kembali", userId);
            throw new BadRequestException("Hadiah kemenangan bos harian sudah pernah diklaim hari ini!");
        }

        // Berikan hadiah
        log.info("Memberikan hadiah kemenangan ke user ID {}: +50 XP & +50 koin", userId);
        userService.addXp(userId, 50, "STRENGTH");
        UserResponse updatedUser = userService.addCoins(userId, 50);

        // Update status klaim
        progress.setRewardClaimed(true);
        progress.setLastUpdated(LocalDateTime.now());
        userBossProgressRepository.save(progress);

        return new ClaimRewardResponse(
                true,
                50,
                50,
                updatedUser.level(),
                updatedUser.coins()
        );
    }
}
