package com.questlog.backend.service;

import com.questlog.backend.dto.QuestResponse;
import java.util.List;

public interface QuestService {
    List<QuestResponse> getDailyQuests(Long userId);
    QuestResponse completeQuest(Long questId);
}
