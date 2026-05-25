package com.questlog.backend.controller;

import com.questlog.backend.dto.DietLogRequest;
import com.questlog.backend.dto.DietLogResponse;
import com.questlog.backend.service.DietService;
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
@RequestMapping("/api/v1/diet")
@RequiredArgsConstructor
@Tag(name = "Diet API", description = "Pencatatan asupan gizi harian (Vitality/Feast)")
public class DietController {

    private final DietService dietService;

    @PostMapping
    @Operation(summary = "Mencatat log diet", description = "Menambahkan log konsumsi makanan harian. Jika protein >= 30g, pahlawan akan mendapatkan bonus VITALITY XP tambahan.")
    public ResponseEntity<DietLogResponse> addDietLog(@Valid @RequestBody DietLogRequest request) {
        DietLogResponse response = dietService.addDietLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/daily/{userId}")
    @Operation(summary = "Mendapatkan log diet harian", description = "Mengambil daftar makanan yang dikonsumsi oleh pahlawan pada tanggal tertentu (default hari ini)")
    public ResponseEntity<List<DietLogResponse>> getDailyDiet(@PathVariable Long userId,
                                                              @RequestParam(required = false)
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<DietLogResponse> response = dietService.getDailyDiet(userId, targetDate);
        return ResponseEntity.ok(response);
    }
}
