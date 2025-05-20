package ru.ivalykhin.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "wallets")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long balance;

    @Version
    private Long version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
