package ru.ivalykhin.wallet.exception;

import lombok.Getter;
import ru.ivalykhin.wallet.entity.OperationType;

@Getter
public class InvalidOperationExceptionWallet extends RuntimeException implements WalletBusinessException {
    private final String errorCode = "INVALID_OPERATION";

    public InvalidOperationExceptionWallet(OperationType operationType) {
        super("Invalid operation type: " + operationType);
    }
}
