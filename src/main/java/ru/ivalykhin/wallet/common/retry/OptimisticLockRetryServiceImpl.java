package ru.ivalykhin.wallet.common.retry;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.ivalykhin.wallet.config.OptimisticLockRetryProperties;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockRetryServiceImpl implements OptimisticLockRetryService {
    private final OptimisticLockRetryProperties optimisticLockRetryProperties;
    private final ThreadLocal<AtomicInteger> count =
            ThreadLocal.withInitial(() -> new AtomicInteger(0));


    @Override
    @Retryable(
            retryFor = {OptimisticLockException.class, StaleObjectStateException.class},
            maxAttemptsExpression = "#{@optimisticLockRetryProperties.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@optimisticLockRetryProperties.delay}",
                    multiplierExpression = "#{@optimisticLockRetryProperties.multiplier}"
            )
    )
    public <T> T runWithRetry(RetryableOperation<T> operation) throws Exception {
        try {
            return operation.execute();
        } catch (Exception ex) {
            log.info("Retry attempt: {}", count.get().incrementAndGet());
            throw ex;
        } finally {
            count.remove();
        }

    }
}
