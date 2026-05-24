package com.questlog.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    @Column(name = "class_type")
    private String classType; // WARRIOR or ARCHER

    @Builder.Default
    private int level = 1;

    @Column(name = "strength_xp")
    @Builder.Default
    private int strengthXp = 0;

    @Column(name = "vitality_xp")
    @Builder.Default
    private int vitalityXp = 0;

    @Column(name = "xp_to_next_level")
    @Builder.Default
    private int xpToNextLevel = 100;

    @Builder.Default
    private int coins = 0;

    @Column(name = "is_premium")
    @Builder.Default
    private boolean isPremium = false;

    @Column(name = "google_sub_id", unique = true)
    private String googleSubId;
}
