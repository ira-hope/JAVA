package com.example.project.service;

/**
 * Defines customer create, update, and query operations.
 */

import com.example.project.dto.request.CustomerCreateRequest;
import com.example.project.dto.request.CustomerUpdateRequest;
import com.example.project.dto.response.CustomerResponse;
import com.example.project.security.UserPrincipal;

public interface CustomerService {

	CustomerResponse createByAdmin(CustomerCreateRequest request, String performedBy);

	CustomerResponse getMyProfile(UserPrincipal principal);

	CustomerResponse updateMyProfile(UserPrincipal principal, CustomerUpdateRequest request);

	void delete(Long id);
}
