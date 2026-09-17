package com.freelance.marketplace.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
  private Long id;
  private Long jobId;
  private String jobTitle;
  private Long reviewerId;
  private String reviewerName;
  private Long revieweeId;
  private Integer rating;
  private String comment;
  private LocalDateTime createdAt;
}
