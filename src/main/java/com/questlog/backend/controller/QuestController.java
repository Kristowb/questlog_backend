package com.questlog.backend.controller;

import com.questlog.backend.dto.QuestResponse;
import com.questlog.backend.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    @GetMapping("/daily/{userId}")
    public ResponseEntity<List<QuestResponse>> getDailyQuests(@PathVariable Long userId) {
        List<QuestResponse> response = questService.getDailyQuests(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<QuestResponse> completeQuest(@PathVariable Long id) {
        QuestResponse response = questService.completeQuest(id);
        return ResponseEntity.ok(response);
    }
}
