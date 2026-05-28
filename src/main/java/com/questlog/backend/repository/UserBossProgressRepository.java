package com.questlog.backend.repository;

import com.questlog.backend.model.UserBossProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserBossProgressRepository extends JpaRepository<UserBossProgress, Long> {
    Optional<UserBossProgress> findByUserIdAndBossId(Long userId, Long bossId);
}
