package com.Kee.V2C.rest;


import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.service.cart.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService){
        this.cartService=cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(@RequestBody @Valid CartItemRequest cartItemRequest){
        return ResponseEntity.status(HttpStatus.OK).body(cartService.addToCart(cartItemRequest));
    }

    @GetMapping
    public ResponseEntity<CartResponse> viewMyCart(){
        return ResponseEntity.status(HttpStatus.OK).body(cartService.viewMyCart());
    }
}
