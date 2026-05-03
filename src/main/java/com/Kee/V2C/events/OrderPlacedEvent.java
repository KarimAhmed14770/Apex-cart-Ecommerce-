package com.Kee.V2C.events;

import java.time.LocalDateTime;

public class OrderPlacedEvent {
    private final Long subOrderId;
    private final Long vendorId;
    private final LocalDateTime placedAt;

    public OrderPlacedEvent(Long subOrderId, Long vendorId, LocalDateTime placedAt) {
        this.subOrderId = subOrderId;
        this.vendorId = vendorId;
        this.placedAt = placedAt;
    }

    public Long getSubOrderId() {
        return subOrderId;
    }


    public Long getVendorId() {
        return vendorId;
    }


    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

}
