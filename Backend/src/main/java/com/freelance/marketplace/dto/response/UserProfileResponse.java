package com.freelance.marketplace.dto.response;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
  private Long id;
  private String email;
  private String fullName;
  private String bio;
  private String avatarUrl;
  private BigDecimal reputationScore;
  private String status;
  private Set<String> skills;
}
