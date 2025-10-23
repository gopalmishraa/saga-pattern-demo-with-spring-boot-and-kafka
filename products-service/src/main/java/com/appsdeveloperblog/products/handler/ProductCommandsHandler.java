package com.appsdeveloperblog.products.handler;

import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.ProductReservationCancelEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${products.commands.topic.name}")
public class ProductCommandsHandler {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productEventsTopicName;

    private static final Logger log = LoggerFactory.getLogger(ProductCommandsHandler.class);

    public ProductCommandsHandler(ProductService productService, KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${products.events.topic.name}") String productEventsTopicName) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.productEventsTopicName = productEventsTopicName;
    }

    @KafkaHandler
    public void handleReserveProductCommand(@Payload ReserveProductCommand command) {
        try {
            Product newProduct = new Product(command.getProductId(), command.getProductQuantity());
            Product reservedProduct = productService.reserve(newProduct, command.getOrderId());
            ProductReservedEvent productReservedEvent = new ProductReservedEvent(command.getOrderId(),
                    command.getProductId(), reservedProduct.getPrice(), command.getProductQuantity());

            kafkaTemplate.send(productEventsTopicName, productReservedEvent);
        } catch (Exception ex){
            log.error("Error while processing reserve command", ex);
            ProductReservationFailedEvent productReservationFailedEvent = new ProductReservationFailedEvent(
                    command.getProductId(), command.getOrderId(), command.getProductQuantity());
            kafkaTemplate.send(productEventsTopicName, productReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handleCancelProductReservationCommand(@Payload CancelProductReservationCommand command) {
        Product product = new Product(command.getProductId(), command.getProductQuantity());
        productService.cancelReservation(product, command.getOrderId());
        ProductReservationCancelEvent cancelEvent = new ProductReservationCancelEvent(command.getProductId(), command.getOrderId());
        kafkaTemplate.send(productEventsTopicName, cancelEvent);
    }
}
