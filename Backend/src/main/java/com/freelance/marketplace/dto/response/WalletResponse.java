package com.freelance.marketplace.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletResponse {
  private BigDecimal balance;
  private BigDecimal lockedBalance;
}
