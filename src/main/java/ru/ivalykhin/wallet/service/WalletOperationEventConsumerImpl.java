package ru.ivalykhin.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.ivalykhin.wallet.dto.WalletOperationEvent;
import ru.ivalykhin.wallet.dto.WalletResponse;
import ru.ivalykhin.wallet.entity.Wallet;
import ru.ivalykhin.wallet.kafka.MessageService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletOperationEventConsumerImpl implements WalletOperationEventConsumer {
    private final MessageService messageService;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"${spring.kafka.wallet-operations-topic}"})
    public void consumeWalletOperationEvent(@Headers Map<String, Object> messageHeaders,
                                            @Payload String messageBody) {

        messageService.processMessage(messageHeaders, messageBody);
        UUID eventId = messageService.getMessageId(messageHeaders);
        try {
            WalletOperationEvent walletOperationEvent = objectMapper.readValue(messageBody, WalletOperationEvent.class);
            Wallet wallet = walletService.processOperation(
                    walletOperationEvent.getWalletId(),
                    walletOperationEvent.getOperationType(),
                    walletOperationEvent.getAmount());
            walletService.complete(eventId, new WalletResponse(wallet.getId(), wallet.getBalance()));
        } catch (JsonProcessingException e) {
            log.error("Failed to read WalletOperationEvent: {}", messageBody, e);
            if (eventId != null) walletService.completeWithError(eventId, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (eventId != null) walletService.completeWithError(eventId, (Exception) cause);
        } catch (Exception e) {
            log.error("Error processing wallet operation", e);
            if (eventId != null) walletService.completeWithError(eventId, e);
        }
    }
}
