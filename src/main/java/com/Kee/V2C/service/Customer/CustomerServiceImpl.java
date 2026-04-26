package com.Kee.V2C.service.Customer;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartItemResponse;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.dto.order.OrderItemResponse;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.UserRoles;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.mapper.CustomerMapper;
import com.Kee.V2C.service.Authentication.JwtService;
import com.Kee.V2C.service.OrderService;
import com.Kee.V2C.utils.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SecurityUtil securityUtil;
    private final CustomerMapper customerMapper;



    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, SecurityUtil securityUtil
                            ,CustomerMapper customerMapper){
        this.customerRepository = customerRepository;
        this.securityUtil=securityUtil;
        this.customerMapper=customerMapper;
    }


    @Override
    public CustomerProfileResponse myProfile(){
        Customer customer=getCurrentCustomer();
        return new CustomerProfileResponse(customer.getId(), customer.getFirstName(),customer.getLastName(),customer.getPhoneNumber()
                ,customer.getImageUrl(),customer.getShippingAddress(),customer.getCredential().getUserStatus().name());
    }

    @Override
    @Transactional
    public CustomerProfileResponse partialUpdateCustomerProfile(CustomerUpdateProfileRequest updateRequest){
        Customer customer=getCurrentCustomer();
        customerMapper.updateCustomerFromDto(updateRequest,customer);
        customerRepository.save(customer);
        return new CustomerProfileResponse(customer.getId(),
                customer.getFirstName(),customer.getLastName(),customer.getPhoneNumber(),customer.getImageUrl(),
                customer.getShippingAddress(),customer.getCredential().getUserStatus().name()
        );
    }

    /*Helper Methods*/

    private Customer getCurrentCustomer(){
        Long id=securityUtil.getCurrentUserId();
         return customerRepository.findByIdWithCredentials(id).
                orElseThrow(()->new UserNotFoundException("customer not found"));
    }

}
