package com.freelance.marketplace.controller;

import com.freelance.marketplace.dto.request.TopUpRequest;
import com.freelance.marketplace.dto.response.WalletResponse;
import com.freelance.marketplace.dto.response.WalletTransactionResponse;
import com.freelance.marketplace.service.wallet.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet")
public class WalletController {
  private final WalletService walletService;

  @GetMapping
  public ResponseEntity<WalletResponse> get(Authentication auth) {
    return ResponseEntity.ok(walletService.get(auth.getName()));
  }

  @GetMapping("/transactions")
  public ResponseEntity<List<WalletTransactionResponse>> history(Authentication auth) {
    return ResponseEntity.ok(walletService.history(auth.getName()));
  }

  @PostMapping("/topup")
  public ResponseEntity<WalletResponse> topUp(
      Authentication auth, @Valid @RequestBody TopUpRequest request) {
    return ResponseEntity.ok(walletService.topUp(auth.getName(), request));
  }
}
