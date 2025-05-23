package ru.ivalykhin.wallet.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.ivalykhin.wallet.dto.WalletOperationEvent;
import ru.ivalykhin.wallet.kafka.MessageService;

import java.util.Map;
import java.util.UUID;

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
            walletService.processOperation(
                    walletOperationEvent.getWalletId(),
                    walletOperationEvent.getOperationType(),
                    walletOperationEvent.getAmount());
        } catch (JsonMappingException e) {
            log.error("Message {} can't be parsed", eventId, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
