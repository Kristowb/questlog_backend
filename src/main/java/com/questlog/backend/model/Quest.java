package com.questlog.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(
    name = "quests",
    indexes = {
        @Index(name = "idx_quests_user_date", columnList = "user_id, quest_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String type; // STRENGTH (Workout) or VITALITY (Feast/Diet)

    @Column(name = "is_completed")
    @Builder.Default
    private boolean isCompleted = false;

    @Column(name = "xp_reward")
    private int xpReward;

    @Column(name = "quest_date")
    private LocalDate questDate;
}
