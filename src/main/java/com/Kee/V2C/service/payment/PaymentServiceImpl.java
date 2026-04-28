package com.Kee.V2C.service.payment;

import com.Kee.V2C.Repository.PaymentRecordRepository;
import com.Kee.V2C.dto.checkout.CheckOutRequest;
import com.Kee.V2C.dto.payment.PaymentRequest;
import com.Kee.V2C.entity.PaymentRecord;
import com.Kee.V2C.enums.PaymentMethod;
import com.Kee.V2C.enums.PaymentStatus;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRecordRepository paymentRecordRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRecordRepository paymentRecordRepository){
        this.paymentRecordRepository=paymentRecordRepository;
    }

    @Override
    public Boolean processPayment(CheckOutRequest checkOutRequest, BigDecimal amount){
        if(checkOutRequest.paymentRequest().paymentMethod()==null) return false;//must provide a payment method
        if(checkOutRequest.paymentRequest().paymentMethod()== PaymentMethod.CASH_ON_DELIVERY){
            return true;//payment successfull
        }
        else{
            if(paymentRecordRepository.existsByIdempotencyKey(checkOutRequest.paymentRequest().idempotencyKey())){
                throw new ResourceAlreadyExistsException("this payment was already processed");
            }
            if(checkOutRequest.paymentRequest().creditCardNumber().length()==20
                    && checkOutRequest.paymentRequest().cvv().length()==3){
                PaymentRecord paymentRecord=new PaymentRecord(checkOutRequest.paymentRequest().idempotencyKey(),
                        amount, PaymentStatus.SUCCESS);
                paymentRecordRepository.save(paymentRecord);
                return true;
            }
            else{
               throw new BadCredentialsException("Bad Card info");
            }
        }
    }
}
