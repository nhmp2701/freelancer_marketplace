package com.freelance.marketplace.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.freelance.marketplace.entity.*;
import com.freelance.marketplace.repository.*;
import com.freelance.marketplace.service.message.MessageService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class MessageServiceTest {
  @Test
  void outsiderCannotOpenJobConversation() {
    UserRepository users = mock(UserRepository.class);
    JobRepository jobs = mock(JobRepository.class);
    ConversationRepository conversations = mock(ConversationRepository.class);
    MessageRepository messages = mock(MessageRepository.class);
    MessageService service = new MessageService(users, jobs, conversations, messages);
    User client = User.builder().id(1L).build();
    User freelancer = User.builder().id(2L).build();
    User outsider = User.builder().id(3L).email("outsider@example.com").build();
    Job job = Job.builder().id(10L).client(client).freelancer(freelancer).build();
    when(users.findByEmail(outsider.getEmail())).thenReturn(Optional.of(outsider));
    when(jobs.findById(job.getId())).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> service.open(outsider.getEmail(), job.getId()))
        .isInstanceOf(AccessDeniedException.class);
  }
}
