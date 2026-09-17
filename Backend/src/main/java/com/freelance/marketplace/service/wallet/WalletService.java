package com.freelance.marketplace.service.wallet;

import com.freelance.marketplace.dto.request.TopUpRequest;
import com.freelance.marketplace.dto.response.WalletResponse;
import com.freelance.marketplace.dto.response.WalletTransactionResponse;
import com.freelance.marketplace.entity.Job;
import com.freelance.marketplace.entity.User;
import com.freelance.marketplace.entity.Wallet;
import com.freelance.marketplace.entity.WalletTransaction;
import com.freelance.marketplace.exception.ResourceNotFoundException;
import com.freelance.marketplace.repository.UserRepository;
import com.freelance.marketplace.repository.WalletRepository;
import com.freelance.marketplace.repository.WalletTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {
  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final WalletTransactionRepository transactionRepository;

  /** Lấy số dư khả dụng và số tiền đang được tạm giữ của chính chủ ví. */
  @Transactional(readOnly = true)
  public WalletResponse get(String email) {
    return wallet(findWallet(email));
  }

  /** Lấy sổ giao dịch theo thứ tự mới nhất để hiển thị và xuất sao kê. */
  @Transactional(readOnly = true)
  public List<WalletTransactionResponse> history(String email) {
    return transactionRepository
        .findByWalletIdOrderByCreatedAtDesc(findWallet(email).getId())
        .stream()
        .map(this::transaction)
        .toList();
  }

  /** Nạp tiền demo có khóa ví và khóa idempotency để một yêu cầu không được cộng hai lần. */
  @Transactional
  public WalletResponse topUp(String email, TopUpRequest request) {
    Long userId =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"))
            .getId();
    Wallet wallet =
        walletRepository
            .findLockedByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    if (transactionRepository
        .findByWalletIdAndIdempotencyKey(wallet.getId(), request.getIdempotencyKey())
        .isPresent()) {
      return wallet(wallet);
    }
    wallet.setBalance(wallet.getBalance().add(request.getAmount()));
    walletRepository.save(wallet);
    transactionRepository.save(
        WalletTransaction.builder()
            .wallet(wallet)
            .type("TOPUP")
            .amount(request.getAmount())
            .balanceAfter(wallet.getBalance())
            .lockedAfter(wallet.getLockedBalance())
            .idempotencyKey(request.getIdempotencyKey())
            .build());
    return wallet(wallet);
  }

  /** Chuyển giá trị đề xuất từ số dư khả dụng sang khoản ký quỹ khi chọn người thực hiện. */
  @Transactional
  public void hold(User client, Job job, BigDecimal amount) {
    Wallet wallet =
        walletRepository
            .findLockedByUserId(client.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    String key = "job-" + job.getId() + "-hold";
    if (transactionRepository.findByWalletIdAndIdempotencyKey(wallet.getId(), key).isPresent()) {
      return;
    }
    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new IllegalStateException("Insufficient wallet balance for this proposal");
    }
    wallet.setBalance(wallet.getBalance().subtract(amount));
    wallet.setLockedBalance(wallet.getLockedBalance().add(amount));
    walletRepository.save(wallet);
    transactionRepository.save(
        WalletTransaction.builder()
            .wallet(wallet)
            .job(job)
            .type("HOLD")
            .amount(amount.negate())
            .balanceAfter(wallet.getBalance())
            .lockedAfter(wallet.getLockedBalance())
            .idempotencyKey(key)
            .build());
  }

  /**
   * Giải ngân nguyên tử khi nghiệm thu: khóa hai ví theo thứ tự ID để tránh deadlock, trừ tiền tạm
   * giữ của khách hàng và cộng tiền cho người thực hiện. Các khóa idempotency ngăn trả hai lần.
   */
  @Transactional
  public void release(User client, User freelancer, Job job, BigDecimal amount) {
    Long firstId = Math.min(client.getId(), freelancer.getId());
    Long secondId = Math.max(client.getId(), freelancer.getId());
    Wallet first =
        walletRepository
            .findLockedByUserId(firstId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    Wallet second =
        walletRepository
            .findLockedByUserId(secondId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    Wallet clientWallet = first.getUser().getId().equals(client.getId()) ? first : second;
    Wallet freelancerWallet = first.getUser().getId().equals(freelancer.getId()) ? first : second;
    String clientKey = "job-" + job.getId() + "-release-out";
    String freelancerKey = "job-" + job.getId() + "-release-in";
    if (transactionRepository
        .findByWalletIdAndIdempotencyKey(freelancerWallet.getId(), freelancerKey)
        .isPresent()) {
      return;
    }
    BigDecimal fromEscrow = clientWallet.getLockedBalance().min(amount);
    BigDecimal legacyRemainder = amount.subtract(fromEscrow);
    if (clientWallet.getBalance().compareTo(legacyRemainder) < 0) {
      throw new IllegalStateException("Escrow balance is insufficient");
    }
    clientWallet.setLockedBalance(clientWallet.getLockedBalance().subtract(fromEscrow));
    clientWallet.setBalance(clientWallet.getBalance().subtract(legacyRemainder));
    freelancerWallet.setBalance(freelancerWallet.getBalance().add(amount));
    walletRepository.save(clientWallet);
    walletRepository.save(freelancerWallet);
    transactionRepository.save(
        WalletTransaction.builder()
            .wallet(clientWallet)
            .job(job)
            .type("RELEASE")
            .amount(amount.negate())
            .balanceAfter(clientWallet.getBalance())
            .lockedAfter(clientWallet.getLockedBalance())
            .idempotencyKey(clientKey)
            .build());
    transactionRepository.save(
        WalletTransaction.builder()
            .wallet(freelancerWallet)
            .job(job)
            .type("RELEASE")
            .amount(amount)
            .balanceAfter(freelancerWallet.getBalance())
            .lockedAfter(freelancerWallet.getLockedBalance())
            .idempotencyKey(freelancerKey)
            .build());
  }

  private Wallet findWallet(String email) {
    Long userId =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"))
            .getId();
    return walletRepository
        .findByUserId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
  }

  private WalletResponse wallet(Wallet value) {
    return WalletResponse.builder()
        .balance(value.getBalance())
        .lockedBalance(value.getLockedBalance())
        .build();
  }

  private WalletTransactionResponse transaction(WalletTransaction value) {
    return WalletTransactionResponse.builder()
        .id(value.getId())
        .jobId(value.getJob() == null ? null : value.getJob().getId())
        .type(value.getType())
        .amount(value.getAmount())
        .balanceAfter(value.getBalanceAfter())
        .lockedAfter(value.getLockedAfter())
        .createdAt(value.getCreatedAt())
        .build();
  }
}
