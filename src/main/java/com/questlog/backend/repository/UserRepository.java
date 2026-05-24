package com.questlog.backend.repository;

import com.questlog.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleSubId(String googleSubId);
    List<User> findAllByOrderByLevelDescStrengthXpDesc();
}
