package ru.ivalykhin.wallet.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ivalykhin.wallet.dto.WalletOperationEvent;
import ru.ivalykhin.wallet.kafka.MessageService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletOperationEventProducerImpl implements WalletOperationEventProducer {
    private final MessageService messageService;

    @Value(value = "${spring.kafka.wallet-operations-topic}")
    private String walletOperationsTopicName;

    public UUID sendWalletOperationEvent(WalletOperationEvent walletOperationEvent) {
        UUID messageKey = walletOperationEvent.getWalletId();
        ProducerRecord<String, Object> message =
                messageService.createMessage(
                        walletOperationsTopicName,
                        messageKey.toString(),
                        walletOperationEvent);
        messageService.sendMessage(message);
        return messageService.getMessageId(message);
    }
}
