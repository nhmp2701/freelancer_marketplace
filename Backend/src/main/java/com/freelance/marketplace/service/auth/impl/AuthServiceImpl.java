package com.freelance.marketplace.service.auth.impl;

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
import com.freelance.marketplace.service.auth.AuthService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  @Override
  @Transactional
  public AuthResponse registerUser(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateEmailException("Email already exists: " + request.getEmail());
    }

    User user =
        User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .role(Role.USER)
            .status(UserStatus.ACTIVE)
            .build();

    User savedUser = userRepository.save(user);

    Wallet wallet =
        Wallet.builder()
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
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid email or password");
    }

    if (user.getStatus() == UserStatus.LOCKED) {
      throw new BadCredentialsException("Invalid email or password");
    }

    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

    return LoginResponse.builder()
        .accessToken(token)
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .role(user.getRole().name())
        .build();
  }
}
