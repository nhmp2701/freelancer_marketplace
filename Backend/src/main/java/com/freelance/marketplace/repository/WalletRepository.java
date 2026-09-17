package com.freelance.marketplace.repository;

import com.freelance.marketplace.entity.Wallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
  Optional<Wallet> findByUserId(Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Wallet> findLockedByUserId(Long userId);
}
