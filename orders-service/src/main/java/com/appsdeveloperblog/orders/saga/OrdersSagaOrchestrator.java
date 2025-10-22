package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"${orders.events.topic.name}", "${products.events.topic.name}"})
public class OrdersSagaOrchestrator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productsCommandTopicName;
    private final String paymentsCommandTopicName;
    private final OrderHistoryService orderHistoryService;

    public OrdersSagaOrchestrator(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${products.commands.topic.name}") String productsCommandTopicName,
                                  @Value("${payments.commands.topic.name}") String paymentsCommandTopicName,
                                  OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.productsCommandTopicName = productsCommandTopicName;
        this.paymentsCommandTopicName = paymentsCommandTopicName;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleOrderCreatedEvent(@Payload OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = new ReserveProductCommand(
                orderCreatedEvent.getProductId(), orderCreatedEvent.getOrderId(), orderCreatedEvent.getQuantity());

        kafkaTemplate.send(productsCommandTopicName, reserveProductCommand);
        orderHistoryService.add(orderCreatedEvent.getOrderId(), OrderStatus.CREATED);
    }

    @KafkaHandler
    public void handleProductReservedEvent(@Payload ProductReservedEvent productReservedEvent) {
        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(productReservedEvent.getOrderId(),
                productReservedEvent.getProductId(), productReservedEvent.getProductPrice(), productReservedEvent.getProductQuantity());
        kafkaTemplate.send(paymentsCommandTopicName, processPaymentCommand);
    }
}
