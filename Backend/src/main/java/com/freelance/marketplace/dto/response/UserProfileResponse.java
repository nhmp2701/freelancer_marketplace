package com.freelance.marketplace.dto.response;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.Set;

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
