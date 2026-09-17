package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.WalletTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
  List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);

  Optional<WalletTransaction> findByWalletIdAndIdempotencyKey(Long walletId, String idempotencyKey);
}
