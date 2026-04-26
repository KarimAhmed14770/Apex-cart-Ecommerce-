package com.Kee.V2C.service.order;

import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.order.OrderResponse;
import com.Kee.V2C.entity.CartItem;
import com.Kee.V2C.entity.Order;
import com.Kee.V2C.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Order convertCartToOrder(CheckOutRequest checkOutRequest, List<CartItem> cart);
    OrderResponse getOrderById(Long id);
    Page<OrderResponse> getAllOrders(Pageable page);
    Page<OrderResponse> getOrderByAttributes(Pageable page, BigDecimal lowerRange, BigDecimal higherRange,
                                     String address, OrderStatus status);
}
