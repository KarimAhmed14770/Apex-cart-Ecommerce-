package com.Kee.V2C.dto.payment;

import com.Kee.V2C.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
                            @NotNull(message = "payment method is required")
                            PaymentMethod paymentMethod,
                            @Size(min = 2, max = 100, message = "Cardholder name must be between 2 and 100 characters")
                            String cardHolderName,

                            @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
                                    message = "Expiry date must be in MM/YY format")

                            String expiryDate,
                            @Size(min = 15,max=16)
                            String creditCardNumber,
                            @Size(min = 3,max=3)
                            String cvv,
                            String idempotencyKey) {
}
