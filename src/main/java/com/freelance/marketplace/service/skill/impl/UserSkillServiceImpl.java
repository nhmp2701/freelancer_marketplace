package com.freelance.marketplace.service.skill.impl;

import com.freelance.marketplace.dto.request.AddSkillRequest;
import com.freelance.marketplace.entity.Skill;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.repository.SkillRepository;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.skill.UserSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public void addSkillToUser(String email, AddSkillRequest request) {
        User user = findUser(email);
        String skillName = request.getName().trim();
        Skill skill = skillRepository.findByName(skillName)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

        user.getSkills().add(skill);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeSkillFromUser(String email, String skillName) {
        User user = findUser(email);
        user.getSkills().removeIf(skill -> skill.getName().equals(skillName));
        userRepository.save(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
