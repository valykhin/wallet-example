package ru.ivalykhin.wallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivalykhin.wallet.common.retry.OptimisticLockRetryService;
import ru.ivalykhin.wallet.dto.WalletOperationEvent;
import ru.ivalykhin.wallet.dto.WalletOperationResponse;
import ru.ivalykhin.wallet.entity.OperationType;
import ru.ivalykhin.wallet.entity.Wallet;
import ru.ivalykhin.wallet.exception.InsufficientFundsExceptionWallet;
import ru.ivalykhin.wallet.exception.InvalidOperationExceptionWallet;
import ru.ivalykhin.wallet.exception.WalletNotFoundException;
import ru.ivalykhin.wallet.repository.WalletRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final OptimisticLockRetryService optimisticLockRetryService;
    private final WalletOperationEventProducer walletOperationEventProducer;


    public WalletOperationResponse publishOperation(UUID walletId,
                                                    OperationType operationType,
                                                    Long amount) {

        WalletOperationEvent walletOperationEvent = WalletOperationEvent.builder()
                .walletId(walletId)
                .operationType(operationType)
                .amount(amount)
                .build();

        UUID eventId = walletOperationEventProducer.sendWalletOperationEvent(walletOperationEvent);

        walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        return WalletOperationResponse.builder()
                .walletId(walletId)
                .operationId(eventId)
                .build();
    }

    public void processOperation(UUID walletId, OperationType operationType, Long amount) throws Exception {
        optimisticLockRetryService.runWithRetry(
                () -> performOperation(walletId, operationType, amount));
    }

    @Transactional
    public Wallet performOperation(UUID walletId, OperationType operationType, Long amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (operationType == null) throw new InvalidOperationExceptionWallet(null);

        switch (operationType) {
            case DEPOSIT -> wallet.setBalance(wallet.getBalance() + amount);
            case WITHDRAW -> {
                if (wallet.getBalance() < amount) throw new InsufficientFundsExceptionWallet();
                wallet.setBalance(wallet.getBalance() - amount);
            }
            default -> throw new InvalidOperationExceptionWallet(operationType);
        }

        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return wallet.getBalance();
    }
}
