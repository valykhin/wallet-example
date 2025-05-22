package ru.ivalykhin.wallet.service;

import java.util.Map;

public interface WalletOperationEventConsumer {
    void consumeWalletOperationEvent(Map<String, Object> messageHeaders, String messageBody);
}
