package com.freelance.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "wallet_transactions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "idempotency_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id")
  private Job job;

  @Column(nullable = false, length = 30)
  private String type;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
  private BigDecimal balanceAfter;

  @Column(name = "locked_after", nullable = false, precision = 15, scale = 2)
  private BigDecimal lockedAfter;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
