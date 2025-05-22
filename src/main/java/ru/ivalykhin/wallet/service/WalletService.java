package ru.ivalykhin.wallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivalykhin.wallet.common.retry.OptimisticLockRetryService;
import ru.ivalykhin.wallet.dto.WalletOperationEvent;
import ru.ivalykhin.wallet.dto.WalletResponse;
import ru.ivalykhin.wallet.entity.OperationType;
import ru.ivalykhin.wallet.entity.Wallet;
import ru.ivalykhin.wallet.exception.InsufficientFundsExceptionWallet;
import ru.ivalykhin.wallet.exception.InvalidOperationExceptionWallet;
import ru.ivalykhin.wallet.exception.WalletNotFoundException;
import ru.ivalykhin.wallet.repository.WalletRepository;

import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final OptimisticLockRetryService optimisticLockRetryService;
    private final WalletOperationEventProducer walletOperationEventProducer;
    private final ConcurrentMap<UUID, CompletableFuture<WalletResponse>> futures = new ConcurrentHashMap<>();
    @Value(value = "${wallet.operation.request-timeout}")
    private int walletOperationRequestTimeout;


    public WalletResponse publishOperation(UUID walletId, OperationType operationType, Long amount)
            throws ExecutionException{
        WalletOperationEvent walletOperationEvent = WalletOperationEvent.builder()
                .walletId(walletId)
                .operationType(operationType)
                .amount(amount)
                .build();

        CompletableFuture<WalletResponse> future = new CompletableFuture<>();
        UUID eventId = walletOperationEventProducer.sendWalletOperationEvent(walletOperationEvent);
        futures.put(eventId, future);

        try {
            return future.get(walletOperationRequestTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException ex) {
            throw new RuntimeException(ex);
        } finally {
            futures.remove(eventId);
        }
    }

    public void complete(UUID eventId, WalletResponse response) {
        CompletableFuture<WalletResponse> future = futures.remove(eventId);
        if (future != null) future.complete(response);
    }

    public void completeWithError(UUID eventId, Exception exception) {
        CompletableFuture<WalletResponse> future = futures.remove(eventId);
        if (future != null) future.completeExceptionally(exception);
    }

    public Wallet processOperation(UUID walletId, OperationType operationType, Long amount) throws Exception {
        return optimisticLockRetryService.runWithRetry(
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
