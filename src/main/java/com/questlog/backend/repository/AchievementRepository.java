package com.questlog.backend.repository;

import com.questlog.backend.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByTitle(String title);
    List<Achievement> findByType(String type);
}
