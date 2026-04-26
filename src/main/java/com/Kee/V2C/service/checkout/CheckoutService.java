package com.Kee.V2C.service.checkout;

import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.checkout.CheckoutResponse;
import com.Kee.V2C.dto.order.InvoiceResponse;

public interface CheckoutService {
    CheckoutResponse checkOut(CheckOutRequest checkOutRequest);

}
