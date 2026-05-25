package com.questlog.backend.repository;

import com.questlog.backend.model.DietLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DietLogRepository extends JpaRepository<DietLog, Long> {
    List<DietLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    long countByUserId(Long userId);
    long countByUserIdAndProteinGreaterThanEqual(Long userId, double protein);
}
