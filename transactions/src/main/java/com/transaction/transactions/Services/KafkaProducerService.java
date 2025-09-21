package com.transaction.transactions.Services;
import com.transaction.transactions.Event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${kafka.topic.transaction}")
    private String topicName;

    public void sendMessage(TransactionEvent transactionEvent) {
        kafkaTemplate.send(topicName, transactionEvent);
    }
}