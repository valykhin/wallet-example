package ru.ivalykhin.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivalykhin.wallet.entity.Wallet;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
}
