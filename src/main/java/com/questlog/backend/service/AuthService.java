package com.questlog.backend.service;

import com.questlog.backend.dto.UserResponse;

public interface AuthService {
    UserResponse authenticateGoogleToken(String idTokenString) throws Exception;
}
