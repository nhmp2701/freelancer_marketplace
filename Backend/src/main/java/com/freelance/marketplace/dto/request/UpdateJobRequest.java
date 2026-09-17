package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Data
public class UpdateJobRequest {

  @Size(max = 255, message = "Title must be less than 255 characters")
  private String title;

  private String description;

  @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
  private BigDecimal budget;

  private LocalDateTime deadline;

  private Set<@NotBlank @Size(max = 100) String> skills;
}
