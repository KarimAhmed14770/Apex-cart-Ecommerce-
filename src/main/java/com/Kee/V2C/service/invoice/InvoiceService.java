package com.Kee.V2C.service.invoice;

import com.Kee.V2C.dto.order.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse generateInvoice(long orderId);

}
