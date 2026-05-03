package com.Kee.V2C.service.checkout;


import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.events.OrderPlacedEvent;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.service.cart.CartService;
import com.Kee.V2C.service.order.OrderService;
import com.Kee.V2C.service.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService{
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    public CheckoutServiceImpl(OrderService orderService,PaymentService paymentService,
                               CartService cartService,ApplicationEventPublisher applicationEventPublisher){
        this.orderService=orderService;
        this.paymentService=paymentService;
        this.cartService=cartService;
        this.applicationEventPublisher=applicationEventPublisher;
    }

    @Override
    @Transactional //to roll back if any error occurs
    public CheckoutResponse checkOut(CheckOutRequest checkOutRequest){
        List<CartItem> cart=cartService.getCustomerCart(); //Retrieve: Fetch the Cart from the database using the userId.
        cartService.cartStockValidationAndUpdate(cart); //Validate: Check if every item in that cart is still in stock (The Atomic Shield).
        Order order=orderService.convertCartToOrder(checkOutRequest,cart);//Convert: Transform the Cart items into Order items and Order, persists order in the db
        List<SubOrder> subOrders=order.getSubOrders();
        //process payment
        if(paymentService.processPayment(checkOutRequest.paymentRequest(),order)) {//payment successful
            cartService.deleteCart(cart);
            //publish an event for each suborder placed
            for(SubOrder subOrder:subOrders){
                applicationEventPublisher.publishEvent(new OrderPlacedEvent(subOrder.getId(),subOrder.getVendor().getId(),
                        subOrder.getOrderedAt()));
            }
            return new CheckoutResponse(order.getId(), order.getTotalPrice(), order.getStatus().name(),  //Respond: Return an OrderResponse.
                    order.getOrderedAt(), order.getShippingAddress());
        }
        else{ throw new PaymentFailedException("Payment Failed");}
    }


}
