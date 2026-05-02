package com.Kee.V2C.rest;


import com.Kee.V2C.dto.order.SubOrderResponse;
import com.Kee.V2C.dto.order.SubOrderUpdateStatusRequest;
import com.Kee.V2C.enums.OrderStatus;
import com.Kee.V2C.service.order.SubOrderService;
import com.Kee.V2C.service.order.SubOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sub-orders")
public class SubOrderController {

    private final SubOrderService subOrderService;

    @Autowired
    public SubOrderController(SubOrderService subOrderService){
        this.subOrderService=subOrderService;
    }

    @GetMapping
    public ResponseEntity<Page<SubOrderResponse>> getMyOrders(Pageable page){
        return ResponseEntity.ok(subOrderService.getVendorSubOrders(page));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<SubOrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long subOrderId,
            Pageable page){
        return ResponseEntity.ok(subOrderService.getVendorSubOrdersWithFilters(status,subOrderId,page));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderStatus> updateMyOrderStatus(@PathVariable("id")Long id,
                                                           @RequestBody SubOrderUpdateStatusRequest subOrderUpdateStatusRequest){
        return ResponseEntity.ok(subOrderService.updateSubOrderState(subOrderUpdateStatusRequest,id));
    }
}
