package ru.ivalykhin.wallet.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;
import java.util.UUID;

public interface MessageService {
    void sendMessage(ProducerRecord<String, Object> message);

    ProducerRecord<String, Object> createMessage(String topic,
                                                 String messageKey,
                                                 Object payload);

    void processMessage(Map<String, Object> messageHeaders, String messageBody);

    UUID getMessageId(ProducerRecord<String, Object> message);

    UUID getMessageId(Map<String, Object> messageHeaders);
}
