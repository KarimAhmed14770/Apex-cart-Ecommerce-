package com.Kee.V2C.rest;

import com.Kee.V2C.dto.order.InvoiceResponse;
import com.Kee.V2C.service.invoice.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService){
        this.invoiceService=invoiceService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long orderId){
        return ResponseEntity.status(HttpStatus.OK).body(invoiceService.generateInvoice(orderId));
    }
}
