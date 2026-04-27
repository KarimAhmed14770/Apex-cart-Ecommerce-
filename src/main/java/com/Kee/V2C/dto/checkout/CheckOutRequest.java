package com.Kee.V2C.dto.checkout;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckOutRequest(@NotNull(message = "Please specify whether to deliver to registered address")
                              Boolean deliverToRegisteredAddress,

                              @Size(max = 500, message = "Shipping address must be under 500 characters")
                              String shippingAddress) {

}
