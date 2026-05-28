package com.questlog.backend.controller;

import com.questlog.backend.dto.DailyBossResponse;
import com.questlog.backend.dto.ClaimRewardResponse;
import com.questlog.backend.service.RaidBossService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/boss")
@RequiredArgsConstructor
@Tag(name = "Raid Boss API", description = "Manajemen pertarungan dan klaim reward Raid Boss Harian")
public class RaidBossController {

    private final RaidBossService raidBossService;

    @GetMapping("/active/{userId}")
    @Operation(summary = "Mendapatkan status Raid Boss aktif", description = "Mengambil data bos harian hari ini yang disesuaikan dengan level pahlawan.")
    public ResponseEntity<DailyBossResponse> getActiveBoss(@PathVariable Long userId) {
        DailyBossResponse response = raidBossService.getActiveBossForUser(userId, LocalDate.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/claim-reward/{userId}")
    @Operation(summary = "Mengklaim hadiah kekalahan Raid Boss", description = "Mendapatkan hadiah koin (+50) dan XP (+50 STRENGTH XP) jika Raid Boss harian berhasil dikalahkan.")
    public ResponseEntity<ClaimRewardResponse> claimDefeatReward(@PathVariable Long userId) {
        ClaimRewardResponse response = raidBossService.claimDefeatReward(userId);
        return ResponseEntity.ok(response);
    }
}
