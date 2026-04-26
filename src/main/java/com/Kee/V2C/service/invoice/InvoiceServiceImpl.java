package com.Kee.V2C.service.invoice;

import com.Kee.V2C.Repository.CustomerRepository;
import com.Kee.V2C.Repository.OrderRepository;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.dto.order.OrderItemResponse;
import com.Kee.V2C.entity.Customer;
import com.Kee.V2C.entity.Order;
import com.Kee.V2C.entity.OrderItem;
import com.Kee.V2C.entity.SubOrder;
import com.Kee.V2C.exception.OrderNotFoundException;
import com.Kee.V2C.exception.UserAccessDeniedException;
import com.Kee.V2C.exception.UserNotFoundException;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService{

    private final SecurityUtil securityUtil;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public InvoiceServiceImpl(OrderRepository orderRepository,SecurityUtil securityUtil,
                              CustomerRepository customerRepository){
        this.securityUtil=securityUtil;
        this.orderRepository=orderRepository;
        this.customerRepository=customerRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse generateInvoice(long orderId){
        Customer customer=getCurrentCustomer();
        Order order=orderRepository.findByIdWithSubOrders(orderId).orElseThrow(
                ()->new OrderNotFoundException("you don't have an order with id: "+orderId)
        );
        if(!(order.getCustomer().equals(customer))){
            throw new UserAccessDeniedException("you can't access this order");
        }
        List<SubOrder> subOrders=new ArrayList<>();
        subOrders=order.getSubOrders();
        List<List<OrderItem>> orderItemsLists=subOrders.stream().map(SubOrder::getOrderItems).toList();
        List<OrderItemResponse> orderItemsResponse=new ArrayList<>();
        for(List<OrderItem> orderItemList:orderItemsLists){
            for(OrderItem orderItem:orderItemList){
                OrderItemResponse response=new OrderItemResponse(
                        orderItem.getProduct().getId(),
                        orderItem.getProduct().getName(),
                        orderItem.getProduct().getProductModel().getImageUrl(),
                        orderItem.getQuantity(),
                        orderItem.getPriceAtPurchase(),
                        orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
                orderItemsResponse.add(response);
            }
        }
        return new InvoiceResponse(
                order.getId(),
                order.getOrderedAt(),
                orderItemsResponse,
                order.getTotalPrice()
        );
    }


    private Customer getCurrentCustomer(){
        Long id=securityUtil.getCurrentUserId();
        return customerRepository.findByIdWithCredentials(id).
                orElseThrow(()->new UserNotFoundException("customer not found"));
    }
}
