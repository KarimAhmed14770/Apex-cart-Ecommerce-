package com.Kee.V2C.Repository;

import com.Kee.V2C.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord,Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
