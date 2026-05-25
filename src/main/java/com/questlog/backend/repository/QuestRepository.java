package com.questlog.backend.repository;

import com.questlog.backend.model.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByUserIdAndQuestDate(Long userId, LocalDate questDate);
    long countByUserIdAndIsCompleted(Long userId, boolean isCompleted);
    long countByUserIdAndTitleAndIsCompleted(Long userId, String title, boolean isCompleted);
}
