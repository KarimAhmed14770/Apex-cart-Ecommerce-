package com.Kee.V2C.dto.order;

import com.Kee.V2C.enums.OrderStatus;

public record SubOrderUpdateStatusRequest(OrderStatus newStatus) {
}
