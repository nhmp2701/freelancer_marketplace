package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.AddSkillRequest;
import com.freelance.marketplace.service.skill.UserSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/skills")
@RequiredArgsConstructor
public class UserSkillController {

  private final UserSkillService userSkillService;

  @PostMapping
  public ResponseEntity<Void> addSkill(
      Authentication authentication, @Valid @RequestBody AddSkillRequest request) {
    userSkillService.addSkillToUser(authentication.getName(), request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{skillName}")
  public ResponseEntity<Void> removeSkill(
      Authentication authentication, @PathVariable String skillName) {
    userSkillService.removeSkillFromUser(authentication.getName(), skillName);
    return ResponseEntity.ok().build();
  }
}
