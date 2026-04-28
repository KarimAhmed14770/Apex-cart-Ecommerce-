package com.Kee.V2C.service.payment;

import com.Kee.V2C.Repository.PaymentRecordRepository;
import com.Kee.V2C.dto.payment.PaymentRequest;
import com.Kee.V2C.entity.Order;
import com.Kee.V2C.entity.PaymentRecord;
import com.Kee.V2C.enums.PaymentMethod;
import com.Kee.V2C.enums.PaymentStatus;
import com.Kee.V2C.exception.BadPaymentInfo;
import com.Kee.V2C.exception.ResourceAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Boolean processPayment(PaymentRequest paymentRequest, Order order){
        if(paymentRequest.paymentMethod()==null) return false;//must provide a payment method

        if(paymentRequest.paymentMethod()== PaymentMethod.CASH_ON_DELIVERY){
            if(paymentRequest.creditCardNumber()==null && paymentRequest.cvv()==null
                    &&paymentRequest.idempotencyKey()==null) {
                PaymentRecord paymentRecord=new PaymentRecord(PaymentMethod.CASH_ON_DELIVERY,PaymentStatus.PENDING
                        ,order,order.getTotalPrice());
                order.setPaymentRecord(paymentRecord);
                paymentRecordRepository.save(paymentRecord);
                return true;//payment successfull
            }
            else{
                throw new BadPaymentInfo("Bad Payment Request");
            }
        }
        else{
            if(paymentRecordRepository.existsByIdempotencyKey(paymentRequest.idempotencyKey())){
                throw new ResourceAlreadyExistsException("this payment was already processed");
            }
            if(paymentRequest.creditCardNumber() != null
                    && paymentRequest.cvv() != null
                    && paymentRequest.idempotencyKey()!=null
                    &&paymentRequest.creditCardNumber().length()==16
                    && paymentRequest.cvv().length()==3 ){
                PaymentRecord paymentRecord=new PaymentRecord(paymentRequest.idempotencyKey(),
                        order, PaymentStatus.SUCCESS,PaymentMethod.CREDIT_CARD,order.getTotalPrice());
                order.setPaymentRecord(paymentRecord);
                paymentRecordRepository.save(paymentRecord);
                return true;
            }
            else{
               throw new BadPaymentInfo("Bad Card info");
            }
        }
    }
}
