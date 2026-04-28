package com.Kee.V2C.service.checkout;

import com.Kee.V2C.Repository.CartItemRepository;

import com.Kee.V2C.Repository.OrderRepository;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.service.cart.CartService;
import com.Kee.V2C.service.order.OrderService;
import com.Kee.V2C.service.payment.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService{
    private final OrderService orderService;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final CartService cartService;


    public CheckoutServiceImpl(OrderService orderService,CartItemRepository cartItemRepository,
                               OrderRepository orderRepository, PaymentService paymentService,
                               CartService cartService){
        this.orderService=orderService;
        this.cartItemRepository=cartItemRepository;
        this.orderRepository=orderRepository;
        this.paymentService=paymentService;
        this.cartService=cartService;
    }


    @Override
    @Transactional //to roll back if anything occurs
    public CheckoutResponse checkOut(CheckOutRequest checkOutRequest){
        //Retrieve: Fetch the Cart from the database using the userId.
        List<CartItem> cart=cartService.getCustomerCart();
        //Validate: Check if every item in that cart is still in stock (The Atomic Shield).
        cartService.cartStockValidationAndUpdate(cart);
        //Convert: Transform the Cart items into Order items and Order
        Order order=orderService.convertCartToOrder(checkOutRequest,cart);
        //persist the order in the db
        orderRepository.save(order);
        //process payment
        if(paymentService.processPayment(checkOutRequest.paymentRequest(),order)) {
            //payment successful


            //notify each vendor with his subOrder

            //empty the cart of the user on the db , this is better than deleting 1 by 1 in loop
            cartItemRepository.deleteAllInBatch(cart);

            //Respond: Return an OrderResponse.
            return new CheckoutResponse(order.getId(), order.getTotalPrice(), order.getStatus().name(),
                    order.getOrderedAt(), order.getShippingAddress());
        }
        else
        {
            throw new PaymentFailedException("Payment Failed");
        }
    }


}
