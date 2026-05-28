package com.example.project.service;

import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RefreshTokenRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.AuthResponse;

public interface AuthService {

	void register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	AuthResponse refresh(RefreshTokenRequest request);
}
