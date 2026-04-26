package com.Kee.V2C.service.Customer;


import com.Kee.V2C.dto.cart.CartItemRequest;
import com.Kee.V2C.dto.cart.CartResponse;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.customer.*;
import com.Kee.V2C.dto.order.InvoiceResponse;

public interface CustomerService {
    CustomerProfileResponse myProfile();
    CustomerProfileResponse partialUpdateCustomerProfile(CustomerUpdateProfileRequest updateRequest);
    CartResponse addToCart(CartItemRequest cartItemRequest);
    CartResponse viewMyCart();
    CheckoutResponse checkOut(CheckOutRequest checkOutRequest);
    InvoiceResponse generateInvoice(long orderId);
}
