package com.freelance.marketplace.service.message;

import com.freelance.marketplace.dto.response.*;
import com.freelance.marketplace.entity.*;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
  private final UserRepository userRepository;
  private final JobRepository jobRepository;
  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;

  /** Mở hội thoại theo dự án, đồng thời chặn mọi tài khoản không phải hai bên của dự án. */
  @Transactional
  public ConversationResponse open(String email, Long jobId) {
    User user = user(email);
    Job job =
        jobRepository
            .findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    checkParticipant(job, user);
    Conversation conversation =
        conversationRepository
            .findByJobId(jobId)
            .orElseGet(() -> conversationRepository.save(Conversation.builder().job(job).build()));
    return conversation(conversation, user);
  }

  /** Trả danh sách hội thoại cùng số tin chưa đọc và bản xem trước mới nhất. */
  @Transactional(readOnly = true)
  public List<ConversationResponse> list(String email) {
    User user = user(email);
    return conversationRepository.findForParticipant(user.getId()).stream()
        .map(c -> conversation(c, user))
        .toList();
  }

  /** Đọc một trang tin nhắn sau khi đã xác minh người gọi là thành viên hội thoại. */
  @Transactional(readOnly = true)
  public PageResponse<MessageResponse> messages(String email, Long id, int page, int size) {
    User user = user(email);
    Conversation c = get(id);
    checkParticipant(c.getJob(), user);
    return PageResponse.from(
        messageRepository.findByConversationId(
            id, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))),
        this::message);
  }

  /** Lưu tin nhắn mới; nội dung được chuẩn hóa khoảng trắng trước khi ghi. */
  @Transactional
  public MessageResponse send(String email, Long id, String body) {
    User user = user(email);
    Conversation c = get(id);
    checkParticipant(c.getJob(), user);
    return message(
        messageRepository.save(
            Message.builder().conversation(c).sender(user).body(body.trim()).build()));
  }

  /** Ghi thời điểm đọc riêng cho khách hàng hoặc người thực hiện. */
  @Transactional
  public void read(String email, Long id) {
    User user = user(email);
    Conversation c = get(id);
    checkParticipant(c.getJob(), user);
    if (c.getJob().getClient().getId().equals(user.getId())) {
      c.setClientReadAt(LocalDateTime.now());
    } else {
      c.setFreelancerReadAt(LocalDateTime.now());
    }
    conversationRepository.save(c);
  }

  /** Kết hợp thông tin dự án, đối tác và trạng thái đọc thành DTO cho giao diện. */
  private ConversationResponse conversation(Conversation c, User user) {
    boolean client = c.getJob().getClient().getId().equals(user.getId());
    User partner = client ? c.getJob().getFreelancer() : c.getJob().getClient();
    LocalDateTime readAt = client ? c.getClientReadAt() : c.getFreelancerReadAt();
    long unread =
        readAt == null
            ? messageRepository.countByConversationIdAndSenderIdNot(c.getId(), user.getId())
            : messageRepository.countByConversationIdAndSenderIdNotAndCreatedAtAfter(
                c.getId(), user.getId(), readAt);
    Message latest =
        messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(c.getId()).orElse(null);
    return ConversationResponse.builder()
        .id(c.getId())
        .jobId(c.getJob().getId())
        .jobTitle(c.getJob().getTitle())
        .partnerId(partner.getId())
        .partnerName(partner.getFullName())
        .unreadCount(unread)
        .lastMessage(latest == null ? null : latest.getBody())
        .lastMessageAt(latest == null ? null : latest.getCreatedAt())
        .build();
  }

  private MessageResponse message(Message m) {
    return MessageResponse.builder()
        .id(m.getId())
        .senderId(m.getSender().getId())
        .senderName(m.getSender().getFullName())
        .body(m.getBody())
        .createdAt(m.getCreatedAt())
        .build();
  }

  private Conversation get(Long id) {
    return conversationRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
  }

  private User user(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  /** Bảo vệ chống truy cập hội thoại theo ID của người dùng ngoài dự án. */
  private void checkParticipant(Job job, User user) {
    if (job.getFreelancer() == null
        || (!job.getClient().getId().equals(user.getId())
            && !job.getFreelancer().getId().equals(user.getId()))) {
      throw new AccessDeniedException("Only job participants can access messages");
    }
  }
}
