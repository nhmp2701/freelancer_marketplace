package com.freelance.marketplace.service.profile.impl;

import com.freelance.marketplace.dto.request.UpdateProfileRequest;
import com.freelance.marketplace.dto.response.UserProfileResponse;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return toResponse(userRepository.save(user));
    }

    private UserProfileResponse toResponse(User user) {
        Set<String> skills = user.getSkills() == null
                ? Collections.emptySet()
                : user.getSkills().stream().map(skill -> skill.getName()).collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .reputationScore(user.getReputationScore() == null ? null : user.getReputationScore().doubleValue())
                .status(user.getStatus().name())
                .skills(skills)
                .build();
    }
}
