package com.questlog.backend.repository;

import com.questlog.backend.model.DailyBoss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyBossRepository extends JpaRepository<DailyBoss, Long> {
    Optional<DailyBoss> findByBossDate(LocalDate date);
}
