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
import com.questlog.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaidBossServiceImplTest {

    @Mock
    private DailyBossRepository dailyBossRepository;

    @Mock
    private UserBossProgressRepository userBossProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RaidBossServiceImpl raidBossService;

    private User testUser;
    private DailyBoss testBoss;
    private UserBossProgress testProgress;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("hero@questlog.com")
                .name("Kaelen")
                .level(3) // level 3 -> HP scaling = 1 + (3-1)*0.15 = 1.3
                .coins(100)
                .build();

        testBoss = DailyBoss.builder()
                .id(10L)
                .name("Golem Test")
                .baseMaxHp(1000.0)
                .imageUrl("golem.png")
                .bossDate(LocalDate.now())
                .build();

        testProgress = UserBossProgress.builder()
                .id(100L)
                .userId(1L)
                .bossId(10L)
                .maxHpScaled(1300.0)
                .currentHp(1300.0)
                .totalDamageDealt(0.0)
                .isDefeated(false)
                .isRewardClaimed(false)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void getActiveBossForUser_ExistingProgress_Success() {
        when(dailyBossRepository.findByBossDate(any(LocalDate.class))).thenReturn(Optional.of(testBoss));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBossProgressRepository.findByUserIdAndBossId(1L, 10L)).thenReturn(Optional.of(testProgress));

        DailyBossResponse response = raidBossService.getActiveBossForUser(1L, LocalDate.now());

        assertThat(response).isNotNull();
        assertThat(response.bossId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Golem Test");
        assertThat(response.maxHp()).isEqualTo(1300.0);
        assertThat(response.currentHp()).isEqualTo(1300.0);
        assertThat(response.isDefeated()).isFalse();
        verify(userBossProgressRepository, never()).save(any(UserBossProgress.class));
    }

    @Test
    void getActiveBossForUser_NewProgress_Success() {
        when(dailyBossRepository.findByBossDate(any(LocalDate.class))).thenReturn(Optional.of(testBoss));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBossProgressRepository.findByUserIdAndBossId(1L, 10L)).thenReturn(Optional.empty());
        when(userBossProgressRepository.save(any(UserBossProgress.class))).thenAnswer(i -> i.getArgument(0));

        DailyBossResponse response = raidBossService.getActiveBossForUser(1L, LocalDate.now());

        assertThat(response).isNotNull();
        assertThat(response.maxHp()).isEqualTo(1300.0);
        assertThat(response.currentHp()).isEqualTo(1300.0);
        verify(userBossProgressRepository, times(1)).save(any(UserBossProgress.class));
    }

    @Test
    void registerDamage_HitBoss_NotDefeated() {
        when(dailyBossRepository.findByBossDate(any(LocalDate.class))).thenReturn(Optional.of(testBoss));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBossProgressRepository.findByUserIdAndBossId(1L, 10L)).thenReturn(Optional.of(testProgress));
        when(userBossProgressRepository.save(any(UserBossProgress.class))).thenReturn(testProgress);

        DailyBossResponse response = raidBossService.registerDamage(1L, 500.0);

        assertThat(response.currentHp()).isEqualTo(800.0);
        assertThat(response.damageDealtToday()).isEqualTo(500.0);
        assertThat(response.isDefeated()).isFalse();
        verify(userBossProgressRepository, times(1)).save(argThat(progress -> 
            progress.getCurrentHp() == 800.0 && 
            progress.getTotalDamageDealt() == 500.0 && 
            !progress.isDefeated()
        ));
    }

    @Test
    void registerDamage_HitBoss_Defeated() {
        when(dailyBossRepository.findByBossDate(any(LocalDate.class))).thenReturn(Optional.of(testBoss));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBossProgressRepository.findByUserIdAndBossId(1L, 10L)).thenReturn(Optional.of(testProgress));
        when(userBossProgressRepository.save(any(UserBossProgress.class))).thenReturn(testProgress);

        DailyBossResponse response = raidBossService.registerDamage(1L, 1500.0);

        assertThat(response.currentHp()).isZero();
        assertThat(response.isDefeated()).isTrue();
        verify(userBossProgressRepository, times(1)).save(argThat(progress -> 
            progress.getCurrentHp() == 0.0 && 
            progress.isDefeated()
        ));
    }

    @Test
    void claimDefeatReward_Success() {
        testProgress.setDefeated(true);
        testProgress.setRewardClaimed(false);

        UserResponse mockUserRes = new UserResponse(
                1L, "hero@questlog.com", "Kaelen", "WARRIOR", 3, 50, 0, 300, 150, false
        );

        when(dailyBossRepository.findByBossDate(any(LocalDate.class))).thenReturn(Optional.of(testBoss));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBossProgressRepository.findByUserIdAndBossId(1L, 10L)).thenReturn(Optional.of(testProgress));
        when(userService.addCoins(1L, 50)).thenReturn(mockUserRes);

        ClaimRewardResponse response = raidBossService.claimDefeatReward(1L);

        assertThat(response.success()).isTrue();
        assertThat(response.coinsAwarded()).isEqualTo(50);
        assertThat(response.newUserCoins()).isEqualTo(150);

        verify(userService, times(1)).addXp(1L, 50, "STRENGTH");
        verify(userService, times(1)).addCoins(1L, 50);
        verify(userBossProgressRepository, times(1)).save(testProgress);
        assertThat(testProgress.isRewardClaimed()).isTrue();
    }

    @Test
    void claimDefeatReward_NotDefeated_ThrowsException() {
        testProgress.setDefeated(false);

        when(dailyBossRepository.findByBossDate(any(LocalDate.class))).thenReturn(Optional.of(testBoss));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userBossProgressRepository.findByUserIdAndBossId(1L, 10L)).thenReturn(Optional.of(testProgress));

        assertThatThrownBy(() -> raidBossService.claimDefeatReward(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Raid Boss belum dikalahkan");

        verify(userBossProgressRepository, never()).save(any(UserBossProgress.class));
    }
}
