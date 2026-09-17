package com.freelance.marketplace.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletTransactionResponse {
  private Long id;
  private Long jobId;
  private String type;
  private BigDecimal amount;
  private BigDecimal balanceAfter;
  private BigDecimal lockedAfter;
  private LocalDateTime createdAt;
}
