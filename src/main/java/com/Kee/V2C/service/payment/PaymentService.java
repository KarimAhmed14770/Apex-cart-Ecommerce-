package com.Kee.V2C.service.payment;

import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.payment.PaymentRequest;
import com.Kee.V2C.entity.Order;
import com.Kee.V2C.entity.PaymentRecord;

import java.math.BigDecimal;

public interface PaymentService {
        Boolean processPayment(PaymentRequest paymentRequest, Order order);
}
