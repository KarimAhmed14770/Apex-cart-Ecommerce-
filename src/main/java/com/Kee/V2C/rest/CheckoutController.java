package com.Kee.V2C.rest;


import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.service.checkout.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Autowired
    public CheckoutController(CheckoutService checkoutService){
        this.checkoutService=checkoutService;
    }

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody @Valid CheckOutRequest checkOutRequest){
        return ResponseEntity.status(HttpStatus.OK).body(checkoutService.checkOut(checkOutRequest));
    }

}
