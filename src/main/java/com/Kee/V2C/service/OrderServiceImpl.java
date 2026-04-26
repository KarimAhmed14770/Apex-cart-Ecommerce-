package com.Kee.V2C.service;

import com.Kee.V2C.Repository.CustomerRepository;
import com.Kee.V2C.Repository.OrderRepository;
import com.Kee.V2C.Repository.VendorRepository;
import com.Kee.V2C.dto.customer.CheckOutRequest;
import com.Kee.V2C.dto.order.OrderResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.OrderStatus;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.specifications.OrderSpecs;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final SecurityUtil securityUtil;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(VendorRepository vendorRepository,CustomerRepository customerRepository,
                            SecurityUtil securityUtil,OrderRepository orderRepository){
        this.vendorRepository=vendorRepository;
            this.customerRepository=customerRepository;
            this.securityUtil=securityUtil;
            this.orderRepository=orderRepository;
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

    @Override
    public OrderResponse getOrderById(Long id){
        Long customerId=securityUtil.getCurrentUserId();
        Order order=orderRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("order with id: "+id+" not found"));
        if(!order.getCustomer().getId().equals(customerId)){
            throw new ResourceNotFoundException("you don't have order with id: "+id);
        }
        return convertOrderToDto(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable page){
        Long customerId=securityUtil.getCurrentUserId();
        Page<Order> orders=orderRepository.findByCustomerId(customerId,page);
        return orders.map(this::convertOrderToDto);
    }

    @Override
    public Page<OrderResponse> getOrderByAttributes(Pageable page, BigDecimal lowerRange, BigDecimal higherRange,
                                     String address, OrderStatus status){
        Specification<Order> spec = (root, query, cb) -> cb.conjunction();
        Long customerId=securityUtil.getCurrentUserId();
        if(customerId!=null) spec=spec.and(OrderSpecs.hasCustomerId(customerId));

        if(lowerRange!=null || higherRange!=null) spec=spec.and(OrderSpecs.hasPriceBetween(lowerRange, higherRange));

        if(address!=null && !address.isEmpty()) spec=spec.and(OrderSpecs.hasAddressContains(address));

        if(status!=null) spec=spec.and(OrderSpecs.hasStatus(status));

        Page<Order> orders=orderRepository.findAll(spec,page);
        return orders.map(this::convertOrderToDto);
    }

    private OrderResponse convertOrderToDto(Order order){
        return new OrderResponse(order.getId(),
                order.getShippingAddress(),
                order.getOrderedAt(),
                order.getDelivered_at(),
                order.getTotalPrice(),
                order.getStatus());
    }
}
