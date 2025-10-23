package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.dto.commands.*;
import com.appsdeveloperblog.core.dto.events.*;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"${orders.events.topic.name}",
                         "${products.events.topic.name}",
                         "${payments.events.topic.name}"})
public class OrdersSagaOrchestrator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productsCommandTopicName;
    private final String paymentsCommandTopicName;
    private final String ordersCommandTopicName;
    private final OrderHistoryService orderHistoryService;

    public OrdersSagaOrchestrator(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${products.commands.topic.name}") String productsCommandTopicName,
                                  @Value("${payments.commands.topic.name}") String paymentsCommandTopicName,
                                  @Value("${orders.commands.topic.name}") String ordersCommandTopicName1,
                                  OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.productsCommandTopicName = productsCommandTopicName;
        this.paymentsCommandTopicName = paymentsCommandTopicName;
        this.ordersCommandTopicName = ordersCommandTopicName1;
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

    @KafkaHandler
    public void handlePaymentProcessedEvent(@Payload PaymentProcessedEvent paymentProcessedEvent) {
        ApprovedOrderCommand approvedOrderCommand = new ApprovedOrderCommand(paymentProcessedEvent.getOrderId());
        kafkaTemplate.send(ordersCommandTopicName, approvedOrderCommand);
    }

    @KafkaHandler
    public void handleOrderApprovedEvent(@Payload OrderApprovedEvent event) {
        orderHistoryService.add(event.getOrderId(), OrderStatus.APPROVED);
    }

    @KafkaHandler
    public void handlePaymentFailedEvent(@Payload PaymentFailedEvent paymentFailedEvent) {
        CancelProductReservationCommand cancelProductReservationCommand = new CancelProductReservationCommand(paymentFailedEvent.getProductId(), paymentFailedEvent.getOrderId(),
                paymentFailedEvent.getProductQuantity());
        kafkaTemplate.send(productsCommandTopicName, cancelProductReservationCommand);
    }

    @KafkaHandler
    public void handleCancelProductReservationEvent(@Payload ProductReservationCancelEvent event) {
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(event.getOrderId());
        kafkaTemplate.send(ordersCommandTopicName, rejectOrderCommand);
        orderHistoryService.add(event.getOrderId(), OrderStatus.REJECTED);
    }
}
