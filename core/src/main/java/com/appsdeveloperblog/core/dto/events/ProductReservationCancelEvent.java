package com.appsdeveloperblog.core.dto.events;

import java.util.UUID;

public class ProductReservationCancelEvent {

    private UUID productId;
    private UUID orderId;

    public ProductReservationCancelEvent() {}

    public ProductReservationCancelEvent(UUID productId, UUID orderId) {
        this.productId = productId;
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
