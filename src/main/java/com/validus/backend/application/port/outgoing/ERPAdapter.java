package com.validus.backend.application.port.outgoing;

import com.validus.backend.domain.model.Invoice;
import com.validus.backend.infrastructure.erp.dto.PurchaseOrder;
import com.validus.backend.infrastructure.erp.dto.SupplierProfile;
import com.validus.backend.domain.model.PaymentBatch;

public interface ERPAdapter {

    SupplierProfile getSupplier(String supplierId);

    PurchaseOrder getPurchaseOrder(String poNumber);

    String postAccountingEntry(Invoice invoice);

    void executePaymentBatch(PaymentBatch batch);
}
