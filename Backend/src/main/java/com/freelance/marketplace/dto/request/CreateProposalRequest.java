package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateProposalRequest {
  @NotBlank
  @Size(max = 5000)
  private String coverLetter;

  @NotNull
  @DecimalMin("0.01")
  private BigDecimal bidAmount;

  @NotNull
  @Min(1)
  private Integer deliveryDays;
}
