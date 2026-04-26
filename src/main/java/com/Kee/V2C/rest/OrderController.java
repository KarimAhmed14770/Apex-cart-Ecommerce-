package com.Kee.V2C.rest;

import com.Kee.V2C.dto.customer.OrderResponse;
import com.Kee.V2C.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable page){
        return ResponseEntity.ok(orderService.getAllOrders(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
