package com.Kee.V2C.service.order;

import com.Kee.V2C.dto.order.SubOrderResponse;
import com.Kee.V2C.dto.order.SubOrderUpdateStatusRequest;
import com.Kee.V2C.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface SubOrderService {
        Page<SubOrderResponse> getVendorSubOrders(Pageable page);
        Page<SubOrderResponse> getVendorSubOrdersWithFilters(OrderStatus status, Long suOrderId
                                                            , Pageable pageable);
        OrderStatus updateSubOrderState(SubOrderUpdateStatusRequest subOrderUpdateStatusRequest, Long subOrderId);
}
