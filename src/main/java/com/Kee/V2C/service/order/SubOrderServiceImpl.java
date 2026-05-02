package com.Kee.V2C.service.order;

import com.Kee.V2C.Repository.SubOrderRepository;
import com.Kee.V2C.dto.order.OrderItemResponse;
import com.Kee.V2C.dto.order.SubOrderResponse;
import com.Kee.V2C.dto.order.SubOrderUpdateStatusRequest;
import com.Kee.V2C.entity.OrderItem;
import com.Kee.V2C.entity.SubOrder;
import com.Kee.V2C.enums.OrderStatus;
import com.Kee.V2C.exception.ResourceNotFoundException;
import com.Kee.V2C.exception.UserAccessDeniedException;
import com.Kee.V2C.utils.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubOrderServiceImpl implements SubOrderService {

    private final SubOrderRepository subOrderRepository;
    private final SecurityUtil securityUtil;
    public SubOrderServiceImpl(SubOrderRepository subOrderRepository,SecurityUtil securityUtil){
        this.subOrderRepository=subOrderRepository;
        this.securityUtil=securityUtil;
    }

    @Override
    public Page<SubOrderResponse> getVendorSubOrders(Pageable page){
        Page<SubOrder> subOrders=subOrderRepository.findByVendorId(securityUtil.getCurrentUserId(),page);
        return subOrders.map(this::convertSubOrderToDto);
    }

    @Override
    public Page<SubOrderResponse> getVendorSubOrdersWithFilters(OrderStatus status, Long suOrderId,
                                                                Pageable page){
        Page<SubOrder>subOrders=subOrderRepository.findByVendorIdWithFilters(securityUtil.getCurrentUserId(),
                status,suOrderId,page);
        return subOrders.map(this::convertSubOrderToDto);
    }


    @Override
    @Transactional
    public OrderStatus updateSubOrderState(SubOrderUpdateStatusRequest subOrderUpdateStatusRequest,
                                           Long subOrderId){
        SubOrder subOrder=subOrderRepository.findById(subOrderId).orElseThrow(
                ()->new ResourceNotFoundException("Sub order with id"+subOrderId+" doesn't exist")
        );
        if(!(subOrder.getVendor().getId()).equals(securityUtil.getCurrentUserId())){
            throw new UserAccessDeniedException("you have no order with id: "+subOrderId);
        }
        subOrder.transitionTo(subOrderUpdateStatusRequest.newStatus());
        subOrderRepository.save(subOrder);
        return subOrder.getStatus();
    }

    private SubOrderResponse convertSubOrderToDto(SubOrder subOrder){
        List<OrderItemResponse> orderItemResponses=new ArrayList<>();
        List<OrderItem> orderItemList=subOrder.getOrderItems();
        for(OrderItem orderItem: orderItemList){
            OrderItemResponse orderItemResponse=new OrderItemResponse(
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getName(),
                    orderItem.getProduct().getImageUrl(),
                    orderItem.getQuantity(),
                    orderItem.getPriceAtPurchase(),
                    orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            );
            orderItemResponses.add(orderItemResponse);
        }

        return new SubOrderResponse(subOrder.getId(),
                subOrder.getOrderedAt(),subOrder.getDelivered_at(),subOrder.getTotalPrice(),
                subOrder.getStatus(),orderItemResponses);
    }
}
