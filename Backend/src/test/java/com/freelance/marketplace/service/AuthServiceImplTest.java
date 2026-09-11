package com.freelance.marketplace.service;

import com.freelance.marketplace.dto.request.LoginRequest;
import com.freelance.marketplace.dto.request.RegisterRequest;
import com.freelance.marketplace.dto.response.AuthResponse;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.Role;
import com.freelance.marketplace.enums.UserStatus;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.repository.WalletRepository;
import com.freelance.marketplace.security.JwtUtil;
import com.freelance.marketplace.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final WalletRepository walletRepository = mock(WalletRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AuthServiceImpl service = new AuthServiceImpl(
            userRepository, walletRepository, passwordEncoder, jwtUtil);

    @Test
    void rejectsUnknownEmailWithoutLeakingAccountState() {
        LoginRequest request = loginRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loginUser(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void rejectsLockedAccountWithTheSamePublicMessage() {
        LoginRequest request = loginRequest();
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash("hash")
                .role(Role.USER)
                .status(UserStatus.LOCKED)
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> service.loginUser(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void registrationCreatesWalletAndBearerToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        when(passwordEncoder.encode(request.getPassword())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(jwtUtil.generateToken(request.getEmail(), Role.USER.name())).thenReturn("token");

        AuthResponse response = service.registerUser(request);

        assertThat(response.getAccessToken()).isEqualTo("token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(walletRepository).save(any());
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");
        return request;
    }
}
