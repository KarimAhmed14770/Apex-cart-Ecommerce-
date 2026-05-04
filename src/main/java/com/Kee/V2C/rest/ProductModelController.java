package com.Kee.V2C.rest;

import com.Kee.V2C.dto.product.*;
import com.Kee.V2C.enums.ProductModelStatus;
import com.Kee.V2C.service.ProductModel.ProductModelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/product-models")
public class ProductModelController {
    private final ProductModelService productModelService;

    @Autowired
    public ProductModelController(ProductModelService productModelService){
        this.productModelService=productModelService;
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductModelResponse> addProductModel(@ModelAttribute @Valid ProductModelRegisterRequest productModelRegisterRequest){
        ProductModelResponse response=productModelService.addProductModel(productModelRegisterRequest);
        URI location= ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductModelResponse>> getProductModelByAttribute(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean isGlobal,
            @RequestParam(required = false) ProductModelStatus status,
            Pageable page){
        return ResponseEntity.status(HttpStatus.OK).body(productModelService.searchProductModel(name, description,
                ownerId, subCategoryId, brandId, isGlobal, status, page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductModelResponse> updateProductModel(@PathVariable("id") Long id,
                                                                   @Valid @ModelAttribute ProductModelUpdateRequest productModelUpdateRequest){
        return ResponseEntity.status(HttpStatus.OK).body(productModelService.updateProductModel(id, productModelUpdateRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/delete/{id}")
    public ResponseEntity<ProductModelResponse> softDeleteProductModel(@PathVariable("id") Long id){
        return ResponseEntity.status(HttpStatus.OK).body(productModelService.softDeleteProductModel(id));
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

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/search/vendor")
    public ResponseEntity<Page<ProductModelResponse>> searchProductModel(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean myModels,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Long brandId,
            Pageable page) {

        Page<ProductModelResponse> result;
        if (Boolean.TRUE.equals(myModels)) {
            result = productModelService.getMyProductModels(description, subCategoryId, brandId, page);
        } else {
            result = productModelService.getProductModelsByAttributes(null, description, null,
                            subCategoryId, brandId, true, ProductModelStatus.ACTIVE, page)
                    .map(productModelService::convertProductModelToDto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }



}
