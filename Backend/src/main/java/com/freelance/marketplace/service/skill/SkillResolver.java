package com.freelance.marketplace.service.skill;

import com.freelance.marketplace.entity.Skill;
import com.freelance.marketplace.repository.SkillRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillResolver {
  private final SkillRepository skillRepository;

  public Skill resolve(String skillName) {
    String normalizedName = skillName.trim();
    return skillRepository
        .findByNameIgnoreCase(normalizedName)
        .orElseGet(() -> skillRepository.save(Skill.builder().name(normalizedName).build()));
  }

  public Set<Skill> resolve(Set<String> skillNames) {
    if (skillNames == null || skillNames.isEmpty()) return new HashSet<>();
    Set<Skill> skills = new HashSet<>();
    skillNames.stream()
        .filter(name -> name != null && !name.isBlank())
        .map(this::resolve)
        .forEach(skills::add);
    return skills;
  }
}
