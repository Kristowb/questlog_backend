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
    name = "daily_bosses",
    indexes = {
        @Index(name = "idx_daily_boss_date", columnList = "boss_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBoss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_max_hp", nullable = false)
    private double baseMaxHp;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "boss_date", unique = true, nullable = false)
    private LocalDate bossDate;
}
