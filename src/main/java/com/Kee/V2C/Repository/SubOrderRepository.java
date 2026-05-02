package com.Kee.V2C.Repository;

import com.Kee.V2C.entity.Order;
import com.Kee.V2C.entity.SubOrder;
import com.Kee.V2C.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubOrderRepository extends JpaRepository<SubOrder,Long> {
    @Query(value="SELECT o FROM SubOrder o " +
            "JOIN FETCH o.orderItems " +
            "WHERE o.id= :id")
    Optional<SubOrder> findByIdWithItemsDetails(Long id);

    @Query(value ="SELECT o FROM SubOrder o " +
            "JOIN FETCH o.orderItems oi " +
            "Join fetch oi.product "+
            "WHERE o.vendor.id= :id "+
            "ORDER BY o.orderedAt DESC",
            countQuery = "SELECT COUNT(o) FROM SubOrder o " +
                    "WHERE o.vendor.id = :id")
    Page<SubOrder> findByVendorId(@Param("id")Long id, Pageable page);

    @Query(value="SELECT o FROM SubOrder o "+
            "JOIN FETCH o.orderItems oi " +
            "Join fetch oi.product "+
            "WHERE o.vendor.id = :vendorId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:id IS NULL OR o.id = :id) "+
            "ORDER BY o.orderedAt DESC",
            countQuery = "SELECT COUNT(o) FROM SubOrder o " +
                    "WHERE o.vendor.id = :vendorId " +
                    "AND (:status IS NULL OR o.status = :status) " +
                    "AND (:id IS NULL OR o.id = :id)")
    Page<SubOrder> findByVendorIdWithFilters(@Param("vendorId")Long vendorId, @Param("status")OrderStatus status,
            @Param("id")Long id, Pageable pageable);
}
