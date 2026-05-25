package com.questlog.backend.controller;

import com.questlog.backend.dto.QuestResponse;
import com.questlog.backend.service.QuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
@Tag(name = "Quest API", description = "Manajemen quest harian pahlawan (Strength & Vitality)")
public class QuestController {

    private final QuestService questService;

    @GetMapping("/daily/{userId}")
    @Operation(summary = "Mendapatkan quest harian", description = "Mengambil daftar quest harian pahlawan. Jika belum di-generate untuk hari ini, 4 quest harian baru akan otomatis dibuat sesuai kelas pahlawan.")
    public ResponseEntity<List<QuestResponse>> getDailyQuests(@PathVariable Long userId) {
        List<QuestResponse> response = questService.getDailyQuests(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Menyelesaikan quest", description = "Menandai quest sebagai selesai dan menghadiahi pahlawan dengan XP sesuai jenis quest")
    public ResponseEntity<QuestResponse> completeQuest(@PathVariable Long id) {
        QuestResponse response = questService.completeQuest(id);
        return ResponseEntity.ok(response);
    }
}
