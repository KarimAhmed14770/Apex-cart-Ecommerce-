package com.Kee.V2C.dto.payment;

import com.Kee.V2C.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
                            @NotNull(message = "payment method is required")
                            PaymentMethod paymentMethod,
                            @Size(min = 15,max=16)
                            String creditCardNumber,
                            @Size(min = 3,max=3)
                            String cvv,
                            String idempotencyKey) {
}
