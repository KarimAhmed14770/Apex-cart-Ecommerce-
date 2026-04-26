package com.Kee.V2C.service.checkout;

import com.Kee.V2C.Repository.CartItemRepository;
import com.Kee.V2C.Repository.CustomerRepository;
import com.Kee.V2C.Repository.OrderRepository;
import com.Kee.V2C.Repository.StockRepository;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.dto.order.OrderItemResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.service.OrderService;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService{
    private final OrderService orderService;
    private final StockRepository stockRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtil securityUtil;
    private final CustomerRepository customerRepository;

    public CheckoutServiceImpl(OrderService orderService,StockRepository stockRepository,
                               CartItemRepository cartItemRepository,OrderRepository orderRepository,
                               SecurityUtil securityUtil,CustomerRepository customerRepository){
        this.orderService=orderService;
        this.stockRepository=stockRepository;
        this.cartItemRepository=cartItemRepository;
        this.orderRepository=orderRepository;
        this.securityUtil=securityUtil;
        this.customerRepository=customerRepository;
    }


    @Override
    @Transactional //to roll back if anything occurs
    public CheckoutResponse checkOut(CheckOutRequest checkOutRequest){
        //Retrieve: Fetch the Cart from the database using the userId.
        List<CartItem> cart=getCustomerCart();
        //Validate: Check if every item in that cart is still in stock (The Atomic Shield).
        cartStockValidationAndUpdate(cart);
        //Convert: Transform the Cart items into Order items and Order
        Order order=orderService.convertCartToOrder(checkOutRequest,cart);
        orderRepository.save(order);

        //empty the cart of the user on the db , this is better than deleting 1 by 1 in loop
        cartItemRepository.deleteAllInBatch(cart);

        //Respond: Return an OrderResponse.
        return new CheckoutResponse(order.getId(),order.getTotalPrice(),order.getStatus().name(),
                order.getOrderedAt(),order.getShippingAddress());
    }



    private List<CartItem> getCustomerCart(){
        List<CartItem> cart=cartItemRepository.findByCustomerIdWithDetails(getCurrentCustomer().getId());
        if(cart.isEmpty()){
            throw new CartEmptyException("your shopping cart is currently empty");
        }
        return cart;
    }

    private void cartStockValidationAndUpdate(List<CartItem> cart){
        int rowsUpdated=0;
        for(CartItem cartItem:cart){
            rowsUpdated= stockRepository.decrementProductStock(cartItem.getQuantity(),
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getVendor().getShop().getId());
            if(rowsUpdated==0){
                throw new InsufficientStockException("Product with id: "+cartItem.getProduct().getId()+
                        " is out of stock");
            }
        }
    }

    private Customer getCurrentCustomer(){
        Long id=securityUtil.getCurrentUserId();
        return customerRepository.findByIdWithCredentials(id).
                orElseThrow(()->new UserNotFoundException("customer not found"));
    }

}
