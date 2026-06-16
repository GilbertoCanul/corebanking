package dev.gcanul.corebanking.producers;

import dev.gcanul.corebanking.events.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "account-events";

    public void sendAccountCreatedEvent(AccountCreatedEvent event) {
        log.info("Sending account created event for account: {}", event.accountNumber());
        kafkaTemplate.send(TOPIC, event.accountId().toString(), event);
    }
}