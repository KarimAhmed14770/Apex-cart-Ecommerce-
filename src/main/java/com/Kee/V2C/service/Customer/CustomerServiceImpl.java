package com.Kee.V2C.service.Customer;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.enums.UserStatus;
import com.Kee.V2C.exception.*;
import com.Kee.V2C.mapper.CustomerMapper;
import com.Kee.V2C.specifications.CustomerSpecs;
import com.Kee.V2C.utils.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<CustomerProfileResponse> getAllCustomers(Pageable page){
        Specification<Customer> spec=Specification.where(CustomerSpecs.fetchCredential());
        return customerRepository.findAll(spec,page).map(this::convertCustomerToDto);

    }

    @Override
    public CustomerProfileResponse getCustomerProfileById(Long id){
        Customer customer= customerRepository.findByIdWithCredentials(id)
                .orElseThrow(()->new UserNotFoundException("user with id: "+ id +" not  found"));
        return convertCustomerToDto(customer);
    }


    @Override
    public Page<CustomerProfileResponse> searchForCustomer(String userName, String email, String firstName,
                                                           String lastName, String shippingAddress,
                                                           UserStatus status, Pageable pageable){
        // 1. Start with an empty "where true" (conjunction) to avoid null issues
        Specification<Customer> spec = (root, query, cb) -> cb.conjunction();        if(userName!=null) spec=spec.and(CustomerSpecs.hasUserName(userName));

        if(email!=null) spec=spec.and(CustomerSpecs.hasEmail(email));

        if(firstName!=null) spec=spec.and(CustomerSpecs.hasFirstName(firstName));

        if(lastName!=null) spec=spec.and(CustomerSpecs.hasLastName(lastName));

        if(shippingAddress!=null) spec=spec.and(CustomerSpecs.hasAddress(shippingAddress));

        if(status!=null) spec=spec.and(CustomerSpecs.hasStatus(status));
        spec=spec.and(CustomerSpecs.fetchCredential());

        Page<Customer> customers=customerRepository.findAll(spec,pageable);

        return customers.map(this::convertCustomerToDto);
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

    @Override
    @Transactional
    public CustomerProfileResponse  modifyCustomerStatus(Long id, StatusUpdateDto status){
        Customer customer=getCustomerById(id);

        customer.getCredential().setUserStatus(status.status());
        customerRepository.save(customer);

        return convertCustomerToDto(customer);

    }

    private CustomerProfileResponse convertCustomerToDto(Customer customer) {
        CustomerProfileResponse dto = new CustomerProfileResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getImageUrl(),
                customer.getShippingAddress(),
                customer.getCredential().getUserStatus().name()
        );
        return dto;
    }
    private Customer getCustomerById(Long id){
        Customer customer=customerRepository.findByIdWithCredentials(id).orElseThrow(
                ()->new ResourceNotFoundException("customer with id: "+id+" not found.")
        );
        return customer;
    }
}
