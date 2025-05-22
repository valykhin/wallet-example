package ru.ivalykhin.wallet.common.retry;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class OptimisticLockRetryServiceTest {
    @Autowired
    private OptimisticLockRetryServiceImpl retryService;

    @Test
    void runWithRetry_FirstAttempt_success() throws Exception {
        RetryableOperation<String> op = () -> "Success";
        String result = retryService.runWithRetry(op);
        Assertions.assertEquals("Success", result);
    }

    @Test
    void runWithRetry_SecondAttempt_success() throws Exception {
        AtomicInteger attempt = new AtomicInteger(0);

        RetryableOperation<String> op = () -> {
            if (attempt.getAndIncrement() < 1) {
                throw new OptimisticLockException();
            }
            return "Retried Success";
        };

        String result = retryService.runWithRetry(op);
        Assertions.assertEquals("Retried Success", result);
        Assertions.assertEquals(2, attempt.get());
    }

    @Test
    void runWithRetry_MaxAttemptsExceeded_throwException() {
        RetryableOperation<String> op = () -> {
            throw new OptimisticLockException();
        };

        Assertions.assertThrows(OptimisticLockException.class, () -> retryService.runWithRetry(op));
    }
}
