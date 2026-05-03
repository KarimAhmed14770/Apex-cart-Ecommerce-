package com.Kee.V2C.rest;

import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.service.Customer.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {
    private final CustomerService customerService;

    @Autowired
    public CustomerRestController(CustomerService customerService){
        this.customerService = customerService;
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers")
    public ResponseEntity<Page<CustomerProfileResponse>> getAllCustomers(Pageable page){
        return ResponseEntity.ok(customerService.getAllCustomers(page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerProfileResponse> getCustomerById(@PathVariable("id") Long id){
        return ResponseEntity.ok(customerService.getCustomerProfileById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/search")
    public ResponseEntity<Page<CustomerProfileResponse>> getCustomerByAttribute
            (@RequestParam(required = false)String firstName,
             @RequestParam(required = false)String lastName,
             @RequestParam(required = false)String userName,
             @RequestParam(required = false)String email,
             @RequestParam(required = false)String address,
             @RequestParam(required = false) UserStatus status,
             Pageable page,
             @SortDefault(sort="firstName",direction = Sort.Direction.ASC)Sort sort){
        return ResponseEntity.ok(customerService.searchForCustomer(userName,email,firstName,lastName,
                address,status,page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("customers/modify-status/{id}")
    public CustomerProfileResponse modifyCustomerStatus(@PathVariable("id") Long id,
                                                        @RequestBody @Valid StatusUpdateDto status){
        return customerService.modifyCustomerStatus(id,status);
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
