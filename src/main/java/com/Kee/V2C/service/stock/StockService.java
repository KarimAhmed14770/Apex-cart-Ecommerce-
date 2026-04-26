package com.Kee.V2C.service.stock;

import com.Kee.V2C.dto.product.ProductAddToStockRequest;
import com.Kee.V2C.dto.product.ProductResponse;

public interface StockService {
    ProductResponse addProductToStock(ProductAddToStockRequest productAddToStockRequest);
    ProductResponse addStock(Long id,Integer quantity);

}
