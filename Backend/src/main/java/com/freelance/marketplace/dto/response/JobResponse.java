package com.freelance.marketplace.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class JobResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long freelancerId;
    private String freelancerName;
    private String title;
    private String description;
    private BigDecimal budget;
    private String status;
    private LocalDateTime deadline;
    private Set<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}