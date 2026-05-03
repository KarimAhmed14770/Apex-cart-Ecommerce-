package com.Kee.V2C.service.Customer;


import com.Kee.V2C.dto.Authentication.StatusUpdateDto;
import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<CustomerProfileResponse> getAllCustomers(Pageable page);
    CustomerProfileResponse getCustomerProfileById(Long id);
    Page<CustomerProfileResponse> searchForCustomer(String userName, String email, String firstName,
                                                    String lastName, String shippingAddress,
                                                    UserStatus status, Pageable pageable);
    CustomerProfileResponse  modifyCustomerStatus(Long id, StatusUpdateDto status);
    CustomerProfileResponse myProfile();
    CustomerProfileResponse partialUpdateCustomerProfile(CustomerUpdateProfileRequest updateRequest);
}
