package com.freelance.marketplace.service;

import com.freelance.marketplace.dto.request.AddSkillRequest;
import com.freelance.marketplace.dto.request.UpdateProfileRequest;
import com.freelance.marketplace.dto.response.UserProfileResponse;
import com.freelance.marketplace.entity.Skill;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.enums.UserStatus;
import com.freelance.marketplace.repository.SkillRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.profile.impl.UserProfileServiceImpl;
import com.freelance.marketplace.service.skill.impl.UserSkillServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceSplitTest {

    @Test
    void updatesOnlyProvidedProfileFields() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = User.builder()
                .email("user@example.com")
                .fullName("Old name")
                .bio("Old bio")
                .status(UserStatus.ACTIVE)
                .skills(new HashSet<>())
                .build();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New name");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse response = new UserProfileServiceImpl(userRepository)
                .updateUserProfile(user.getEmail(), request);

        assertThat(response.getFullName()).isEqualTo("New name");
        assertThat(response.getBio()).isEqualTo("Old bio");
    }

    @Test
    void addsAndRemovesAnExistingSkill() {
        UserRepository userRepository = mock(UserRepository.class);
        SkillRepository skillRepository = mock(SkillRepository.class);
        User user = User.builder()
                .email("user@example.com")
                .skills(new HashSet<>())
                .build();
        Skill skill = Skill.builder().name("Java").build();
        AddSkillRequest request = new AddSkillRequest();
        request.setName(" Java ");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));

        UserSkillServiceImpl service = new UserSkillServiceImpl(userRepository, skillRepository);
        service.addSkillToUser(user.getEmail(), request);
        assertThat(user.getSkills()).containsExactly(skill);

        service.removeSkillFromUser(user.getEmail(), "Java");
        assertThat(user.getSkills()).isEmpty();
        verify(userRepository, times(2)).findByEmail(user.getEmail());
    }
}
