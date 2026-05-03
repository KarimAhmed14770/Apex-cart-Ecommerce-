package com.Kee.V2C.rest;

import com.Kee.V2C.dto.product.AdminAdditionOnProductRequest;
import com.Kee.V2C.dto.product.NewProductRequest;
import com.Kee.V2C.dto.product.ProductModelResponse;
import com.Kee.V2C.dto.product.ProductRequestResponse;
import com.Kee.V2C.service.productRequest.ProductRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/product-requests")
public class ProductRequestController {

    private final ProductRequestService productRequestService;

    public ProductRequestController(ProductRequestService productRequestService){
        this.productRequestService=productRequestService;
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductRequestResponse> requestNewProduct(@Valid @ModelAttribute NewProductRequest newProductRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(productRequestService.requestNewProduct(newProductRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ProductRequestResponse>> getAllProductsRequests(Pageable page){
        return ResponseEntity.ok(productRequestService.getAllProductsRequests(page));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<Page<ProductRequestResponse>> getPendingVendorsProductsRequests(Pageable page){
        return ResponseEntity.ok(productRequestService.getPendingVendorsProductsRequests(page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductRequestResponse> viewProductAddRequest(@PathVariable("id") Long id){
        return ResponseEntity.ok(productRequestService.viewProductAddRequest(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/reject/{id}")
    public ResponseEntity<ProductRequestResponse> rejectProductRequest(@PathVariable("id") Long id){
        return ResponseEntity.ok(productRequestService.rejectProductAddRequest(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/process/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductModelResponse> processProductRequest(@PathVariable("id") Long id,
                                                                      @Valid @ModelAttribute AdminAdditionOnProductRequest adminAdditionOnProductRequest){
        return ResponseEntity.ok(productRequestService.processProductAddRequest(id,adminAdditionOnProductRequest));
    }

}
