package com.appsdeveloperblog.core.dto.events;

import java.util.UUID;

public class ProductReservationFailedEvent {

    private UUID productId;
    private UUID orderId;
    private Integer productQuantity;

    public ProductReservationFailedEvent() {

    }

    public ProductReservationFailedEvent(UUID productId, UUID orderId, Integer productQuantity) {
        this.productId = productId;
        this.orderId = orderId;
        this.productQuantity = productQuantity;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
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

    @Override
    public String toString() {
        return "ProductReservationFailedEvent{" +
                "productId=" + productId +
                ", orderId=" + orderId +
                ", productQuantity=" + productQuantity +
                '}';
    }
}
