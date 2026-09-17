package com.freelance.marketplace.service.skill;

import com.freelance.marketplace.dto.request.AddSkillRequest;

public interface UserSkillService {
  void addSkillToUser(String email, AddSkillRequest request);

  void removeSkillFromUser(String email, String skillName);
}
