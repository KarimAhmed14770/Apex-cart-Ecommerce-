package com.Kee.V2C.dto.order;

import com.Kee.V2C.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(Long id,
                            String shippingAddress,
                            LocalDateTime orderedAt,
                            LocalDateTime deliveredAt,
                            BigDecimal price,
                            OrderStatus status) {
}
