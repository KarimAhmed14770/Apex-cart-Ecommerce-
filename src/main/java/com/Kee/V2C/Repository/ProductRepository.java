package com.Kee.V2C.Repository;

import com.Kee.V2C.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> , JpaSpecificationExecutor<Product> {
    //returning only active products  by description containing
    Page<Product> findByDescriptionContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    @Query(value = "select p from Product p "
    +" Join fetch p.vendor "
    +"WHERE p.vendor.id= :id")
    Page<Product> findAllByVendorId(@Param("id") Long id, Pageable page);

    Page<Product> findProductByStockShopId(Long shopId,Pageable page);

}
