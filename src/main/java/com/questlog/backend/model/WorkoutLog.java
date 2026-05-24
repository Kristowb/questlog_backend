package com.questlog.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "workout_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_name", nullable = false)
    private String exerciseName;

    private int sets;

    private int reps;

    private double weight; // in kg

    @Column(name = "log_date")
    private LocalDate logDate;
}
