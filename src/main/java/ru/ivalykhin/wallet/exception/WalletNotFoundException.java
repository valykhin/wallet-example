package ru.ivalykhin.wallet.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class WalletNotFoundException extends RuntimeException implements WalletBusinessException {
    public final String errorCode = "WALLET_NOT_FOUND";

    public WalletNotFoundException(UUID walletId) {
        super("Wallet not found: " + walletId);
    }
}
