package com.example.project.service;

import com.example.project.dto.request.UserCreateRequest;
import com.example.project.dto.request.UserUpdateRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

	PagedResponse<UserResponse> findAll(Pageable pageable, String search);

	UserResponse findById(Long id);

	UserResponse getCurrentUser(String email);

	UserResponse create(UserCreateRequest request);

	UserResponse update(Long id, UserUpdateRequest request);

	void delete(Long id);
}
