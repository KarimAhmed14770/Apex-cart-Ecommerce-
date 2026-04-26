package com.Kee.V2C.service;

import com.Kee.V2C.dto.customer.CheckOutRequest;
import com.Kee.V2C.entity.CartItem;
import com.Kee.V2C.entity.Order;

import java.util.List;

public interface OrderService {
    Order convertCartToOrder(CheckOutRequest checkOutRequest, List<CartItem> cart);
}
