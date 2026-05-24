package com.questlog.backend.controller;

import com.questlog.backend.dto.TokenRequest;
import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<UserResponse> authenticateGoogle(@Valid @RequestBody TokenRequest tokenRequest) throws Exception {
        UserResponse response = authService.authenticateGoogleToken(tokenRequest.idToken());
        return ResponseEntity.ok(response);
    }
}
