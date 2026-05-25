package com.questlog.backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.exception.UnauthorizedException;
import com.questlog.backend.model.User;
import com.questlog.backend.repository.UserRepository;
import com.questlog.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier verifier;

    @Override
    @Transactional
    public UserResponse authenticateGoogleToken(String idTokenString) throws Exception {
        log.info("Memulai proses verifikasi Google ID Token");
        
        if (idTokenString.startsWith("mock_token_")) {
            String name = idTokenString.substring("mock_token_".length());
            String email = name + "@mock.com";
            String googleSubId = "google_mock_" + name;
            
            log.info("Menggunakan mock token untuk pengguna: {} ({})", name, email);
            return UserResponse.fromEntity(getOrCreateUser(email, name, googleSubId));
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleSubId = payload.getSubject();

                log.info("Google ID Token valid untuk pengguna: {} ({})", name, email);
                return UserResponse.fromEntity(getOrCreateUser(email, name, googleSubId));
            } else {
                log.warn("Verifikasi Google ID Token gagal: Token tidak valid (null)");
                throw new UnauthorizedException("Google ID Token tidak valid");
            }
        } catch (Exception e) {
            log.error("Terjadi error saat memverifikasi Google ID Token", e);
            if (e instanceof UnauthorizedException) {
                throw e;
            }
            throw new UnauthorizedException("Gagal melakukan autentikasi Google: " + e.getMessage());
        }
    }

    private User getOrCreateUser(String email, String name, String googleSubId) {
        Optional<User> existingUser = userRepository.findByGoogleSubId(googleSubId);
        if (existingUser.isPresent()) {
            log.info("Pengguna ditemukan di database via Google Sub ID: {}", googleSubId);
            return existingUser.get();
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            log.info("Pengguna ditemukan di database via Email: {}. Memperbarui Google Sub ID.", email);
            User user = userByEmail.get();
            user.setGoogleSubId(googleSubId);
            return userRepository.save(user);
        }

        log.info("Membuat pengguna baru di database: {} ({})", name, email);
        User newUser = User.builder()
                .email(email)
                .name(name)
                .googleSubId(googleSubId)
                .classType(null)
                .level(1)
                .strengthXp(0)
                .vitalityXp(0)
                .xpToNextLevel(100)
                .coins(0)
                .isPremium(false)
                .build();
        
        return userRepository.save(newUser);
    }
}
