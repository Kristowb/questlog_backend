package com.questlog.backend.controller;

import com.questlog.backend.dto.ClassSelectionRequest;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User / Hero API", description = "Manajemen profil pahlawan, pemilihan kelas (Warrior/Archer), dan Leaderboard")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Mendapatkan profil pahlawan", description = "Mengambil data profil lengkap pahlawan berdasarkan ID user")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/class")
    @Operation(summary = "Memilih kelas pahlawan", description = "Memilih kelas Warrior atau Archer untuk pertama kalinya. Kelas tidak dapat diubah setelah dipilih.")
    public ResponseEntity<UserResponse> chooseClass(@PathVariable Long id, @Valid @RequestBody ClassSelectionRequest request) {
        UserResponse response = userService.chooseClass(id, request.classType());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Mendapatkan leaderboard pahlawan", description = "Mengambil daftar pahlawan terkuat berdasarkan level tertinggi dan XP terbanyak")
    public ResponseEntity<List<UserResponse>> getLeaderboard() {
        List<UserResponse> response = userService.getLeaderboard();
        return ResponseEntity.ok(response);
    }
}
