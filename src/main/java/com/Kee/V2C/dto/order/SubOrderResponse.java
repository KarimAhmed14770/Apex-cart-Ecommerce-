package com.Kee.V2C.dto.order;

import com.Kee.V2C.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SubOrderResponse(Long id,
                               LocalDateTime orderedAt,
                               LocalDateTime deliveredAt,
                               BigDecimal price,
                               OrderStatus status,
                               List<OrderItemResponse> orderItems) {
}
