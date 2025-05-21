package ru.ivalykhin.wallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wallet.retry.optimistic-lock")
@Data
public class OptimisticLockRetryProperties {
    private int maxAttempts = 3;
    private long delay = 50; // ms
    private double multiplier = 1.5;
}