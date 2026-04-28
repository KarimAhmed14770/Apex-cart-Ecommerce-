package com.Kee.V2C.dto.checkout;

import com.Kee.V2C.dto.payment.PaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckOutRequest(@NotNull(message = "Please specify whether to deliver to registered address")
                              Boolean deliverToRegisteredAddress,

                              @Size(max = 500, message = "Shipping address must be under 500 characters")
                              String shippingAddress,

                              @NotNull(message = "Payment request is required")
                              @Valid //put this here so that validation cascades to paymentRequest
                              PaymentRequest paymentRequest) {

}
