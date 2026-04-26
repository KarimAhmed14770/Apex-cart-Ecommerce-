package com.Kee.V2C.rest;

import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.service.Customer.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {
    private final CustomerService customerService;

    @Autowired
    public CustomerRestController(CustomerService customerService){
        this.customerService = customerService;
    }

    @GetMapping("/my-profile")
    public ResponseEntity<CustomerProfileResponse> myProfile(){
        return ResponseEntity.status(HttpStatus.OK).body(customerService.myProfile());
    }
    @PatchMapping("/my-profile")
    public ResponseEntity<CustomerProfileResponse> myProfileUpdate(@RequestBody @Valid CustomerUpdateProfileRequest updateRequest){
        return ResponseEntity.status(HttpStatus.OK).body(customerService.partialUpdateCustomerProfile(updateRequest));
    }

}
