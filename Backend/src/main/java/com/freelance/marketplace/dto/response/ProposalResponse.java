package com.freelance.marketplace.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
