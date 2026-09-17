package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.SendMessageRequest;
import com.freelance.marketplace.dto.response.*;
import com.freelance.marketplace.service.message.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MessageController {
  private final MessageService service;

  @PostMapping("/jobs/{jobId}/conversation")
  public ResponseEntity<ConversationResponse> open(Authentication a, @PathVariable Long jobId) {
    return new ResponseEntity<>(service.open(a.getName(), jobId), HttpStatus.CREATED);
  }

  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationResponse>> list(Authentication a) {
    return ResponseEntity.ok(service.list(a.getName()));
  }

  @GetMapping("/conversations/{id}/messages")
  public ResponseEntity<PageResponse<MessageResponse>> messages(
      Authentication a,
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
    return ResponseEntity.ok(service.messages(a.getName(), id, page, size));
  }

  @PostMapping("/conversations/{id}/messages")
  public ResponseEntity<MessageResponse> send(
      Authentication a, @PathVariable Long id, @Valid @RequestBody SendMessageRequest r) {
    return new ResponseEntity<>(service.send(a.getName(), id, r.getBody()), HttpStatus.CREATED);
  }

  @PostMapping("/conversations/{id}/read")
  public ResponseEntity<Void> read(Authentication a, @PathVariable Long id) {
    service.read(a.getName(), id);
    return ResponseEntity.noContent().build();
  }
}
