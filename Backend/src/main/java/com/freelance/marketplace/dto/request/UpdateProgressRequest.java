package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProgressRequest {
  @Min(0)
  @Max(100)
  private int progress;

  @NotBlank
  @Size(max = 500)
  private String note;
}
