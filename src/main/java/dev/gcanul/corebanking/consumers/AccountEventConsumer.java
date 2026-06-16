package dev.gcanul.corebanking.consumers;

import dev.gcanul.corebanking.events.AccountCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountEventConsumer {

    @KafkaListener(topics = "account-events", groupId = "account-group")
    public void consume(AccountCreatedEvent event) {
        log.info("✅ Evento recibido en el consumidor: {}", event);

        // Aquí podrías añadir lógica de negocio, por ejemplo:
        // - Enviar un email de bienvenida.
        // - Registrar en una tabla de auditoría.
        // - Actualizar un dashboard en tiempo real.
    }
}