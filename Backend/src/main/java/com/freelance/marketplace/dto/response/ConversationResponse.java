package com.freelance.marketplace.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
  private Long id;
  private Long jobId;
  private String jobTitle;
  private Long partnerId;
  private String partnerName;
  private long unreadCount;
  private String lastMessage;
  private LocalDateTime lastMessageAt;
}
