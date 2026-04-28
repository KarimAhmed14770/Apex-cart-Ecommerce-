package com.Kee.V2C.dto.payment;

import com.Kee.V2C.enums.PaymentMethod;

public record PaymentRequest(PaymentMethod paymentMethod, String creditCardNumber, String cvv, String idempotencyKey) {
}
