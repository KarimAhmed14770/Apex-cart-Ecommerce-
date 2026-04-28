package com.Kee.V2C.service.cart;

import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.entity.CartItem;

import java.util.List;

public interface CartService {
    CartResponse addToCart(CartItemRequest cartItemRequest);
    CartResponse viewMyCart();
    List<CartItem> getCustomerCart();
    void cartStockValidationAndUpdate(List<CartItem> cart);
}
