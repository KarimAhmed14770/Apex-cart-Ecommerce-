package com.Kee.V2C.service;

import com.Kee.V2C.Repository.CustomerRepository;
import com.Kee.V2C.Repository.VendorRepository;
import com.Kee.V2C.dto.customer.CheckOutRequest;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.OrderStatus;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.service.Customer.CustomerService;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public OrderServiceImpl(VendorRepository vendorRepository,CustomerRepository customerRepository,SecurityUtil securityUtil){
        this.vendorRepository=vendorRepository;
            this.customerRepository=customerRepository;
            this.securityUtil=securityUtil;
    }

    @Override
    @Transactional
    public Order convertCartToOrder(CheckOutRequest checkOutRequest, List<CartItem> cart){
        HashMap<Long, SubOrder> map=new HashMap<>();
        Long vendorId=null;
        SubOrder subOrder=null;
        Vendor vendor=null;
        OrderItem orderItem=null;
        for(CartItem cartItem:cart){
            vendorId=cartItem.getProduct().getVendor().getId();

            if(map.containsKey(vendorId)){
                subOrder=map.get(vendorId);
                subOrder.addOrderItem(new OrderItem(cartItem.getProduct(),cartItem.getQuantity(),
                        cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))));
            }else {
                map.put(vendorId,new SubOrder());
                subOrder=map.get(vendorId);
                vendor=vendorRepository.findById(vendorId)
                        .orElseThrow(()->new ResourceNotFoundException("vendor not found"));
                subOrder.setVendor(vendor);
                vendor.addSubOrder(subOrder);
                subOrder.addOrderItem(new OrderItem(cartItem.getProduct(),cartItem.getQuantity(),
                        cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))));
                subOrder.setStatus(OrderStatus.PENDING);
            }
            subOrder.setTotalPrice(subOrder.getTotalPrice().add(cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()))));

        }
        Order order=new Order(checkOutRequest.shippingAddress(), OrderStatus.PENDING);//order first status is pending
        Customer customer=customerRepository.findById(securityUtil.getCurrentUserId()).
                orElseThrow(()->new ResourceNotFoundException("customer not found"));
        order.setCustomer(customer);
        map.forEach((id,subOrder1)->{
            order.addSubOrder(subOrder1);
            order.setTotalPrice(order.getTotalPrice().add(subOrder1.getTotalPrice()));
        });
        if(!checkOutRequest.deliverToRegisteredAddress()){
            order.setShippingAddress(checkOutRequest.shippingAddress());
        }
        else{
            order.setShippingAddress(customer.getShippingAddress());
        }
        return order;
    }
}
