package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Data
public class CreateJobRequest {

  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must be less than 255 characters")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  @NotNull(message = "Budget is required")
  @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
  private BigDecimal budget;

  private LocalDateTime deadline;

  private Set<@NotBlank @Size(max = 100) String> skills;
}
