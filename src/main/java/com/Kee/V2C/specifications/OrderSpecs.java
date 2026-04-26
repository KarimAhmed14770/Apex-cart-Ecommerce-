package com.Kee.V2C.specifications;

import com.Kee.V2C.entity.Order;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.enums.OrderStatus;
import com.Kee.V2C.enums.ProductModelStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class OrderSpecs {
    private OrderSpecs(){}

    public static Specification<Order> hasId(Long id){
        return (root, query, criteriaBuilder) -> {
            if(id==null) return null;
            return criteriaBuilder.equal(root.get("id"),id );
        };
    }
    public static Specification<Order> hasCustomerId(Long id){
        return (root, query, criteriaBuilder) -> {
            if(id==null) return null;
            return criteriaBuilder.equal(root.get("customer").get("id"),id );
        };
    }
    public static Specification<Order> hasPriceBetween(BigDecimal lowerRange, BigDecimal higherRange){
        return (root, query, criteriaBuilder) -> {
            if(lowerRange==null && higherRange==null) return null;
            if(lowerRange!=null &&higherRange==null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"),lowerRange);
            }
            if(lowerRange==null &&higherRange!=null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"),higherRange);
            }
            return criteriaBuilder.between(root.get("totalPrice"),lowerRange,higherRange);
        };
    }
    public static Specification<Order> hasAddressContains(String address){
        return (root, query, criteriaBuilder) -> {
            if(address==null || address.isEmpty()) return null;
            return criteriaBuilder.like(root.get("shipping_address"),"%address%" );
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status){
        return (root,query,cb)->{
            if(status==null)return null;
            return cb.equal(root.get("status"),status);
        };
    }
}
