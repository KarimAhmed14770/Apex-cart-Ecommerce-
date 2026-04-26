package com.Kee.V2C.Repository;

import com.Kee.V2C.entity.Order;
import com.Kee.V2C.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {

    Page<Order> findByCustomerId(Long id, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query(value = "Select o " +
            "From Order o "+
    "Join fetch o.subOrders s "
    +"where o.id= :id")
    Optional<Order> findByIdWithSubOrders(@Param("id") Long  orderId);
}
