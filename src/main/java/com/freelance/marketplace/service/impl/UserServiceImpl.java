package com.freelance.marketplace.service.impl;

import com.freelance.marketplace.dto.request.RegisterRequest;
import com.freelance.marketplace.dto.response.AuthResponse;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.entity.Wallet;
import com.freelance.marketplace.enums.Role;
import com.freelance.marketplace.enums.UserStatus;
import com.freelance.marketplace.exception.DuplicateEmailException;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.repository.WalletRepository;
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

    @Override
    @Transactional // Đảm bảo cả User và Wallet được tạo cùng lúc, nếu 1 trong 2 lỗi thì cả 2 rollback
    public AuthResponse registerUser(RegisterRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        // Tạo User mới
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)          // Role enum
                .status(UserStatus.ACTIVE) // UserStatus enum (nếu đã sửa)
                .build();

        User savedUser = userRepository.save(user);

        // Tạo Wallet mới cho User
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO) // Mặc định số dư là 0
                .lockedBalance(BigDecimal.ZERO) // Mặc định số dư bị khóa là 0
                .build();

        walletRepository.save(wallet);

        // Trả về response
        return AuthResponse.builder()
                .accessToken("") // Token sẽ được cấp sau khi đăng nhập, ở đây để trống
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .build();
    }
}
