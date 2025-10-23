package com.appsdeveloperblog.payments.handler;

import com.appsdeveloperblog.core.dto.Payment;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.exceptions.CreditCardProcessorUnavailableException;
import com.appsdeveloperblog.payments.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${payments.commands.topic.name}")
public class PaymentsCommandsHandler {

    private final PaymentService paymentService;
    private final String paymentEventsTopicName;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Logger log = LoggerFactory.getLogger(PaymentsCommandsHandler.class);

    public PaymentsCommandsHandler(PaymentService paymentService,
                                   @Value("${payments.events.topic.name}") String paymentEventsTopicName,
                                   KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentService = paymentService;
        this.paymentEventsTopicName = paymentEventsTopicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaHandler
    public void handleProcessPaymentCommand(@Payload ProcessPaymentCommand command) {
        try {
            Payment payment = new Payment(command.getOrderId(), command.getProductId(),
                    command.getProductPrice(), command.getProductQuantity());
            Payment processedPayment = paymentService.process(payment);
            PaymentProcessedEvent paymentProcessedEvent =
                    new PaymentProcessedEvent(processedPayment.getOrderId(), processedPayment.getId());
            kafkaTemplate.send(paymentEventsTopicName, paymentProcessedEvent);
        } catch (CreditCardProcessorUnavailableException e) {
            log.error("Error while processing Payment Command", e);
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(command.getOrderId(),
                    command.getProductId(), command.getProductQuantity());
            kafkaTemplate.send(paymentEventsTopicName, paymentFailedEvent);
        }
    }
}
