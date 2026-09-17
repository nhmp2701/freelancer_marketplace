package com.freelance.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.freelance.marketplace.dto.request.TopUpRequest;
import com.freelance.marketplace.entity.*;
import com.freelance.marketplace.repository.*;
import com.freelance.marketplace.service.wallet.WalletService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WalletServiceTest {
  private final UserRepository users = mock(UserRepository.class);
  private final WalletRepository wallets = mock(WalletRepository.class);
  private final WalletTransactionRepository transactions = mock(WalletTransactionRepository.class);
  private final WalletService service = new WalletService(users, wallets, transactions);

  @Test
  void topUpWritesLedgerAndRetryIsIdempotent() {
    User user = User.builder().id(1L).email("user@example.com").build();
    Wallet wallet =
        Wallet.builder()
            .id(2L)
            .user(user)
            .balance(BigDecimal.ZERO)
            .lockedBalance(BigDecimal.ZERO)
            .build();
    TopUpRequest request = new TopUpRequest();
    request.setAmount(new BigDecimal("10000"));
    request.setIdempotencyKey("request-1");
    when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(wallets.findLockedByUserId(user.getId())).thenReturn(Optional.of(wallet));
    when(transactions.findByWalletIdAndIdempotencyKey(wallet.getId(), request.getIdempotencyKey()))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(WalletTransaction.builder().build()));

    service.topUp(user.getEmail(), request);
    service.topUp(user.getEmail(), request);

    assertThat(wallet.getBalance()).isEqualByComparingTo("10000");
    verify(transactions, times(1)).save(any(WalletTransaction.class));
  }

  @Test
  void holdAndReleaseTransferEscrowExactlyOnce() {
    User client = User.builder().id(1L).build();
    User freelancer = User.builder().id(2L).build();
    Job job = Job.builder().id(9L).client(client).freelancer(freelancer).build();
    Wallet clientWallet =
        Wallet.builder()
            .id(11L)
            .user(client)
            .balance(new BigDecimal("500"))
            .lockedBalance(BigDecimal.ZERO)
            .build();
    Wallet freelancerWallet =
        Wallet.builder()
            .id(12L)
            .user(freelancer)
            .balance(BigDecimal.ZERO)
            .lockedBalance(BigDecimal.ZERO)
            .build();
    when(wallets.findLockedByUserId(client.getId())).thenReturn(Optional.of(clientWallet));
    when(wallets.findLockedByUserId(freelancer.getId())).thenReturn(Optional.of(freelancerWallet));
    when(transactions.findByWalletIdAndIdempotencyKey(anyLong(), anyString()))
        .thenReturn(Optional.empty());

    service.hold(client, job, new BigDecimal("300"));
    service.release(client, freelancer, job, new BigDecimal("300"));

    assertThat(clientWallet.getBalance()).isEqualByComparingTo("200");
    assertThat(clientWallet.getLockedBalance()).isZero();
    assertThat(freelancerWallet.getBalance()).isEqualByComparingTo("300");
    verify(transactions, times(3)).save(any(WalletTransaction.class));
  }
}
