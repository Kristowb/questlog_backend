package com.questlog.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(
    name = "user_boss_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "boss_id"}),
    indexes = {
        @Index(name = "idx_user_boss_user_boss", columnList = "user_id, boss_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBossProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Column(name = "current_hp", nullable = false)
    private double currentHp;

    @Column(name = "max_hp_scaled", nullable = false)
    private double maxHpScaled;

    @Column(name = "total_damage_dealt", nullable = false)
    private double totalDamageDealt;

    @Column(name = "is_defeated", nullable = false)
    private boolean isDefeated;

    @Column(name = "is_reward_claimed", nullable = false)
    private boolean isRewardClaimed;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
