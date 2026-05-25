package com.questlog.backend.controller;

import com.questlog.backend.dto.WorkoutLogRequest;
import com.questlog.backend.dto.WorkoutLogResponse;
import com.questlog.backend.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
@Tag(name = "Workout API", description = "Pencatatan log latihan fisik harian (Strength/Workout)")
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    @Operation(summary = "Mencatat log latihan fisik", description = "Menambahkan latihan fisik baru dan secara otomatis menghadiahi pahlawan dengan +10 STRENGTH XP.")
    public ResponseEntity<WorkoutLogResponse> addWorkoutLog(@Valid @RequestBody WorkoutLogRequest request) {
        WorkoutLogResponse response = workoutService.addWorkoutLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/daily/{userId}")
    @Operation(summary = "Mendapatkan log latihan fisik harian", description = "Mengambil seluruh log latihan fisik yang dicatat oleh pahlawan pada tanggal tertentu (default hari ini)")
    public ResponseEntity<List<WorkoutLogResponse>> getDailyWorkouts(@PathVariable Long userId,
                                                                     @RequestParam(required = false)
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<WorkoutLogResponse> response = workoutService.getDailyWorkouts(userId, targetDate);
        return ResponseEntity.ok(response);
    }
}
