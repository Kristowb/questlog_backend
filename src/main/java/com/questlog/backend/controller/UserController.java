package com.questlog.backend.controller;

import com.questlog.backend.dto.ClassSelectionRequest;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/class")
    public ResponseEntity<UserResponse> chooseClass(@PathVariable Long id, @Valid @RequestBody ClassSelectionRequest request) {
        UserResponse response = userService.chooseClass(id, request.classType());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserResponse>> getLeaderboard() {
        List<UserResponse> response = userService.getLeaderboard();
        return ResponseEntity.ok(response);
    }
}
