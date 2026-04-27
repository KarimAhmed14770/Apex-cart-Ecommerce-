package com.Kee.V2C.entity;

import com.Kee.V2C.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_orders")
@EntityListeners(AuditingEntityListener.class)
public class SubOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_price")
    BigDecimal totalPrice=BigDecimal.ZERO;

    @Column(name = "ordered_at")
    @CreatedDate
    private LocalDateTime orderedAt;

    @Column(name = "delivered_at")
    private LocalDateTime delivered_at;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    //also when we search for an order we don't  want the order items unless stated
    //when we save or delete an order we want order items to be saved or deleted automatically
    //so cacade is all
    @OneToMany(mappedBy = "subOrder",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    List<OrderItem> orderItems=new ArrayList<>();


    public SubOrder(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public LocalDateTime getDelivered_at() {
        return delivered_at;
    }

    public void setDelivered_at(LocalDateTime delivered_at) {
        this.delivered_at = delivered_at;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setSubOrder(this);
    }

    public void transitionTo(OrderStatus newStatus) {
        if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELED) {
            throw new IllegalStateException(
                    "Cannot change status of a sub-order that is already " + this.status);
        }
        if (newStatus == OrderStatus.DELIVERED && this.status != OrderStatus.SHIPPING) {
            throw new IllegalStateException(
                    "Sub-order must be SHIPPING before it can be marked DELIVERED");
        }
        this.status = newStatus;
    }
}
