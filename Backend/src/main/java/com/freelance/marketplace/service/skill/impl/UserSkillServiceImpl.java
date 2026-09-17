package com.freelance.marketplace.service.skill.impl;

import com.freelance.marketplace.dto.request.AddSkillRequest;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.service.skill.SkillResolver;
import com.freelance.marketplace.service.skill.UserSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService {

  private final UserRepository userRepository;
  private final SkillResolver skillResolver;

  @Override
  @Transactional
  public void addSkillToUser(String email, AddSkillRequest request) {
    User user = findUser(email);
    user.getSkills().add(skillResolver.resolve(request.getName()));
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void removeSkillFromUser(String email, String skillName) {
    User user = findUser(email);
    String normalizedName = skillName.trim();
    user.getSkills().removeIf(skill -> skill.getName().equalsIgnoreCase(normalizedName));
    userRepository.save(user);
  }

  private User findUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
  }
}
