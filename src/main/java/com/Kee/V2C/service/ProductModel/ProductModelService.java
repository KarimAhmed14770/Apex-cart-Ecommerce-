package com.Kee.V2C.service.ProductModel;

import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.entity.ProductModel;
import com.Kee.V2C.enums.ProductModelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductModelService {
    ProductModelResponse addProductModel(ProductModelRegisterRequest productModelRegisterRequest);
    ProductModelResponse updateProductModel(Long id, ProductModelUpdateRequest productModelUpdateRequest);
    ProductModelResponse softDeleteProductModel(Long id);
    Page<ProductModelResponse> searchProductModel(String name, String description, Long ownerId,
                                                  Long subCategoryId, Long brandId, Boolean isGlobal,
                                                  ProductModelStatus status, Pageable page);

    ProductModel getProductModelById(Long id);
    Page<ProductModel> getActiveProductModels(Pageable page);
    Page<ProductModel> getProductModelsByAttributes(String name, String description,
                                                            Long ownerId, Long subCategoryId, Long brandId,
                                                            Boolean isGlobal, ProductModelStatus status,
                                                            Pageable page);
    ProductModelResponse convertProductModelToDto(ProductModel productModel);


}
