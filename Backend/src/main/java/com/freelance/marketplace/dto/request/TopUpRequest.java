package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class TopUpRequest {
  @NotNull
  @DecimalMin("10000")
  @DecimalMax("100000000")
  private BigDecimal amount;

  @NotBlank
  @Size(max = 100)
  private String idempotencyKey;
}
