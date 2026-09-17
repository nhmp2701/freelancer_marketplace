package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddSkillRequest {
  @NotBlank(message = "Skill name is required")
  @Size(max = 100, message = "Skill name must be less than 100 characters")
  private String name;
}
