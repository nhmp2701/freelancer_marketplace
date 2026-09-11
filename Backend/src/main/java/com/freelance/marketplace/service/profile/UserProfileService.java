package com.freelance.marketplace.service.profile;

import com.freelance.marketplace.dto.request.UpdateProfileRequest;
import com.freelance.marketplace.dto.response.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getUserProfile(Long userId);
    UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request);
}
