package com.freelance.marketplace.service;

import com.freelance.marketplace.dto.request.LoginRequest;
import com.freelance.marketplace.dto.request.RegisterRequest;
import com.freelance.marketplace.dto.response.AuthResponse;
import com.freelance.marketplace.dto.response.LoginResponse;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);
    LoginResponse loginUser(LoginRequest request);  // <--- THÊM DÒNG NÀY
}