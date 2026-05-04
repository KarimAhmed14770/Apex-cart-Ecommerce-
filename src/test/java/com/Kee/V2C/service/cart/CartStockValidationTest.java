package com.Kee.V2C.service.cart;

import com.Kee.V2C.Repository.*;
import com.Kee.V2C.entity.*;
import com.Kee.V2C.exception.InsufficientStockException;
import com.Kee.V2C.utils.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartStockValidationTest {

    @Mock private SecurityUtil securityUtil;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StockRepository stockRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartItem cartItemWith(int qty, Long productId, Long shopId) {
        Shop shop = mock(Shop.class);
        when(shop.getId()).thenReturn(shopId);

        Vendor vendor = mock(Vendor.class);
        when(vendor.getShop()).thenReturn(shop);

        Product product = mock(Product.class);
        when(product.getId()).thenReturn(productId);
        when(product.getVendor()).thenReturn(vendor);

        CartItem item = mock(CartItem.class);
        when(item.getQuantity()).thenReturn(qty);
        when(item.getProduct()).thenReturn(product);
        return item;
    }

    @Test
    void whenStockInsufficient_throwsInsufficientStockException() {
        CartItem item = cartItemWith(10, 1L, 1L);
        when(stockRepository.decrementProductStock(10, 1L, 1L)).thenReturn(0); // 0 rows updated = out of stock

        assertThrows(InsufficientStockException.class,
                () -> cartService.cartStockValidationAndUpdate(List.of(item)));
    }

    @Test
    void whenStockSufficient_completesWithoutException() {
        CartItem item = cartItemWith(5, 1L, 1L);
        when(stockRepository.decrementProductStock(5, 1L, 1L)).thenReturn(1); // 1 row updated = success

        assertDoesNotThrow(() -> cartService.cartStockValidationAndUpdate(List.of(item)));
    }
}
