package com.Kee.V2C.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        long orderId,//order id
        LocalDateTime generatedAt,
        List<OrderItemResponse> orderItems,
        BigDecimal totalPrice
) {
}
