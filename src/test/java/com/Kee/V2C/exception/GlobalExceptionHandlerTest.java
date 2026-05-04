package com.Kee.V2C.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void duplicateOrder_returns409_withIdempotencyMessage() {
        ResponseEntity<UserErrorResponse> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("unique constraint violated"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("this request was already processed", response.getBody().getMessage());
    }

    @Test
    void insufficientStock_returns400_withExceptionMessage() {
        ResponseEntity<UserErrorResponse> response =
                handler.handleException(new InsufficientStockException("Product 1 is out of stock"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Product 1 is out of stock", response.getBody().getMessage());
    }
}
