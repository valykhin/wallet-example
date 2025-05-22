package ru.ivalykhin.wallet.service;

import ru.ivalykhin.wallet.dto.WalletOperationEvent;

import java.util.UUID;

public interface WalletOperationEventProducer {
    UUID sendWalletOperationEvent(WalletOperationEvent walletOperationEvent);
}
