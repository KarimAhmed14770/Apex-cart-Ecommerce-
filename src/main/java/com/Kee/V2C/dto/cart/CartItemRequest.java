package com.Kee.V2C.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(@NotNull(message = "Product ID is required")
                              @Positive(message = "Product ID must be a positive integer")
                              Long productId,

                              @NotNull(message = "Quantity change is required")
                              @Min(value = -9999, message = "Quantity cannot be lower than -9999")
                              Integer quantity) {
}
