package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.UpdateProfileRequest;
import com.freelance.marketplace.dto.response.UserProfileResponse;
import com.freelance.marketplace.service.profile.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // API công khai: xem profile của bất kỳ user nào theo ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.getUserProfile(userId));
    }

    // API yêu cầu đăng nhập: cập nhật profile của chính mình
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(userProfileService.updateUserProfile(email, request));
    }
}