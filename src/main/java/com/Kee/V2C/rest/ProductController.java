package com.Kee.V2C.rest;


import com.Kee.V2C.dto.product.ProductResponse;
import com.Kee.V2C.dto.product.ProductUpdateRequest;
import com.Kee.V2C.dto.product.ProductViewResponse;
import com.Kee.V2C.service.Product.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public  ProductController(ProductService productService){
        this.productService=productService;
    }

        @GetMapping
        public ResponseEntity<Page<ProductViewResponse>> getProductsByDescription(@RequestParam String search, Pageable page){
            return ResponseEntity.ok(productService.getProductByDescription(search,page));
        }
    @GetMapping("/{id}")
    public ResponseEntity<ProductViewResponse> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @GetMapping("/vendor")
    public ResponseEntity<Page<ProductResponse>> getMyProducts(Pageable page){
        return ResponseEntity.ok(productService.showMyProducts(page));
    }

    @PatchMapping(value = "/vendor/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long id, @Valid @ModelAttribute ProductUpdateRequest productUpdateRequest){
        return ResponseEntity.ok(productService.updateProductInfo(id,productUpdateRequest));
    }

}
