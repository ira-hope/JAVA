package com.example.project.service;

/**
 * Defines registration, login, and token refresh operations.
 */

import com.example.project.dto.request.ActivateAccountRequest;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RefreshTokenRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.security.UserPrincipal;

public interface AuthService {

	String register(RegisterRequest request, UserPrincipal principal);

	AuthResponse login(LoginRequest request);

	AuthResponse refresh(RefreshTokenRequest request);

	void activateAccount(ActivateAccountRequest request);
}
