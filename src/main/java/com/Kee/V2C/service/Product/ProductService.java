package com.Kee.V2C.service.Product;


import com.Kee.V2C.dto.product.ProductResponse;
import com.Kee.V2C.dto.product.ProductUpdateRequest;
import com.Kee.V2C.dto.product.ProductViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductViewResponse> getProductByDescription(String description, Pageable page);
    ProductViewResponse getProductById(Long id);

    Page<ProductResponse> showMyProducts(Pageable page);
    ProductResponse updateProductInfo(Long id, ProductUpdateRequest productUpdateRequest);

}
