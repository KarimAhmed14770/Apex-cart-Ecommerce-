package com.Kee.V2C.service.cart;

import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;

public interface CartService {
    CartResponse addToCart(CartItemRequest cartItemRequest);
    CartResponse viewMyCart();
}
