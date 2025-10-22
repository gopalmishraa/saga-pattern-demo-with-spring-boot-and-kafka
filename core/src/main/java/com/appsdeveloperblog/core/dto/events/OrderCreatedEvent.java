package com.appsdeveloperblog.core.dto.events;

import java.util.UUID;

public class OrderCreatedEvent {

    private UUID orderId;
    private UUID productId;
    private UUID customerId;
    private Integer quantity;

    public OrderCreatedEvent() {

    }

    public OrderCreatedEvent(UUID orderId, UUID productId, UUID customerId, Integer quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.quantity = quantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                ", productId=" + productId +
                ", customerId=" + customerId +
                ", quantity=" + quantity +
                '}';
    }
}
