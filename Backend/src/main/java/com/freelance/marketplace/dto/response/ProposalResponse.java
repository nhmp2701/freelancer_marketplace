package com.freelance.marketplace.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProposalResponse {
  private Long id;
  private Long jobId;
  private String jobTitle;
  private Long freelancerId;
  private String freelancerName;
  private String coverLetter;
  private BigDecimal bidAmount;
  private Integer deliveryDays;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
