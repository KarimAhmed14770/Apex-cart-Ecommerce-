package com.Kee.V2C.rest;

import com.Kee.V2C.dto.product.NewProductRequest;
import com.Kee.V2C.dto.product.ProductModelResponse;
import com.Kee.V2C.dto.product.ProductRequestResponse;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.service.ProductModel.ProductModelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-models")
public class ProductModelController {
    private final ProductModelService productModelService;

    @Autowired
    public ProductModelController(ProductModelService productModelService){
        this.productModelService=productModelService;
    }
    @GetMapping
    public ResponseEntity<Page<ProductModelResponse>> getActiveProductModels(Pageable page){
        return ResponseEntity.status(HttpStatus.OK).
                body((productModelService.getActiveProductModels(page).map(productModelService::convertProductModelToDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModelResponse> getProductModelById(@PathVariable("id") Long id){
        return ResponseEntity.status(HttpStatus.OK)
                .body(productModelService.convertProductModelToDto(productModelService.getProductModelById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductModelResponse>> searchProductModel
            (@RequestParam(required = false) String description,
             @RequestParam(required = false) Long ownerId,
             @RequestParam(required = false) Long subCategoryId,
             @RequestParam(required = false) Long brandId,
             @RequestParam(required = false) Boolean isGlobal,
             Pageable page){
        return ResponseEntity.status(HttpStatus.OK)
                .body(productModelService.getProductModelsByAttributes(null,description,null,
                        subCategoryId,brandId,true, ProductModelStatus.ACTIVE,page)
                        .map(productModelService::convertProductModelToDto));
    }

    @PostMapping(value = "/product-requests",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductRequestResponse> requestNewProduct(@Valid @ModelAttribute NewProductRequest newProductRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(productModelService.requestNewProduct(newProductRequest));
    }

}
