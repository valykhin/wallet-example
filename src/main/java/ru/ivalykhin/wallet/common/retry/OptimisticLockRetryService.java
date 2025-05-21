package ru.ivalykhin.wallet.common.retry;

public interface OptimisticLockRetryService {
    <T> T runWithRetry(RetryableOperation<T> operation);
}
