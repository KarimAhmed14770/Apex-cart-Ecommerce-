package com.Kee.V2C.service.ProductModel;

import com.Kee.V2C.dto.product.NewProductRequest;
import com.Kee.V2C.dto.product.ProductModelResponse;
import com.Kee.V2C.dto.product.ProductRequestResponse;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.enums.ProductModelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductModelService {
    ProductModel getProductModelById(Long id);
    Page<ProductModel> getActiveProductModels(Pageable page);
    Page<ProductModel> getProductModelsByAttributes(String name, String description,
                                                            Long ownerId, Long subCategoryId, Long brandId,
                                                            Boolean isGlobal, ProductModelStatus status,
                                                            Pageable page);
    ProductModelResponse convertProductModelToDto(ProductModel productModel);
    ProductRequestResponse requestNewProduct(NewProductRequest newProductRequest);

}
