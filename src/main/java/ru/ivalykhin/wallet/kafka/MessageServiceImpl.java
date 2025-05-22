package ru.ivalykhin.wallet.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    @Value(value = "${spring.kafka.enabled}")
    private boolean kafkaEnabled;

    @Value(value = "${spring.kafka.send-timeout-ms}")
    private Long sendTimeoutMs;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(ProducerRecord<String, Object> message) {
        if (!kafkaEnabled) {
            return;
        }
        try {
            var sendResult = kafkaTemplate.send(message).get(sendTimeoutMs, TimeUnit.MILLISECONDS);
            log.info("Sent message message_id={} with partition={}, offset={}",
                    getMessageHeader(message, "message_id"),
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset());

        } catch (ExecutionException | TimeoutException | InterruptedException ex) {
            log.error("Unable to send message message_id={} due to : {}",
                    getMessageHeader(message, "message_id"),
                    ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public ProducerRecord<String, Object> createMessage(String topic,
                                                        String messageKey,
                                                        Object payload) {
        var record = new ProducerRecord<>(topic, messageKey, payload);
        record.headers().add("message_id", UUID.randomUUID().toString().getBytes(UTF_8));
        record.headers().add("created_at", OffsetDateTime.now(ZoneOffset.UTC).toString().getBytes(UTF_8));
        return record;
    }

    private String getMessageHeader(ProducerRecord<String, Object> message, String key) {
        return new String(message.headers().lastHeader(key).value(), UTF_8);
    }

    public UUID getMessageId(ProducerRecord<String, Object> message) {
        return UUID.fromString(
                new String(message.headers().lastHeader("message_id").value(), UTF_8)
        );
    }

    public UUID getMessageId(Map<String, Object> messageHeaders) {
        var v = messageHeaders.get("message_id");
        if (v instanceof byte[]) {
            return UUID.fromString(new String((byte[]) v, UTF_8));
        }
        return null;
    }

    public void processMessage(Map<String, Object> messageHeaders, String messageBody) {
        String messageId = messageHeaders.get("message_id").toString();
        log.info("Message processing topic={}, partition={}, offset={}, message_id={}",
                messageHeaders.get(KafkaHeaders.RECEIVED_TOPIC),
                messageHeaders.get(KafkaHeaders.RECEIVED_PARTITION),
                messageHeaders.get(KafkaHeaders.OFFSET),
                messageId);
        log.debug("Message processing {}: {}", messageId, messageBody);
    }
}
