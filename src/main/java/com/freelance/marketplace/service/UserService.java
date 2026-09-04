package com.freelance.marketplace.service;

import com.freelance.marketplace.dto.request.RegisterRequest;
import com.freelance.marketplace.dto.response.AuthResponse;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);
}
