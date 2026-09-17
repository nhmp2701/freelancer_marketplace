package com.freelance.marketplace.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
  private Long id;
  private Long senderId;
  private String senderName;
  private String body;
  private LocalDateTime createdAt;
}
