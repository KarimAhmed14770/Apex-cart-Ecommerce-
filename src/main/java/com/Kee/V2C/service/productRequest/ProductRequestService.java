package com.Kee.V2C.service.productRequest;

import com.Kee.V2C.dto.product.AdminAdditionOnProductRequest;
import com.Kee.V2C.dto.product.NewProductRequest;
import com.Kee.V2C.dto.product.ProductModelResponse;
import com.Kee.V2C.dto.product.ProductRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRequestService {

    ProductRequestResponse requestNewProduct(NewProductRequest newProductRequest);
    Page<ProductRequestResponse> getAllProductsRequests(Pageable page);
    Page<ProductRequestResponse> getPendingVendorsProductsRequests(Pageable page);
    ProductRequestResponse viewProductAddRequest(Long id);
    ProductRequestResponse rejectProductAddRequest(Long id);
    ProductModelResponse processProductAddRequest(Long requestId, AdminAdditionOnProductRequest adminAdditionOnProductRequest);

}
