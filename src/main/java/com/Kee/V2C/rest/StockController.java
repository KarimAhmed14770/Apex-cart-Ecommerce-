package com.Kee.V2C.rest;

import com.Kee.V2C.dto.product.ProductAddToStockRequest;
import com.Kee.V2C.dto.product.ProductResponse;
import com.Kee.V2C.service.stock.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService){
        this.stockService=stockService;
    }
    @PostMapping(value = "/add-to-shop",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> addToStock(@Valid @ModelAttribute ProductAddToStockRequest productAddToStockRequest){
        ProductResponse productResponse=stockService.addProductToStock(productAddToStockRequest);
        URI location= ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(productResponse.id()).toUri();
        return ResponseEntity.created(location).body(productResponse);
    }

    @PatchMapping("/edit-product-stock/{id}")
    public ResponseEntity<ProductResponse> editStock(@PathVariable("id") Long id, @RequestParam int quantity){
        return ResponseEntity.ok(stockService.addStock(id,quantity));
    }
}
