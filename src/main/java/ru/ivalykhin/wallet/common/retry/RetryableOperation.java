package ru.ivalykhin.wallet.common.retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T execute() throws Exception;
}
