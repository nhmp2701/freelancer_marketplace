package com.freelance.marketplace.service.impl;

import com.freelance.marketplace.dto.request.LoginRequest;
import com.freelance.marketplace.dto.request.RegisterRequest;
import com.freelance.marketplace.dto.response.AuthResponse;
import com.freelance.marketplace.dto.response.LoginResponse;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.entity.Wallet;
import com.freelance.marketplace.enums.Role;
import com.freelance.marketplace.enums.UserStatus;
import com.freelance.marketplace.exception.DuplicateEmailException;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.repository.WalletRepository;
import com.freelance.marketplace.security.JwtUtil;
import com.freelance.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;  // <--- THÊM DÒNG NÀY

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        // ... code đã có (không thay đổi)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .lockedBalance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Kiểm tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Kiểm tra tài khoản có bị khóa không
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new RuntimeException("Account is locked");
        }

        // 4. Tạo JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 5. Trả về response
        return LoginResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}