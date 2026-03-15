package com.fluenz.api.service;

import com.fluenz.api.dto.request.LoginRequest;
import com.fluenz.api.dto.request.RefreshTokenRequest;
import com.fluenz.api.dto.request.RegisterRequest;
import com.fluenz.api.dto.response.AuthResponse;
import com.fluenz.api.dto.response.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    UserResponse getCurrentUser(String email);
}
