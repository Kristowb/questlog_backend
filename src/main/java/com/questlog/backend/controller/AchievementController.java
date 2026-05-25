package com.questlog.backend.controller;

import com.questlog.backend.dto.AchievementResponse;
import com.questlog.backend.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievement API", description = "Manajemen pencapaian/trofi pahlawan (Achievements)")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Mendapatkan status pencapaian pahlawan", description = "Mengambil seluruh daftar pencapaian beserta status pembukaan kunci (unlocked) untuk user tertentu.")
    public ResponseEntity<List<AchievementResponse>> getAchievementsForUser(@PathVariable Long userId) {
        List<AchievementResponse> response = achievementService.getAchievementsForUser(userId);
        return ResponseEntity.ok(response);
    }
}
