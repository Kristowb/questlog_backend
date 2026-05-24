package com.questlog.backend.repository;

import com.questlog.backend.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    List<WorkoutLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}
