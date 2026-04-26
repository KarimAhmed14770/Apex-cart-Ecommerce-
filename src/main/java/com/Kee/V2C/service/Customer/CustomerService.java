package com.Kee.V2C.service.Customer;


import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.entity.Customer;

public interface CustomerService {
    CustomerProfileResponse myProfile();
    CustomerProfileResponse partialUpdateCustomerProfile(CustomerUpdateProfileRequest updateRequest);
    CartResponse addToCart(CartItemRequest cartItemRequest);
    CartResponse viewMyCart();
    CheckoutResponse checkOut(CheckOutRequest checkOutRequest);
    InvoiceResponse generateInvoice(long orderId);
}
