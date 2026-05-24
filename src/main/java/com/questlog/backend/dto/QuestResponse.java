package com.questlog.backend.dto;

import com.questlog.backend.model.Quest;
import java.time.LocalDate;

public record QuestResponse(
    Long id,
    Long userId,
    String title,
    String description,
    String type,
    boolean isCompleted,
    int xpReward,
    LocalDate questDate
) {
    public static QuestResponse fromEntity(Quest quest) {
        if (quest == null) return null;
        return new QuestResponse(
            quest.getId(),
            quest.getUserId(),
            quest.getTitle(),
            quest.getDescription(),
            quest.getType(),
            quest.isCompleted(),
            quest.getXpReward(),
            quest.getQuestDate()
        );
    }
}
