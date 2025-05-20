package ru.ivalykhin.wallet.exception;

import lombok.Getter;

@Getter
public class InsufficientFundsExceptionWallet extends RuntimeException implements WalletBusinessException {
    private final String errorCode = "INSUFFICIENT_FUNDS";

    public InsufficientFundsExceptionWallet() {
        super("Insufficient funds");
    }
}
