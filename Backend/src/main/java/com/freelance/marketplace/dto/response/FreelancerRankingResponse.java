package com.freelance.marketplace.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FreelancerRankingResponse {
  private Long userId;
  private String fullName;
  private String avatarUrl;
  private BigDecimal averageRating;
  private long reviewCount;
  private long completedJobs;
}
