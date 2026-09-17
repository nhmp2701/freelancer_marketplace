package com.freelance.marketplace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestChangesRequest {
  @NotBlank
  @Size(max = 500)
  private String reason;
}
