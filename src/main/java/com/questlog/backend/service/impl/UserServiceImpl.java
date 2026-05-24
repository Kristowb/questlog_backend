package com.questlog.backend.service.impl;

import com.questlog.backend.exception.BadRequestException;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.User;
import com.questlog.backend.repository.UserRepository;
import com.questlog.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public com.questlog.backend.dto.UserResponse getUserById(Long id) {
        log.info("Mengambil data user dengan ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + id + " tidak ditemukan"));
        return com.questlog.backend.dto.UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public com.questlog.backend.dto.UserResponse chooseClass(Long id, String classType) {
        log.info("Proses pemilihan kelas untuk user ID {}: {}", id, classType);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + id + " tidak ditemukan"));
        
        if (user.getClassType() != null) {
            log.warn("User ID {} mencoba memilih kelas kembali, padahal sudah memiliki kelas: {}", id, user.getClassType());
            throw new BadRequestException("Kelas pahlawan sudah pernah dipilih dan tidak dapat diubah");
        }
        
        String upperClass = classType.toUpperCase();
        if (!"WARRIOR".equals(upperClass) && !"ARCHER".equals(upperClass)) {
            log.warn("User ID {} mencoba memilih jenis kelas tidak valid: {}", id, classType);
            throw new BadRequestException("Jenis kelas tidak valid. Pilihan yang tersedia: WARRIOR atau ARCHER");
        }

        user.setClassType(upperClass);
        log.info("User ID {} berhasil memilih kelas {}", id, upperClass);
        return com.questlog.backend.dto.UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public com.questlog.backend.dto.UserResponse addXp(Long userId, int xpAmount, String xpType) {
        log.info("Menambahkan {} XP bertipe {} ke user ID {}", xpAmount, xpType, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));

        switch (xpType.toUpperCase()) {
            case "STRENGTH" -> user.setStrengthXp(user.getStrengthXp() + xpAmount);
            case "VITALITY" -> user.setVitalityXp(user.getVitalityXp() + xpAmount);
            default -> {
                log.warn("Tipe XP tidak valid: {}", xpType);
                throw new BadRequestException("Tipe XP tidak valid. Pilihan yang tersedia: STRENGTH atau VITALITY");
            }
        }

        checkLevelUp(user);
        return com.questlog.backend.dto.UserResponse.fromEntity(userRepository.save(user));
    }

    private void checkLevelUp(User user) {
        int totalXp = user.getStrengthXp() + user.getVitalityXp();
        int xpToNext = user.getXpToNextLevel();

        while (totalXp >= xpToNext) {
            int oldLevel = user.getLevel();
            user.setLevel(oldLevel + 1);
            user.setCoins(user.getCoins() + (user.getLevel() * 10));
            log.info("User ID {} LEVEL UP dari {} ke {}! Mendapatkan {} koin bonus.", user.getId(), oldLevel, user.getLevel(), user.getLevel() * 10);
            
            int over = totalXp - xpToNext;
            if ("STRENGTH".equalsIgnoreCase(user.getClassType())) {
                user.setStrengthXp(over / 2 + (over % 2));
                user.setVitalityXp(over / 2);
            } else {
                user.setVitalityXp(over / 2 + (over % 2));
                user.setStrengthXp(over / 2);
            }

            user.setXpToNextLevel(user.getLevel() * 100);
            
            totalXp = user.getStrengthXp() + user.getVitalityXp();
            xpToNext = user.getXpToNextLevel();
        }
    }

    @Override
    public List<com.questlog.backend.dto.UserResponse> getLeaderboard() {
        log.info("Mengambil data leaderboard pahlawan");
        return userRepository.findAllByOrderByLevelDescStrengthXpDesc().stream()
                .map(com.questlog.backend.dto.UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public com.questlog.backend.dto.UserResponse setPremium(Long userId, boolean isPremium) {
        log.info("Mengubah status premium user ID {} menjadi {}", userId, isPremium);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + userId + " tidak ditemukan"));
        user.setPremium(isPremium);
        return com.questlog.backend.dto.UserResponse.fromEntity(userRepository.save(user));
    }
}
