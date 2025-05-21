package ru.ivalykhin.wallet.common.retry;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hibernate.StaleObjectStateException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.ivalykhin.wallet.config.OptimisticLockRetryProperties;

@Service
@RequiredArgsConstructor
public class OptimisticLockRetryServiceImpl implements OptimisticLockRetryService {
    private final OptimisticLockRetryProperties optimisticLockRetryProperties;

    @Override
    @Retryable(
            retryFor = {OptimisticLockException.class, StaleObjectStateException.class},
            maxAttemptsExpression = "#{@optimisticLockRetryProperties.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@optimisticLockRetryProperties.delay}",
                    multiplierExpression = "#{@optimisticLockRetryProperties.multiplier}"
            )
    )
    @SneakyThrows
    public <T> T runWithRetry(RetryableOperation<T> operation) {
        return operation.execute();
    }
}
