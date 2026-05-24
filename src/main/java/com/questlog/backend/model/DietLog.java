package com.questlog.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "diet_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    private double protein; // in grams

    private double carbs; // in grams

    private double fat; // in grams

    private double calories; // in kcal

    @Column(name = "log_date")
    private LocalDate logDate;
}
